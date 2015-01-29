package com.sosop.zkjedis.agent.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class SlavesListen extends CacheListener implements IZKListener {

    private String clusterPath;
    private String slaveNodePath;

    public SlavesListen(String clusterPath, String slaveNodePath) {
        super();
        this.clusterPath = clusterPath;
        this.slaveNodePath = slaveNodePath;
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        PathChildrenCacheEvent.Type type = event.getType();
        if (type == PathChildrenCacheEvent.Type.CHILD_ADDED) {
            new SlaveNodeListen(this.clusterPath, this.slaveNodePath).start(client, event.getData()
                    .getPath());
        }
    }

    @Override
    public void jobNode(CuratorFramework client) throws Exception {
        // do nothing ...
    }

    @Override
    public void start(CuratorFramework client, String path) throws Exception {
        pathChilderCache(client, path, false).start();
    }



}