package com.sosop.zkJedis.client;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("127.0.0.1", 8000));
        nodes.add(new HostAndPort("127.0.0.1", 8001));
        nodes.add(new HostAndPort("127.0.0.1", 8002));
        JedisCluster cluster = new JedisCluster(nodes);

        cluster.set("a", "a");
        cluster.set("b", "b");
        cluster.set("c", "c");
        cluster.set("think", "value");
        cluster.close();
    }
}
