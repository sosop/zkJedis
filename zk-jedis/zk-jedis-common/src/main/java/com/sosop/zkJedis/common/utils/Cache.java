package com.sosop.zkJedis.common.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

public class Cache {

    public static PathChildrenCache pathChilderCache(CuratorFramework client, String path,
            boolean cacheData) {
        final PathChildrenCache cache = new PathChildrenCache(client, path, cacheData);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework framework, PathChildrenCacheEvent event)
                    throws Exception {
                if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED
                        || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
                    System.out.println(event.getData().getPath());
                }
            }
        });
        return cache;
    }

    public static NodeCache nodeCache(CuratorFramework client, String path, boolean cacheData) {
        final NodeCache cache = new NodeCache(client, path, cacheData);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println(cache.getCurrentData().getData());
            }

        });
        return cache;
    }
}
