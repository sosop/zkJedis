package com.sosop.zkjedis.agent.listener;


import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;

import com.sosop.zkJedis.common.utils.Constants;
import com.sosop.zkJedis.common.utils.StringUtil;
import com.sosop.zkJedis.common.utils.ZKUtil;
import com.sosop.zkJedis.common.zkCache.CacheListener;
import com.sosop.zkJedis.common.zkCache.IZKListener;

public class NodeListen extends CacheListener implements IZKListener {

    private String slaveNodePath;

    public NodeListen(String slaveNodePath) {
        super();
        this.slaveNodePath = slaveNodePath;
    }

    @Override
    public void job(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        PathChildrenCacheEvent.Type type = event.getType();
        if (StringUtil.notNull(this.slaveNodePath)
                && type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            String[] masterPath = event.getData().getPath().split("/");
            String[] slavePath = this.slaveNodePath.split("/");
            List<String> slaves;
            String ssPath = StringUtil.append("/", slavePath[1], "/", slavePath[2]);
            if (masterPath[3].equals(slavePath[2])
                    && (slaves = ZKUtil.children(client, ssPath)).size() > 0) {
                String data = ZKUtil.getData(client, ssPath);
                String newSlavePath = StringUtil.append(Constants.ZK.SLAVES, "/", slavePath[3]);
                ZKUtil.create(client, newSlavePath, CreateMode.PERSISTENT, data.getBytes());
                if (slaves.get(0).equals(slavePath[3])) {
                    slaves.remove(0);
                    System.out.println(StringUtil.append(Constants.ZK.CLUSTERS, "/", masterPath[2],
                            "/", slaves.get(0)));
                    ZKUtil.create(client, StringUtil.append(Constants.ZK.CLUSTERS, "/",
                            masterPath[2], "/", slaves.get(0)), CreateMode.EPHEMERAL, data
                            .getBytes());
                    for (String s : slaves) {
                        ZKUtil.delete(client, StringUtil.append(ssPath, "/", s));
                    }
                    ZKUtil.delete(client, ssPath);

                } else {
                    ZKUtil.create(client, StringUtil.append(newSlavePath, "/", slavePath[3]),
                            CreateMode.EPHEMERAL);
                }
            }
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
