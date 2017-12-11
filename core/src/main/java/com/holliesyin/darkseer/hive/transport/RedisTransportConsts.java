package com.holliesyin.darkseer.hive.transport;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public interface RedisTransportConsts {
    /**
     * 每个数据块大小限制为10M
     */
    int REDIS_DATA_CHUNK_SIZE = 10 * 1024 * 1024;

    /**
     * 结果集默认保存key
     */
    String HIVE_MAPPER_RESULT_KEY_FORMAT = "HIVE:MAPPER:RESULT:%s:%s";
    String META_DATA = "metaData";
}
