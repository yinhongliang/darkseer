package com.holliesyin.darkseer.hive;

import com.holliesyin.darkseer.hive.transport.RedisTransportProtocol;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveTransportCallable implements Callable<HiveProxyResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(HiveTransportCallable.class);

    private RedisMessageListenerContainer container;
    private HiveResponseMessageListener listener;
    private Lock lock;
    private Condition done;
    private RedisTemplate redisTemplate;

    public HiveTransportCallable(final RedisMessageListenerContainer container, final String receiveChannel, final HiveResponseMessageListener listener, final RedisTemplate redisTemplate) {
        this.container = container;
        this.listener = listener;
        this.lock = listener.getLock();
        this.done = listener.getDone();
        this.container.addMessageListener(listener, new ChannelTopic(receiveChannel));
        this.redisTemplate = redisTemplate;
    }

    @Override
    public HiveProxyResponse call() throws Exception {
        lock.lock();
        try {
            while (listener.getResponse() == null) {
                done.await();
            }
            String appId = listener.getResponse().getAppId();
            String msgId = listener.getResponse().getMsgId();
            HiveProxyResponse response = listener.getResponse();
            //从redis读取结果
            response.setResult(StringUtils.defaultString(new RedisTransportProtocol(redisTemplate).read(appId, msgId),""));
            return response;
        } catch (Exception e) {
            LOG.error("read response error.", e);
            HiveProxyResponse response = listener.getResponse();
            response.setStatusCode(HiveProxyResponse.StatusCode.FAIL);
            response.setResult(e.getMessage());
            return response;
        } finally {
            lock.unlock();
            container.removeMessageListener(listener);
        }
    }
}