package com.sosop.zkJedis.client.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.sosop.zkJedis.client.redis.ClusterInfo;

public class MasterWatcher implements CuratorWatcher {

    private CuratorFramework client;

    private String nodePath;

    private ClusterInfo clusters;

    public MasterWatcher(CuratorFramework client, String nodePath, ClusterInfo clusters) {
        this.client = client;
        this.nodePath = nodePath;
        this.clusters = clusters;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        if (event.getType() == EventType.NodeChildrenChanged) {
            List<String> servers = client.getChildren().forPath(nodePath);
            int index = event.getPath().lastIndexOf("/") + 1;
            clusters.rebuildCluster(event.getPath().substring(index), servers);
        }
        if (event.getType() != EventType.NodeDeleted) {
            client.getChildren().usingWatcher(this).forPath(nodePath);
        }
    }

}
