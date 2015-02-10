package com.sosop.zkJedis.common.utils;

public enum ClusterType {
    SHARD("shard"), SENTINEL("sentinel"), PROTOTYPE("prototype");

    ClusterType(String value) {}
}
