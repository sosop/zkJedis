package com.sosop.zkJedis.client.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.utils.ZKUtil;

public class ClustersWatcher implements CuratorWatcher {


    private CuratorFramework client;
    private String nodePath;
    private ClusterInfo clusters;

    public ClustersWatcher(CuratorFramework client, String nodePath, ClusterInfo clusters) {
        this.client = client;
        this.nodePath = nodePath;
        this.clusters = clusters;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        StringBuilder sb = new StringBuilder();
        String tmpPath = null;
        if (event.getType() == EventType.NodeChildrenChanged) {
            List<String> clusters = ZKUtil.children(client, nodePath);
            for (String cluster : clusters) {
                if (!this.clusters.getClusters().containsKey(cluster)) {
                    this.clusters.getClusters().put(cluster, null);
                }
                tmpPath = sb.append(nodePath).append("/").append(cluster).toString();
                client.getChildren()
                        .usingWatcher(new MasterWatcher(client, tmpPath, this.clusters))
                        .forPath(tmpPath);
                sb.delete(0, sb.length());
            }
        }
        if (event.getType() != EventType.NodeDeleted) {
            client.getChildren().usingWatcher(this).forPath(nodePath);
        }
    }
}
