package com.sosop.zkjedis.agent;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sosop.zkJedis.common.utils.Constants;
import com.sosop.zkJedis.common.utils.CreateClient;
import com.sosop.zkJedis.common.utils.FileUtil;
import com.sosop.zkJedis.common.utils.NodeMode;
import com.sosop.zkJedis.common.utils.PropsUtil;
import com.sosop.zkJedis.common.utils.StringUtil;
import com.sosop.zkJedis.common.utils.ZKUtil;
import com.sosop.zkjedis.agent.exception.UnknownHostAndPortException;
import com.sosop.zkjedis.agent.listener.NodeListen;
import com.sosop.zkjedis.agent.opt.ZkJedis;

public class Agent {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    public static final ExponentialBackoffRetry RETRY_POLICY;

    private Properties props = null;

    private CuratorFramework client;

    private String clusterPath;

    private String slavesPath;

    private String slaveNodePath;

    private ZkJedis jedis;

    private static final int maxCheckTimes = 10;

    static {
        RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);
    }

    public static void main(String[] args) throws Exception {
        Agent agent =
                new Agent(PropsUtil.properties(FileUtil.getAbsoluteFile("/data/config.properties")));
        if ("m".equals(args[0])) {
            agent.init(NodeMode.MASTER);
        } else if ("s".equals(args[0])) {
            agent.init(NodeMode.SLAVE);
        }
        new NodeListen(agent.slaveNodePath, agent.jedis).start(agent.client, agent.clusterPath);
        int flag = 0;
        int sleepTime = 2000;
        while (true) {
            try {
                agent.jedis.ping();
                sleepTime = 2000;
                flag = 0;
            } catch (Exception e) {
                LOG.info("connet refused " + flag);
                sleepTime = 100;
                flag++;
            }
            if (flag == maxCheckTimes) {
                agent.deleteNode();
                break;
            }
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }
        agent.close();
    }

    private Agent(Properties props) {
        super();
        this.props = props;
    }


    // initial current node
    private void init(NodeMode mode) throws UnknownHostAndPortException {
        String connString = props.getProperty("zk.zkConnect", "localhost:2181");
        Integer connTimeout =
                Integer.parseInt(props.getProperty("zk.zkConnectionTimeoutMs",
                        String.valueOf(Constants.ZK.CONNECTION_TIMEOUT_MS)));
        Integer sessionTimeout =
                Integer.parseInt(props.getProperty("zk.zkSessionTimeoutMs",
                        String.valueOf(Constants.ZK.SESSION_TIMEOUT_MS)));
        client =
                CreateClient.create(connString, Constants.ZK.NAMESPACE, RETRY_POLICY, connTimeout,
                        sessionTimeout);
        client.start();
        String[] hap = props.getProperty("redis.hostAndPort").split(":");
        jedis = new ZkJedis(hap[0], Integer.valueOf(hap[1]));
        if (mode == NodeMode.MASTER) {
            createCluster();
            createMasterNode();
        } else {
            createSlaves();
            createSlaveNode();
        }
    }

    // if current node is master and add it to cluster
    private void createCluster() {
        String clusterName = props.getProperty("cluster.name", "my-cluster");
        clusterPath = StringUtil.append(Constants.ZK.CLUSTERS, "/", clusterName);
        ZKUtil.create(client, clusterPath, CreateMode.PERSISTENT, "0".getBytes());
    }

    // create slaves dir on zk, and make the slave follow the master
    private void createSlaves() throws UnknownHostAndPortException {
        String clusterName = props.getProperty("cluster.name");
        clusterPath = StringUtil.append(Constants.ZK.CLUSTERS, "/", clusterName);
        String master = props.getProperty("redis.master");
        if (clusterName == null || master == null) {
            this.close();
            throw new UnknownHostAndPortException(
                    "set property like this #[cluster.name=xxx | redis.master=192.168.1.10:6371]");
        }
        String masterPath = StringUtil.append(Constants.ZK.CLUSTERS, "/", clusterName, "/", master);
        slavesPath = StringUtil.append(Constants.ZK.SLAVES, "/", master);
        if (ZKUtil.notExist(client, masterPath)) {
            this.close();
            throw new UnknownHostAndPortException("集群或master不存在");
        }
        if (ZKUtil.notExist(client, slavesPath)) {
            String index = ZKUtil.getData(client, masterPath);
            ZKUtil.create(client, slavesPath, CreateMode.PERSISTENT, index.getBytes());
            String[] hap = master.split(":");
            jedis.slaveOf(hap[0], hap[1]);
        }
    }

    private void createMasterNode() throws UnknownHostAndPortException {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        if (hostAndPort == null) {
            this.close();
            throw new UnknownHostAndPortException(
                    "set property like this redis.hostAndPort=192.168.1.10:6371");
        }
        int index = Integer.valueOf(ZKUtil.getData(client, clusterPath));
        String nodePath = StringUtil.append(clusterPath, "/", hostAndPort);
        ZKUtil.setData(client, clusterPath, String.valueOf(index + 1).getBytes());
        ZKUtil.create(client, nodePath, CreateMode.EPHEMERAL, String.valueOf(index).getBytes());
    }

    private void createSlaveNode() throws UnknownHostAndPortException {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        if (hostAndPort == null) {
            this.close();
            throw new UnknownHostAndPortException(
                    "set property like this #redis.hostAndPort=192.168.1.10:6371");
        }
        slaveNodePath = StringUtil.append(slavesPath, "/", hostAndPort);
        ZKUtil.create(client, slaveNodePath, CreateMode.EPHEMERAL);
    }

    private void deleteNode() {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        String path = StringUtil.append(clusterPath, "/", hostAndPort);
        ZKUtil.delete(client, path);
    }

    public void close() {
        CloseableUtils.closeQuietly(client);
    }
}
