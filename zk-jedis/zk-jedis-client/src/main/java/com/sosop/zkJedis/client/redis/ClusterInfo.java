package com.sosop.zkJedis.client.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.sosop.zkJedis.client.zk.ZkAction;
import com.sosop.zkJedis.common.utils.FileUtil;
import com.sosop.zkJedis.common.utils.PropsUtil;

public class ClusterInfo {

    private Map<String, ShardedCluster> clusters;

    private JedisPoolConfig config;

    private Map<String, JedisPoolConfig> configs;

    private String defaultName;


    public ClusterInfo(JedisPoolConfig config) {
        this.config = config;
    }

    public ClusterInfo(Map<String, JedisPoolConfig> configs) {
        this.configs = configs;
    }

    public ClusterInfo(String defaultName, JedisPoolConfig config) {
        this.config = config;
        this.defaultName = defaultName;

    }

    public ClusterInfo(String defaultName, Map<String, JedisPoolConfig> configs) {
        this.configs = configs;
        this.defaultName = defaultName;
    }

    public void init() {
        new ZkAction(PropsUtil.properties(FileUtil.getConfigFile("config.properties"))).start(this);
        clusters = new HashMap<>();
    }

    public void rebuildCluster(String clusterName, List<String> servers) {
        ShardedCluster shard = clusters.get(clusterName);
        if (null != shard) {
            shard.checkNodes(servers);
            shard.setPool(buildPool(clusterName, servers));
            clusters.put(clusterName, shard);
        } else {
            clusters.put(clusterName,
                    new ShardedCluster(clusterName, servers, buildPool(clusterName, servers)));
        }
    }

    public ShardedJedisPool buildPool(String clusterName, List<String> servers) {
        List<JedisShardInfo> shards = new ArrayList<>();
        for (String s : servers) {
            String[] hap = s.split(":");
            shards.add(new JedisShardInfo(hap[0], hap[1]));
        }
        if (null != config) {
            return new ShardedJedisPool(config, shards);
        } else {
            return new ShardedJedisPool(configs.get(clusterName), shards);
        }

    }

    public ShardedJedis redis() {
        return clusters.get(defaultName).getPool().getResource();
    }

    public ShardedJedis redis(String clusterName) {
        return clusters.get(clusterName).getPool().getResource();
    }

    public void retrieve(ShardedJedis redis) {
        clusters.get(defaultName).getPool().returnResource(redis);
    }

    public void retrieve(String clusterName, ShardedJedis redis) {
        clusters.get(clusterName).getPool().returnResource(redis);
    }

    public void retrieveBroken(ShardedJedis redis) {
        clusters.get(defaultName).getPool().returnBrokenResource(redis);;
    }

    public void retrieveBroken(String clusterName, ShardedJedis redis) {
        clusters.get(clusterName).getPool().returnBrokenResource(redis);
    }

    public Map<String, ShardedCluster> getClusters() {
        return clusters;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, ShardedCluster> c : clusters.entrySet()) {
            sb.append(c.getKey()).append("  ").append(c.getValue());
        }
        return sb.toString();
    }


}
