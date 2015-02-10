package com.sosop.zkJedis.client.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.sosop.zkJedis.client.zk.ZkAction;
import com.sosop.zkJedis.common.utils.ClusterType;
import com.sosop.zkJedis.common.utils.Constants;
import com.sosop.zkJedis.common.utils.FileUtil;
import com.sosop.zkJedis.common.utils.PropsUtil;
import com.sosop.zkJedis.common.utils.StringUtil;

public class ClusterInfo {

    private Map<String, ShardedCluster> clusters;

    private JedisPoolConfig config;

    private Map<String, JedisPoolConfig> configs;

    private Map<String, JedisCluster> proClusters;

    private String defaultName;

    public ClusterInfo() {}

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
        Properties props = PropsUtil.properties(FileUtil.getConfigFile("config.properties"));
        String style = props.getProperty("clutster.style");
        if (style.equalsIgnoreCase(ClusterType.SHARD.name())) {
            new ZkAction(props).start(this);
            clusters = new HashMap<>();
        } else if (style.equalsIgnoreCase(ClusterType.PROTOTYPE.name())) {
            proClusters = new HashMap<>();
            String[] names = props.getProperty("cluster.names").split(",");
            for (String name : names) {
                if (StringUtil.notNull(name)) {
                    String[] servers =
                            props.getProperty(StringUtil.append("cluster.", name, ".servers"))
                                    .split(",");
                    Set<HostAndPort> nodes = new HashSet<>();
                    for (String server : servers) {
                        if (StringUtil.notNull(server)) {
                            String[] hap = server.split(":");
                            nodes.add(new HostAndPort(hap[0], Integer.parseInt(hap[1])));
                        }
                    }
                    proClusters.put(name, new JedisCluster(nodes));
                    defaultName = name;
                }
            }
            this.defaultName = props.getProperty("default", defaultName);
        } else if (style.equalsIgnoreCase(ClusterType.SENTINEL.name())) {

        }
    }

    @Deprecated
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

    public void rebuildCluster(String clusterName, String node, int index) {
        ShardedCluster shard = clusters.get(clusterName);
        List<String> nodes = null;
        if (null != shard) {
            nodes = shard.getNodes();
            if (index == -1) {
                nodes.remove(node);
            } else if (index >= nodes.size()) {
                for (int i = nodes.size(); i <= index; i++) {
                    nodes.add(Constants.NULL_STRING);
                }
                nodes.set(index, node);
            } else {
                int ind = -1;
                if ((ind = nodes.indexOf(Constants.NULL_STRING)) >= 0) {
                    nodes.set(ind, node);
                } else {
                    nodes.add(index, node);
                }
            }
            shard.setPool(buildPool(clusterName, nodes));
            clusters.put(clusterName, shard);
        } else {
            nodes = new ArrayList<>();
            if (index > 0) {
                for (int i = 0; i <= index; i++) {
                    nodes.add(Constants.NULL_STRING);
                }
                nodes.set(index, node);
            } else {
                nodes.add(index, node);
            }
            clusters.put(clusterName,
                    new ShardedCluster(clusterName, nodes, buildPool(clusterName, nodes)));
        }
    }

    public ShardedJedisPool buildPool(String clusterName, List<String> servers) {
        List<JedisShardInfo> shards = new ArrayList<>();
        for (String s : servers) {
            if (StringUtil.notNull(s)) {
                String[] hap = s.split(":");
                shards.add(new JedisShardInfo(hap[0], hap[1]));
            }
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

    public JedisCluster cluster() {
        return proClusters.get(defaultName);
    }

    public JedisCluster cluster(String clusterName) {
        return proClusters.get(clusterName);
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
