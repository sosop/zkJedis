package com.sosop.zkJedis.common.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ZKUtil.class);

    public static void create(CuratorFramework client, String path, CreateMode mode) {
        try {
            if (client.checkExists().forPath(path) == null) {
                client.create().withMode(mode).forPath(path);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }

    public static void delete(CuratorFramework client, String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                client.delete().forPath(path);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }

}
