package com.holliesyin.darkseer.hive.spring;

import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveSessionSupport implements InitializingBean {

    private HiveSessionManager session;

    public HiveSessionSupport() {
    }

    public HiveSessionManager getSession() {
        return session;
    }

    public void setSession(HiveSessionManager session) {
        this.session = session;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}