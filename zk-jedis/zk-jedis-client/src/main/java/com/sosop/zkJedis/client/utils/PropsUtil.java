package com.sosop.zkJedis.client.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sosop.zkJedis.client.zk.ZkAction;

public class PropsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ZkAction.class);

    public static Properties properties(File file) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(file));
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return props;
    }
}
