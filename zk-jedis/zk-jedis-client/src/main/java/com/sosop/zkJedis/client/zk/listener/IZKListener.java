package com.sosop.zkJedis.client.zk.listener;

import org.apache.curator.framework.CuratorFramework;

import com.sosop.zkJedis.client.redis.ClusterInfo;

public interface IZKListener {
    public void start(CuratorFramework client, String path, ClusterInfo clsuters) throws Exception;
}
