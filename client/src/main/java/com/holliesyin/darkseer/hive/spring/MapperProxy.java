package com.holliesyin.darkseer.hive.spring;

import com.holliesyin.darkseer.hive.HiveProxyClient;
import com.holliesyin.darkseer.hive.annotation.Insert;
import com.holliesyin.darkseer.hive.annotation.Param;
import com.holliesyin.darkseer.hive.annotation.Select;
import com.holliesyin.darkseer.hive.exception.HiveException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private final static Logger LOG = LoggerFactory.getLogger(MapperProxy.class);

    private static final long serialVersionUID = -6424540398559729838L;
    private String appId;
    private RedisMessageListenerContainer container;
    private RedisTemplate redis;
    private long timeout;

    public MapperProxy(HiveClientConfig config) {
        this.appId = config.getAppId();
        this.container = config.getContainer();
        this.redis = config.getRedis();
        this.timeout = config.getTimeout();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Annotation annotation = method.getDeclaredAnnotations()[0];
        if (annotation instanceof Select) {
            String sql = instanceSqlTemplate(method, args, method.getAnnotation(Select.class).value());
            return select(method, sql);
        }
        if (annotation instanceof Insert) {
            String sql = instanceSqlTemplate(method, args, method.getAnnotation(Insert.class).value());
            insert(sql);
            return null;
        }
        throw new HiveException("Not supported Hive method other than Select,Insert");
    }

    private void insert(String sql) {
        doInsert(sql);
    }

    private void doInsert(String sql) {
        LOG.info("[HiveProxy.insert] sql:{}", sql);
        HiveProxyClient.sendRequest(appId, container, redis, sql, timeout);
    }

    private Object select(Method method, String sql) throws Exception {
        if(method.getGenericReturnType() instanceof Class){
            Class returnClazz = method.getReturnType();
            Object tmp = doSelect(returnClazz, sql);
            if(tmp == null){
                return null;
            }
            List tmpList = (List)tmp;
            if(tmpList.isEmpty()){
                return null;
            }else{
                return tmpList.get(0);
            }
        }else{
            Type actualReturnType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            Class returnClazz = Class.forName(actualReturnType.getTypeName());
            return doSelect(returnClazz, sql);
        }
    }

    private String instanceSqlTemplate(Method method, Object[] args, String sql) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Annotation[] annotations = parameterAnnotations[i];
                if (annotations == null || annotations.length != 1) {
                    throw new HiveException("参数名未指定");
                }

                Param param = null;
                if (annotations[0] instanceof Param) {
                    param = (Param) annotations[0];
                }

                String paramName = param.value();
                if (StringUtils.isBlank(paramName)) {
                    throw new HiveException("参数名为空");
                }
                //替换#{}符号
                sql = StringUtils.replace(sql, "#{" + paramName + "}", "'" + arg.toString() + "'");
                //替换${}符号
                sql = StringUtils.replace(sql, "${" + paramName + "}", arg.toString());
            }
        }
        return sql;
    }

    private Object doSelect(Class clazz, String sql) throws Exception {
        LOG.debug("[HiveProxy.orm] Find Return Type class:{}", clazz);
        String hiveResultStr = HiveProxyClient.sendRequest(appId, container, redis, sql, timeout);
        return MapperHelper.rowsToList(clazz, hiveResultStr);
    }
}