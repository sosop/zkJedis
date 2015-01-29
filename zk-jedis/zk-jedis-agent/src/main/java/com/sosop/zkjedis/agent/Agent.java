package com.sosop.zkjedis.agent;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sosop.zkJedis.common.utils.CreateClient;
import com.sosop.zkJedis.common.utils.FileUtil;
import com.sosop.zkJedis.common.utils.NodeMode;
import com.sosop.zkJedis.common.utils.PropsUtil;
import com.sosop.zkJedis.common.utils.StringUtil;
import com.sosop.zkJedis.common.utils.ZKUtil;
import com.sosop.zkjedis.agent.exception.UnknownHostAndPortException;
import com.sosop.zkjedis.agent.opt.ZkJedis;
import com.sosop.zkjedis.agent.watcher.SlaveNodeWatcher;

public class Agent {

    private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

    private static final String NAMESPACE = "redis-clusters";

    private static final String CLUSTERS = "/clusters";

    private static final String SLAVES = "/slaves";

    private static final ExponentialBackoffRetry RETRY_POLICY;

    private final Integer CONNECTION_TIMEOUT_MS = 5000;

    private final Integer SESSION_TIMEOUT_MS = 10000;

    private Properties props = null;

    private CuratorFramework client;

    private String clusterPath;

    private String slavesPath;

    private ZkJedis jedis;

    private static final int maxCheckTimes = 10;

    static {
        RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);
    }

    public static void main(String[] args) throws UnknownHostAndPortException, InterruptedException {
        Agent agent = new Agent(PropsUtil.properties(FileUtil.getFile("/data/config.properties")));


        if ("m".equals(args[0])) {
            agent.init(NodeMode.MASTER);
        } else if ("s".equals(args[0])) {
            agent.init(NodeMode.SLAVE);
        }
        int flag = 0;
        int sleepTime = 2000;
        while (true) {
            try {
                agent.jedis.ping();
            } catch (Exception e) {
                LOG.info("connet refused " + flag);
                sleepTime = 100;
                flag++;
            }
            if (flag == maxCheckTimes) {
                agent.deleteNode();
                if ("m".equals(args[0])) {
                    agent.slaveToMaster();
                }
                break;
            }
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }

    }

    private Agent(Properties props) {
        super();
        this.props = props;
    }

    private void init(NodeMode mode) throws UnknownHostAndPortException {
        String connString = props.getProperty("zk.zkConnect", "localhost:2181");
        Integer connTimeout =
                Integer.parseInt(props.getProperty("zk.zkConnectionTimeoutMs",
                        String.valueOf(CONNECTION_TIMEOUT_MS)));
        Integer sessionTimeout =
                Integer.parseInt(props.getProperty("zk.zkSessionTimeoutMs",
                        String.valueOf(SESSION_TIMEOUT_MS)));
        client =
                CreateClient.create(connString, NAMESPACE, RETRY_POLICY, connTimeout,
                        sessionTimeout);
        client.start();
        client.getCuratorListenable().addListener(new SlaveNodeWatcher());
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

    private void createCluster() {
        String clusterName = props.getProperty("cluster.name", "my-cluster");
        clusterPath = StringUtil.append(CLUSTERS, "/", clusterName);
        ZKUtil.create(client, clusterPath, CreateMode.PERSISTENT, "0".getBytes());
    }

    private void createSlaves() throws UnknownHostAndPortException {
        String clusterName = props.getProperty("cluster.name");
        String master = props.getProperty("redis.master");
        if (clusterName == null || master == null) {
            throw new UnknownHostAndPortException(
                    "set property like this #[cluster.name=xxx | redis.master=192.168.1.10:6371]");
        }
        String masterPath = StringUtil.append(CLUSTERS, "/", clusterName, "/", master);
        slavesPath = StringUtil.append(SLAVES, "/", master);
        if (ZKUtil.notExist(client, masterPath)) {
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
            throw new UnknownHostAndPortException(
                    "set property like this redis.hostAndPort=192.168.1.10:6371");
        }
        int index = Integer.valueOf(ZKUtil.getData(client, clusterPath)) + 1;
        String max = String.valueOf(index);
        String nodePath = StringUtil.append(clusterPath, "/", hostAndPort);
        ZKUtil.setData(client, clusterPath, max.getBytes());
        ZKUtil.create(client, nodePath, CreateMode.EPHEMERAL, max.getBytes());
    }

    private void createSlaveNode() throws UnknownHostAndPortException {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        if (hostAndPort == null) {
            throw new UnknownHostAndPortException(
                    "set property like this #redis.hostAndPort=192.168.1.10:6371");
        }
        String nodePath = StringUtil.append(slavesPath, "/", hostAndPort);
        ZKUtil.create(client, nodePath, CreateMode.EPHEMERAL);
    }

    private void deleteNode() {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        String path = StringUtil.append(clusterPath, "/", hostAndPort);
        ZKUtil.delete(client, path);
    }

    private void slaveToMaster() {
        String hostAndPort = props.getProperty("redis.hostAndPort");
        String path = StringUtil.append(SLAVES, "/", hostAndPort);
        try {
            if (ZKUtil.exist(client, path)) {
                List<String> children = ZKUtil.children(client, path);
                if (children.get(0) != null) {
                    // String index = ZKUtil.getData(client, path);
                    // ZKUtil.create(client, StringUtil.append(clusterPath, "/", children.get(0)),
                    // CreateMode.EPHEMERAL, index.getBytes());
                    ZKUtil.delete(client, StringUtil.append(path, "/", children.get(0)));
                }
                if (children.size() == 1) {
                    ZKUtil.delete(client, path);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }
}
