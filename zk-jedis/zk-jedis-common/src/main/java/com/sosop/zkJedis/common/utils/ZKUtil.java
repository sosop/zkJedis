package com.sosop.zkJedis.common.utils;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
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

    public static void addChildrenWatcher(CuratorFramework client, String path,
            CuratorWatcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }

    public static List<String> children(CuratorFramework client, String path) {
        List<String> children = null;
        try {
            children = client.getChildren().forPath(path);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
        return children;
    }

}
