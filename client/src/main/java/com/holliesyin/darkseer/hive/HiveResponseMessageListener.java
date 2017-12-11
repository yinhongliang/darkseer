package com.holliesyin.darkseer.hive;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveResponseMessageListener extends MessageListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(HiveResponseMessageListener.class);
    private HiveProxyRequest request;
    private HiveProxyResponse response;
    private Lock lock = new ReentrantLock();
    private Condition done = lock.newCondition();

    public HiveResponseMessageListener(HiveProxyRequest request) {
        this.request = request;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = null;
        try {
            body = new String(message.getBody(), HiveProxyConsts.UTF8);
            HiveProxyResponse tmp = new Gson().fromJson(body, HiveProxyResponse.class);
            if (tmp != null && StringUtils.equals(tmp.getMsgId(), request.getMsgId())) {
                LOG.info("get response from channel,response:{}", tmp);
                response = tmp;
                lock.lock();
                try {
                    done.signal();
                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            LOG.error("[hive.proxy] listen message error,body:{}", body, e);
        }
    }

    public HiveProxyResponse getResponse() {
        return response;
    }

    public Lock getLock() {
        return lock;
    }

    public Condition getDone() {
        return done;
    }
}