package com.holliesyin.darkseer.hive.spring;

import java.lang.reflect.Proxy;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class MapperProxyFactory {
    public MapperProxyFactory() {
    }

    @SuppressWarnings("unchecked")
    protected static <T> T newInstance(Class<T> mapperInterface, MapperProxy mapperProxy) {
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

    public static <T> T newInstance(HiveSessionManager session, Class<T> mapperInterface) {
        final MapperProxy mapperProxy = session.getSession(mapperInterface);
        return newInstance(mapperInterface, mapperProxy);
    }
}