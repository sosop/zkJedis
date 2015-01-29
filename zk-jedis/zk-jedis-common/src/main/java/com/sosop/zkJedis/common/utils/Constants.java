package com.sosop.zkJedis.common.utils;


public class Constants {

    public static abstract class ZK {
        public static final String NAMESPACE = "redis-clusters";

        public static final String CLUSTERS = "/clusters";

        public static final String SLAVES = "/slaves";

        public static final Integer CONNECTION_TIMEOUT_MS = 5000;

        public static final Integer SESSION_TIMEOUT_MS = 10000;
    }

    public static final String NULL_STRING = "";
}
