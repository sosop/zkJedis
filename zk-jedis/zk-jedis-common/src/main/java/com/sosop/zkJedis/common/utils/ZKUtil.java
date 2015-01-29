package com.sosop.zkJedis.common.utils;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ZKUtil.class);

    public static void create(CuratorFramework client, String path, CreateMode mode, byte... data) {
        try {
            if (!exist(client, path)) {
                if (data != null && data.length > 0) {
                    client.create().withMode(mode).forPath(path, data);
                } else {
                    client.create().withMode(mode).forPath(path);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }

    public static void delete(CuratorFramework client, String path) {
        try {
            if (exist(client, path)) {
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

    public static void addPathWatcher(CuratorFramework client, String path, CuratorListener watcher) {
        try {

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

    public static String getData(CuratorFramework client, String path) {
        String data = null;
        try {
            if (exist(client, path)) {
                data = new String(client.getData().forPath(path));
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
        return data;
    }

    public static void setData(CuratorFramework client, String path, byte[] data) {
        try {
            if (exist(client, path)) {
                client.setData().forPath(path, data);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
    }

    public static boolean exist(CuratorFramework client, String path) {
        boolean isExist = false;
        try {
            isExist = client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
        return isExist;
    }

    public static boolean notExist(CuratorFramework client, String path) {
        boolean isExist = true;
        try {
            isExist = client.checkExists().forPath(path) == null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e.getCause());
        }
        return isExist;
    }
}
