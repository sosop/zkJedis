package com.sosop.zkJedis.client.zk;

import org.junit.Before;
import org.junit.Test;

import com.sosop.zkJedis.common.utils.FileUtil;
import com.sosop.zkJedis.common.utils.PropsUtil;

public class ZkActionTest {

    private ZkAction action;

    @Before
    public void init() {
        action = new ZkAction(PropsUtil.properties(FileUtil.getConfigFile("config.properties")));
    }

    @Test
    public void testCreateSecond() throws Exception {
        action.start(null);
        // while (true);
        action.close();
    }
}
