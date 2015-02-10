package com.sosop.zkJedis.client.redis;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.JedisCluster;
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
    }

    @Test
    public void testJedis() throws InterruptedException {
        shard = new ClusterInfo(config);
        shard.init();
        TimeUnit.SECONDS.sleep(10);
        System.out.println(shard);

        ShardedJedis jedis = shard.redis();
        System.out.println(jedis.rpush("l", "1", "2"));
        System.out.println(jedis.get("b"));
        shard.retrieve(jedis);
    }

    @Test
    public void testCluster() {
        shard = new ClusterInfo();
        shard.init();
        JedisCluster jc = shard.cluster();
        System.out.println(jc.get("ok"));
        jc.set("test", "test");
        System.out.println(jc.get("test"));
    }
}
