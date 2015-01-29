package com.sosop.zkJedis.common.zkCache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public abstract class CacheListener {

    public abstract void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception;

    public abstract void jobNode(CuratorFramework client) throws Exception;

    public PathChildrenCache pathChilderCache(CuratorFramework client, String path,
            boolean cacheData) {
        final PathChildrenCache cache = new PathChildrenCache(client, path, cacheData);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework framework, PathChildrenCacheEvent event)
                    throws Exception {
                jobPathChildren(framework, event);
            }
        });
        return cache;
    }

    public NodeCache nodeCache(final CuratorFramework client, String path, boolean cacheData) {
        final NodeCache cache = new NodeCache(client, path, cacheData);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                jobNode(client);
            }

        });
        return cache;
    }
}
