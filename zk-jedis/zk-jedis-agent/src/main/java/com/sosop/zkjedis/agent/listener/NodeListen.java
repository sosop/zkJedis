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
import com.sosop.zkjedis.agent.opt.ZkJedis;

public class NodeListen extends CacheListener implements IZKListener {

    private String slaveNodePath;
    private ZkJedis jedis;

    public NodeListen(String slaveNodePath, ZkJedis jedis) {
        super();
        this.slaveNodePath = slaveNodePath;
        this.jedis = jedis;
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
                System.out.println(slaves.get(0));
                if (slaves.get(0).equals(slavePath[3])) {
                    jedis.slaveOfNoOne();
                    ZKUtil.create(client, StringUtil.append(Constants.ZK.CLUSTERS, "/",
                            masterPath[2], "/", slavePath[3]), CreateMode.EPHEMERAL, data
                            .getBytes());
                    for (String s : slaves) {
                        ZKUtil.delete(client, StringUtil.append(ssPath, "/", s));
                    }
                    ZKUtil.delete(client, ssPath);
                } else {
                    ZKUtil.create(client, StringUtil.append(newSlavePath, "/", slaves.get(0)),
                            CreateMode.EPHEMERAL);
                    String[] hap = slaves.get(0).split(":");
                    jedis.slaveOf(hap[0], hap[1]);
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
