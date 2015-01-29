package com.sosop.zkJedis.client.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.zkCache.CacheListener;

public class ClusterListener extends CacheListener implements IZKListener {

    private ClusterInfo clusters;

    @Override
    public void start(CuratorFramework client, String path, ClusterInfo clusters) throws Exception {
        this.clusters = clusters;
        pathChilderCache(client, path, false).start();
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
            String path = event.getData().getPath();
            new MasterListener().start(client, path, clusters);
        }
    }

    @Override
    public void jobNode(CuratorFramework client) {
        // TODO do nothing
    }

}
