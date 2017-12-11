package com.holliesyin.darkseer.hive;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public interface HiveProxyConsts {
    /**
     * 从客户端到服务端通信的channel，定义为上行channel
     */
    String UP_CHANNEL="darkseer:hive:proxy:up";
    /**
     * 从服务端到客户端通信的channel，定义为下行channel
     */
    String DOWN_CHANNEL="darkseer:hive:proxy:down";

    String UTF8 = "UTF8";
}