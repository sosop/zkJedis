package com.sosop.zkJedis.client.redis;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.JedisPoolConfig;

public class ShardClusterTest {

    private JedisPoolConfig config;
    private ClusterInfo shard;

    @Before
    public void init() {
        config = new JedisPoolConfig();
        config.setMaxIdle(10);
        config.setMaxTotal(50);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        shard = new ClusterInfo(config);
    }

    @Test
    public void testJedis() {
        // shard.init();
        // while (true);
    }
}
