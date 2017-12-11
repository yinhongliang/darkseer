package com.holliesyin.darkseer.hive.listener;

import com.google.gson.Gson;
import com.holliesyin.darkseer.hive.HiveProxy;
import com.holliesyin.darkseer.hive.HiveProxyConsts;
import com.holliesyin.darkseer.hive.HiveProxyRequest;
import com.holliesyin.darkseer.hive.HiveProxyWorker;
import com.holliesyin.darkseer.hive.exception.HiveException;
import com.holliesyin.darkseer.hive.utils.CallableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxyListener implements MessageListener {

    private final static Logger LOG = LoggerFactory.getLogger(HiveProxyListener.class);

    private HiveProxy proxy;

    private Boolean hiveClient;

    public void setHiveClient(Boolean hiveClient) {
        this.hiveClient = hiveClient;
    }

    public void setProxy(HiveProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            if (!hiveClient) {
                LOG.error("[HiveProxyListener] Hive client has not installed,return.");
                return;
            }
            String channel = new String(message.getChannel(), HiveProxyConsts.UTF8);
            String body = new String(message.getBody(), HiveProxyConsts.UTF8);
            HiveProxyRequest request = new Gson().fromJson(body, HiveProxyRequest.class);

            check(channel, request);
            CallableUtils.runWithTimeout(new HiveProxyWorker<Object>(proxy, request), 10L, TimeUnit.HOURS);
            LOG.info("[hive.proxy] add to worker list success.request:{}", request);
        } catch (Exception e) {
            LOG.error("[hive.proxy] add to worker list fail.", e);
        }
    }

    private void check(String channel, HiveProxyRequest request) {
        if (request == null) {
            throw new HiveException("Hive代理请求为空");
        }

        if (StringUtils.isBlank(request.getAppId()) || StringUtils.isBlank(request.getMsgId())) {
            throw new HiveException("Hive代理请求中AppId或MsgId为空");
        }

        if (!StringUtils.equals(channel, HiveProxyConsts.UP_CHANNEL + ":" + request.getAppId())) {
            throw new HiveException("Hive代理请求中AppId与分发的AppId不匹配");
        }
    }
}