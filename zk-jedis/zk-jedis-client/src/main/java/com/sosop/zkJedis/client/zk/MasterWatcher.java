package com.sosop.zkJedis.client.zk;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;

public class MasterWatcher implements CuratorWatcher {

    private CuratorFramework client;

    private String nodePath;

    public MasterWatcher(CuratorFramework client, String nodePath) {
        this.client = client;
        this.nodePath = nodePath;
    }

    @Override
    public void process(WatchedEvent event) throws Exception {
        if (event.getType() == EventType.NodeChildrenChanged) {
            List<String> clusters = client.getChildren().forPath(nodePath);
            for (String cluster : clusters) {
                System.out.println("MasterListener");
                System.out.println(cluster);
            }
        }
        if (event.getType() != EventType.NodeDeleted) {
            client.getChildren().usingWatcher(this).forPath(nodePath);
        }
    }

}
