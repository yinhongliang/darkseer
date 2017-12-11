package com.holliesyin.darkseer.hive;

import com.google.gson.Gson;
import com.holliesyin.darkseer.hive.utils.CallableUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxyClient {
    private final static Logger LOG = LoggerFactory.getLogger(HiveProxyClient.class);

    public static String sendRequest(String appId, RedisMessageListenerContainer container, RedisTemplate template, String content, long timeout) {
        HiveProxyRequest request = new HiveProxyRequest();
        request.setAppId(appId);
        request.setMsgId(RandomStringUtils.randomAlphanumeric(10));
        request.setContent(content);
        HiveProxyResponse response = sendRequest(container, template, request, timeout);
        if (response != null && HiveProxyResponse.StatusCode.SUCCESS.equals(response.getStatusCode())) {
            LOG.info("[hive.proxy] response success.response:{}", response);
            return response.getResult();
        } else {
            LOG.error("[hive.proxy] response fail.response:{}", response);
        }
        return "";
    }

    private static HiveProxyResponse sendRequest(RedisMessageListenerContainer container, RedisTemplate template, HiveProxyRequest request, long timeout) {
        LOG.info("[hive] send request:{}", request);
        String appId = request.getAppId();
        String receiveChannel = new StringBuilder().append(HiveProxyConsts.DOWN_CHANNEL).append(":").append(appId).toString();
        String sendChannel = new StringBuilder().append(HiveProxyConsts.UP_CHANNEL).append(":").append(appId).toString();

        //准备接收响应
        HiveResponseMessageListener listener = new HiveResponseMessageListener(request);

        Callable<HiveProxyResponse> callable = new HiveTransportCallable(container, receiveChannel, listener,template);

        //发送请求

        template.convertAndSend(sendChannel, new Gson().toJson(request));

        //接收响应
        try {
            return CallableUtils.runWithTimeout(callable, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOG.error("[hive.proxy] execute error.request:{}", request, e);
        }

        return null;
    }
}