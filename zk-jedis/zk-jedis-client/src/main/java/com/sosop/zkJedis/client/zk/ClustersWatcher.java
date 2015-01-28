package com.sosop.zkJedis.client.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

public class ClustersWatcher implements CuratorWatcher {


    private CuratorFramework client;
    private String nodePath;

    public ClustersWatcher(CuratorFramework client, String nodePath) {
        this.client = client;
        this.nodePath = nodePath;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        StringBuilder sb = new StringBuilder();
        String tmpPath = null;
        if (event.getType() == EventType.NodeChildrenChanged) {
            List<String> clusters = client.getChildren().forPath(nodePath);
            for (String cluster : clusters) {
                tmpPath = sb.append(nodePath).append("/").append(cluster).toString();
                System.out.println("CLusterListener");
                client.getChildren().usingWatcher(new MasterWatcher(client, tmpPath))
                        .forPath(tmpPath);
                sb.delete(0, sb.length());
            }
        }
        if (event.getType() != EventType.NodeDeleted) {
            client.getChildren().usingWatcher(this).forPath(nodePath);
        }
    }
}
