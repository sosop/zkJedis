package com.sosop.zkJedis.client.zk;

import java.util.Properties;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sosop.zkJedis.common.utils.CreateClient;
import com.sosop.zkJedis.common.utils.ZKUtil;


public class ZkAction {

    private static final Logger LOG = LoggerFactory.getLogger(ZkAction.class);

    private static final String NAMESPACE = "redis-clusters";

    private static final String CLUSTERS = "/clusters";

    private static final String SLAVES = "/slaves";

    private static final ExponentialBackoffRetry RETRY_POLICY;

    private final Integer CONNECTION_TIMEOUT_MS = 5000;

    private final Integer SESSION_TIMEOUT_MS = 10000;

    private Properties props = null;

    private CuratorFramework client;


    static {
        RETRY_POLICY = new ExponentialBackoffRetry(1000, 3);
    }


    public ZkAction(Properties props) {
        super();
        this.props = props;
    }

    public void init() {
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
        createSecondAndWatch();
    }

    private void createSecondAndWatch() {
        ZKUtil.create(client, CLUSTERS, CreateMode.PERSISTENT);
        ZKUtil.create(client, SLAVES, CreateMode.PERSISTENT);
        ZKUtil.addChildrenWatcher(client, CLUSTERS, new ClustersWatcher(client, CLUSTERS));
        String path = null;
        for (String cluster : ZKUtil.children(client, CLUSTERS)) {
            path = CLUSTERS + "/" + cluster;
            ZKUtil.addChildrenWatcher(client, path, new MasterWatcher(client, path));
        }
    }

    public void close() {
        this.client.close();
    }
}
