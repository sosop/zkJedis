package com.sosop.zkJedis.client.zk.listener;

import java.io.Closeable;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.utils.CloseableUtils;

import com.sosop.zkJedis.client.redis.ClusterInfo;
import com.sosop.zkJedis.common.utils.StringUtil;
import com.sosop.zkJedis.common.utils.ZKUtil;
import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class MasterListener extends CacheListener implements IZKListener {

    private ClusterInfo clusters;
    private PathChildrenCache cache;

    public MasterListener(ClusterInfo clusters) {
        this.clusters = clusters;
    }

    @Override
    public void start(CuratorFramework client, String path) throws Exception {
        cache = pathChildrenCache(client, path, false);
        cache.start();
    }

    @Override
    public void jobPathChildren(CuratorFramework client, PathChildrenCacheEvent event) {
        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED
                || event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            String path = event.getData().getPath();

            int ind1 = path.lastIndexOf("/");
            String clusterPath = path.substring(0, ind1);
            String node = path.substring(ind1 + 1);
            int ind2 = clusterPath.lastIndexOf("/");
            String clusterName = clusterPath.substring(ind2 + 1);
            if (StringUtil.isNull(clusters.getDefaultName())) {
                clusters.setDefaultName(clusterName);
            }
            int index = -1;
            if (event.getType() == Type.CHILD_ADDED) {
                index = Integer.valueOf(ZKUtil.getData(client, path));
            }
            clusters.rebuildCluster(clusterName, node, index);
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
