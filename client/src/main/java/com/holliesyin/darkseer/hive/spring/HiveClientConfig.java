package com.holliesyin.darkseer.hive.spring;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveClientConfig {
    private String appId;
    private RedisMessageListenerContainer container;
    private RedisTemplate redis;
    private long timeout;

    public HiveClientConfig(String appId, RedisMessageListenerContainer container, RedisTemplate redis, long timeout) {
        this.appId = appId;
        this.container = container;
        this.redis = redis;
        this.timeout = timeout;
    }

    public String getAppId() {
        return appId;
    }

    public RedisMessageListenerContainer getContainer() {
        return container;
    }

    public RedisTemplate getRedis() {
        return redis;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setContainer(RedisMessageListenerContainer container) {
        this.container = container;
    }

    public void setRedis(RedisTemplate redis) {
        this.redis = redis;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
