package com.sosop.zkjedis.agent.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.utils.CloseableUtils;

import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class SlaveNodeListen extends CacheListener implements IZKListener {

    private String clusterPath;
    private String slaveNodePath;
    private PathChildrenCache cache;

    public SlaveNodeListen(String clusterPath, String slaveNodePath) {
        super();
        this.clusterPath = clusterPath;
        this.slaveNodePath = slaveNodePath;
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event)
            throws Exception {
        PathChildrenCacheEvent.Type type = event.getType();
        if (type == PathChildrenCacheEvent.Type.CHILD_REMOVED
                && client.getZookeeperClient().isConnected()) {
            String path = event.getData().getPath().trim();
            int ind1 = path.lastIndexOf("/");
            int ind2 = slaveNodePath.lastIndexOf("/");
            if (path.equals(slaveNodePath.trim())) {
                // promote
                String node = path.substring(ind1 + 1);


            }
            System.out.println(clusterPath);
            System.out.println(slaveNodePath);
            System.out.println();
        }
    }

    @Override
    public void jobNode(CuratorFramework client) throws Exception {
        // do nothing ...
    }

    @Override
    public void start(CuratorFramework client, String path) throws Exception {
        cache = pathChildrenCache(client, path, false);
        cache.start();
    }

    @Override
    public void close() {
        CloseableUtils.closeQuietly(cache);
    }



}
