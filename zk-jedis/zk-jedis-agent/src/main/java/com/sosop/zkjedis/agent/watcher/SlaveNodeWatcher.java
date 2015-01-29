package com.sosop.zkjedis.agent.watcher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;

public class SlaveNodeWatcher implements CuratorListener {

    @Override
    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
        System.out.println(event.getName());
        System.out.println(event.getData());
        System.out.println(event.getPath());
    }

}
