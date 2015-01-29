package com.sosop.zkJedis.client.zk.listener;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.utils.StringUtil;
import com.sosop.zkJedis.common.utils.ZKUtil;
import com.sosop.zkJedis.common.zkCache.CacheListener;

public class MasterListener extends CacheListener implements IZKListener {

    private ClusterInfo clusters;

    @Override
    public void start(CuratorFramework client, String path, ClusterInfo clusters) throws Exception {
        this.clusters = clusters;
        pathChilderCache(client, path, false).start();
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event) {
        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED
                || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            String path = event.getData().getPath();
            int index = path.lastIndexOf("/");
            String clusterPath = path.substring(0, index);
            List<String> servers = ZKUtil.children(client, clusterPath);
            int ind = clusterPath.lastIndexOf("/");
            String clusterName = clusterPath.substring(ind + 1);
            if (StringUtil.isNull(clusters.getDefaultName())) {
                clusters.setDefaultName(clusterName);
            }
            clusters.rebuildCluster(clusterName, servers);
        }
    }

    @Override
    public void jobNode(CuratorFramework client) {
        // TODO do nothing
    }

}
