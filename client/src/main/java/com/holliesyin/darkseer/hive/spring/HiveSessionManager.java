package com.holliesyin.darkseer.hive.spring;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveSessionManager {
    private final static ConcurrentMap<Class, MapperProxy> MAP = new ConcurrentHashMap<Class, MapperProxy>();

    private HiveClientConfig hiveClientConfig;

    public HiveSessionManager(HiveClientConfig hiveClientConfig) {
        this.hiveClientConfig = hiveClientConfig;
    }

    public <T> MapperProxy<T> getSession(Class<T> mapperInterface) {
        MapperProxy proxy = MAP.get(mapperInterface);

        if (proxy == null) {
            proxy = new MapperProxy<T>(hiveClientConfig);
        }
        MAP.put(mapperInterface, proxy);
        return proxy;
    }
}