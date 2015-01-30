package com.sosop.zkJedis.client.zk.listener;

import java.io.Closeable;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.utils.CloseableUtils;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class ClusterListener extends CacheListener implements IZKListener {

    private ClusterInfo clusters;
    private PathChildrenCache cache;

    public ClusterListener(ClusterInfo clusters) {
        this.clusters = clusters;
    }

    @Override
    public void start(CuratorFramework client, String path) throws Exception {
        cache = pathChildrenCache(client, path, false);
        cache.start();
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
            String path = event.getData().getPath();
            new MasterListener(clusters).start(client, path);
        }
    }

    @Override
    public void jobNode(CuratorFramework client) {
        // TODO do nothing
    }

    @Override
    public void close(Closeable closeable) {
        CloseableUtils.closeQuietly(cache);
    }

}
