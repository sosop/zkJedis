package com.sosop.zkJedis.client.redis;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ShardedJedis;

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
    public void testJedis() throws InterruptedException {
        shard.init();
        TimeUnit.SECONDS.sleep(10);
        System.out.println(shard);

        ShardedJedis jedis = shard.redis();
        System.out.println(jedis.rpush("l", "1", "2"));
        System.out.println(jedis.get("b"));
        shard.retrieve(jedis);

    }
}
