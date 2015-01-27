package com.sosop.zkJedis.client.zk;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sosop.zkJedis.common.utils.CreateClient;

public class CreateClientTest {

    private String connectionsRight;
    private String connectionsWrong;

    @Before
    public void init() {
        connectionsRight = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        connectionsWrong = "192.168.1.1:8000";
    }

    @Test
    public void testCreate() throws InterruptedException {
        CuratorFramework clientRight = CreateClient.create(connectionsRight);
        clientRight.start();
        Assert.assertTrue(clientRight.blockUntilConnected(1, TimeUnit.SECONDS));
        clientRight.close();

        CuratorFramework clientWrong = CreateClient.create(connectionsWrong);
        Assert.assertFalse(clientWrong.blockUntilConnected(1, TimeUnit.SECONDS));
        clientWrong.start();
    }
}
