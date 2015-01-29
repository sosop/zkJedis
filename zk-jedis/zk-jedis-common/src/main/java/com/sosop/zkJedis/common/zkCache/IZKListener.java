package com.sosop.zkJedis.common.zkCache;

import org.apache.curator.framework.CuratorFramework;

public interface IZKListener {
    public void start(CuratorFramework client, String path) throws Exception;
}
