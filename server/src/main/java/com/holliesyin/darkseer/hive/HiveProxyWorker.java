package com.holliesyin.darkseer.hive;

import java.util.concurrent.Callable;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxyWorker<T> implements Callable<T>{

    private HiveProxyRequest request;

    private HiveProxy proxy;

    public HiveProxyWorker(HiveProxy proxy, HiveProxyRequest request) {
        this.proxy = proxy;
        this.request = request;
    }

    @Override
    public T call() {
        proxy.process(request);
        return null;
    }
}