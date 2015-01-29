package com.sosop.zkjedis.agent.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class SlaveNodeListen extends CacheListener implements IZKListener {

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        PathChildrenCacheEvent.Type type = event.getType();
        if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED
                && client.getZookeeperClient().isConnected()) {

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
