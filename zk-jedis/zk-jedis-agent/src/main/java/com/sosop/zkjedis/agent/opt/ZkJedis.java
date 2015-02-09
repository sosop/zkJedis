package com.sosop.zkjedis.agent.opt;

import static redis.clients.jedis.Protocol.Command.SLAVEOF;
import static redis.clients.jedis.Protocol.Keyword.NO;
import static redis.clients.jedis.Protocol.Keyword.ONE;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Protocol.Command;

public class ZkJedis extends Connection {

    public ZkJedis(String host, int port) {
        super(host, port);
    }

    public ZkJedis(String host, int port, int timeout) {
        super(host, port);
    }

    @Override
    protected Connection sendCommand(Command cmd, String... args) {
        return super.sendCommand(cmd, args);
    }

    @Override
    protected Connection sendCommand(Command cmd, byte[]... args) {
        return super.sendCommand(cmd, args);
    }

    @Override
    public String getStatusCodeReply() {
        return super.getStatusCodeReply();
    }

    public String ping() {
        super.sendCommand(Protocol.Command.PING);
        return super.getStatusCodeReply();
    }

    public String slaveOf(String host, String port) {
        super.sendCommand(SLAVEOF, host, port);
        return super.getStatusCodeReply();
    }

    public String slaveOfNoOne() {
        super.sendCommand(SLAVEOF, NO.raw, ONE.raw);
        return super.getStatusCodeReply();
    }
}
