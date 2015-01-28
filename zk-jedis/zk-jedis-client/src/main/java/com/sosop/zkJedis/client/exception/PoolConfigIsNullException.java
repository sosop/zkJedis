package com.sosop.zkJedis.client.exception;

public class PoolConfigIsNullException extends Exception {

    /**
     * @Fields serialVersionUID : TODO(用一句话描述这个变量表示什么)
     */
    private static final long serialVersionUID = 2L;

    public PoolConfigIsNullException(String msg) {
        super(msg);
    }

}
