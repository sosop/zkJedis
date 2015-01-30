package com.sosop.zkJedis.common.zkCache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.CloseableUtils;

public abstract class CacheListener {

    private PathChildrenCache pathCache;
    private NodeCache nodeCache;

    public abstract void job(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception;


    public PathChildrenCache pathChildrenCache(CuratorFramework client, String path,
            boolean cacheData) {
        this.pathCache = new PathChildrenCache(client, path, cacheData);
        this.pathCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework framework, PathChildrenCacheEvent event)
                    throws Exception {
                job(framework, event);
            }
        });
        return this.pathCache;
    }

    public NodeCache nodeCache(final CuratorFramework client, String path, boolean cacheData) {
        this.nodeCache = new NodeCache(client, path, cacheData);
        this.nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                job(client, null);
            }
        });
        return this.nodeCache;
    }

    public void close() {
        CloseableUtils.closeQuietly(pathCache);
        CloseableUtils.closeQuietly(nodeCache);
    }
}
