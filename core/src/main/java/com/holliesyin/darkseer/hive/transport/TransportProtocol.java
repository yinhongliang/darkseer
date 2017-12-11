package com.holliesyin.darkseer.hive.transport;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public interface TransportProtocol {
    void write(String appId, String msgId, String content);
    String read(String appId, String msgId);
}