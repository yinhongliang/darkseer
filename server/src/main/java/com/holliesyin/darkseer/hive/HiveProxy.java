package com.holliesyin.darkseer.hive;

import com.google.gson.Gson;
import com.holliesyin.darkseer.hive.transport.RedisTransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveProxy {
    private final static Logger LOG = LoggerFactory.getLogger(HiveProxy.class);

    private RedisTemplate template;

    public HiveProxy(RedisTemplate template) {
        this.template = template;
    }

    public void process(HiveProxyRequest request) {
        HiveProxyResponse response = new HiveProxyResponse();
        response.setAppId(request.getAppId());
        response.setMsgId(request.getMsgId());

        String result;
        try {
            result = ExecuteShellCommand.executeHiveSql(request.getContent());
            new RedisTransportProtocol(template).write(request.getAppId(), request.getMsgId(), result);
            response.setStatusCode(HiveProxyResponse.StatusCode.SUCCESS);
            response.setResult("");
            LOG.info("[HiveClientProxy.process] success,request:{},response:{}", request, response);
        } catch (Exception e) {
            response.setStatusCode(HiveProxyResponse.StatusCode.FAIL);
            response.setResult(e.getMessage());
            LOG.error("[HiveClientProxy.process] fail,request:{},response:{}", request, response, e);
        } finally {
            String channel = HiveProxyConsts.DOWN_CHANNEL + ":" + request.getAppId();
            try {
                template.convertAndSend(channel, new Gson().toJson(response));
                LOG.info("[HiveClientProxy.process] response success,request:{},response:{}", request, response);
            } catch (Exception e) {
                LOG.error("[HiveClientProxy.process] response fail.",e);
            }
        }
    }
}