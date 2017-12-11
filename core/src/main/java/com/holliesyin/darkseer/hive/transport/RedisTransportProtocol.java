package com.holliesyin.darkseer.hive.transport;

import com.google.gson.Gson;
import com.holliesyin.darkseer.hive.exception.HiveException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static com.holliesyin.darkseer.hive.transport.RedisTransportConsts.META_DATA;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class RedisTransportProtocol implements TransportProtocol{

    private static final Logger LOG = LoggerFactory.getLogger(RedisTransportProtocol.class);

    private RedisTemplate redisTemplate;

    public RedisTransportProtocol(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void write(String appId, String msgId, String content) {
        long start = System.currentTimeMillis();
        String redisKey = String.format(RedisTransportConsts.HIVE_MAPPER_RESULT_KEY_FORMAT, appId, msgId);
        int length = StringUtils.isBlank(content) ? 0 : content.length();
        int chunkCount = (length + RedisTransportConsts.REDIS_DATA_CHUNK_SIZE - 1) / RedisTransportConsts.REDIS_DATA_CHUNK_SIZE;
        ResponseMetaData metaData = new ResponseMetaData(length, RedisTransportConsts.REDIS_DATA_CHUNK_SIZE, chunkCount);
        try {
            //将数据分块处理
            redisTemplate.boundHashOps(redisKey).put(META_DATA, new Gson().toJson(metaData));
            // TODO 设置超时时间5分钟
            redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);
            LOG.info("write meta data in redis success,meta:{},key:{}.", metaData, redisKey);
            //TODO 边界情况待考虑
            for (int i = 1; i <= chunkCount; i++) {
                String chunk = StringUtils.substring(content, RedisTransportConsts.REDIS_DATA_CHUNK_SIZE * (i - 1), RedisTransportConsts.REDIS_DATA_CHUNK_SIZE * i);
                //向redis中按顺序写入分块
                redisTemplate.boundHashOps(redisKey).put(String.valueOf(i), chunk);
            }
            LOG.info("write data in redis success,meta:{},key:{},cost {}ms.", metaData, redisKey, System.currentTimeMillis() - start);
        } catch (Exception e) {
            LOG.error("write data in redis fail,meta:{},key:{},cost {}ms.", metaData, redisKey, System.currentTimeMillis() - start, e);
            throw new HiveException("响应数据写入失败");
        }
    }

    @Override
    public String read(String appId, String msgId) {
        long start = System.currentTimeMillis();
        String redisKey = String.format(RedisTransportConsts.HIVE_MAPPER_RESULT_KEY_FORMAT, appId, msgId);
        try {
            ResponseMetaData metaData = new Gson().fromJson(redisTemplate.boundHashOps(redisKey).get(META_DATA).toString(), ResponseMetaData.class);
            LOG.info("read meta data from redis success,meta:{},key:{}.", metaData,redisKey);
            //查看分块列表
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= metaData.getChunkCount(); i++) {
                //从redis中读取分块
                String chunk = (String) redisTemplate.boundHashOps(redisKey).get(String.valueOf(i));
                if (StringUtils.isNotBlank(chunk)) {
                    //组装分块
                    sb.append(chunk);
                }
            }
            LOG.info("read data from redis success,key:{},cost {}ms.", redisKey, System.currentTimeMillis() - start);
            return sb.toString();
        } catch (Exception e) {
            LOG.error("read data from redis fail,key:{},cost {}ms.", redisKey, System.currentTimeMillis() - start, e);
            throw new HiveException("响应数据读取失败");
        } finally {
            redisTemplate.delete(redisKey);
        }
    }
}