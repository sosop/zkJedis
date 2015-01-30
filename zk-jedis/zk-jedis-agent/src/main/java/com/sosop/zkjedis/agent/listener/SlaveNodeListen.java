package com.sosop.zkjedis.agent.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class SlaveNodeListen extends CacheListener implements IZKListener {

    private String clusterPath;
    private String slaveNodePath;

    public SlaveNodeListen(String clusterPath, String slaveNodePath) {
        super();
        this.clusterPath = clusterPath;
        this.slaveNodePath = slaveNodePath;
    }

    @Override
    public void job(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
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
    public void start(CuratorFramework client, String path) throws Exception {
        pathChildrenCache(client, path, false).start();
    }

    @Override
    public void close() {
        super.close();
    }



}
