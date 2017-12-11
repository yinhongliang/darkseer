package com.holliesyin.darkseer.hive.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class MapperFactoryBean<T> extends HiveSessionSupport implements FactoryBean<T> {
    private Class<T> mapperInterface;
    private boolean addToConfig = true;

    public MapperFactoryBean() {
    }

    public MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public boolean isAddToConfig() {
        return addToConfig;
    }

    public void setAddToConfig(boolean addToConfig) {
        this.addToConfig = addToConfig;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() throws Exception {
        return MapperProxyFactory.newInstance(getSession(),mapperInterface);
    }

    @Override
    public Class<T> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}