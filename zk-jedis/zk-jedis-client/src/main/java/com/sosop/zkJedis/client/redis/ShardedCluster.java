package com.sosop.zkJedis.client.redis;

import java.util.List;

import redis.clients.jedis.ShardedJedisPool;

public class ShardedCluster {
    private String name;

    private ShardedJedisPool pool;

    private List<String> nodes;

    public ShardedCluster() {}

    public ShardedCluster(String name, List<String> nodes, ShardedJedisPool pool) {
        this.name = name;
        this.pool = pool;
        this.nodes = nodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public ShardedJedisPool getPool() {
        return pool;
    }

    public void setPool(ShardedJedisPool pool) {
        this.pool = pool;
    }

    public void checkNodes(List<String> servers) {
        int size = 0;
        if ((size = this.nodes.size()) == servers.size()) {
            for (int i = 0; i < size; i++) {
                if (!nodes.get(i).equals(servers.get(i))) {
                    nodes.set(i, servers.get(i));
                }
            }
        } else {
            this.nodes = servers;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.name);
        for (String s : nodes) {
            sb.append("  ").append(s);
        }
        return sb.toString();
    }


}
