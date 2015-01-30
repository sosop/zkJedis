package com.sosop.zkJedis.client.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class ClusterListener extends CacheListener implements IZKListener {

    private ClusterInfo clusters;

    public ClusterListener(ClusterInfo clusters) {
        this.clusters = clusters;
    }

    @Override
    public void start(CuratorFramework client, String path) throws Exception {
        pathChildrenCache(client, path, false).start();
    }

    @Override
    public void job(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
            String path = event.getData().getPath();
            new MasterListener(clusters).start(client, path);
        }
    }

    @Override
    public void close() {
        super.close();
    }

}
