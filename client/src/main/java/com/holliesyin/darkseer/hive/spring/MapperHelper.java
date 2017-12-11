package com.holliesyin.darkseer.hive.spring;

import com.holliesyin.darkseer.hive.utils.HiveUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class MapperHelper {
    private final static Logger LOG = LoggerFactory.getLogger(MapperHelper.class);

    public static Object rowToObj(Class clazz, Field[] fields, Map<String, Integer> rowMeta, String[] row) throws Exception {
        Object obj = clazz.newInstance();
        for (Field field : fields) {
            //rowMeta中的key为hive返回的列名，都是小写
            String fieldName = field.getName().toLowerCase();
            if (!rowMeta.containsKey(fieldName)) {
                continue;
            }
            String cell = row[rowMeta.get(fieldName)];

            LOG.debug("[HiveProxy.orm] try to set row cell to field,cell:{},field name:{},field type:{}", cell, field.getName(), field.getType());
            if (StringUtils.isBlank(cell) || "NULL".equals(cell)) {
                continue;
            }
            Class fieldType = field.getType();

            if (String.class.equals(fieldType)) {
                field.set(obj, cell);
                continue;
            } else if (BigDecimal.class.equals(fieldType)) {
                field.set(obj, new BigDecimal(cell));
                continue;
            } else if (Integer.class.equals(fieldType)) {
                field.set(obj, Integer.valueOf(cell));
                continue;
            } else if (int.class.equals(fieldType)) {
                field.setInt(obj, Integer.valueOf(cell));
                continue;
            } else if (Long.class.equals(fieldType)) {
                field.set(obj, Long.valueOf(cell));
                continue;
            } else if (long.class.equals(fieldType)) {
                field.setLong(obj, Long.valueOf(cell));
                continue;
            } else if (Boolean.class.equals(fieldType)) {
                field.set(obj, Boolean.valueOf(cell));
                continue;
            } else if (boolean.class.equals(fieldType)) {
                field.setBoolean(obj, Boolean.valueOf(cell));
                continue;
            } else if (Date.class.equals(fieldType)) {
                if(StringUtils.contains(cell,":")){
                    field.set(obj, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cell));
                }else{
                    field.set(obj, new SimpleDateFormat("yyyyMMddHHmmss").parse(cell));
                }
                continue;
            } else {
                field.set(obj, cell);
                continue;
            }
        }
        LOG.debug("[HiveProxy.orm] trans row to obj,row:{},obj:{}", Arrays.asList(row), obj);
        return obj;
    }

    static Object rowsToList(Class clazz, String hiveResultRows) throws Exception {
        //分拆返回值，为列名colNames和数据记录rows
        List<String[]> hiveResult = HiveUtils.parseHiveResult(hiveResultRows);
        String[] colNames = hiveResult.remove(0);
        List<String[]> rows = hiveResult;
        LOG.debug("[HiveProxy.orm] hive result column names:{},trans to type:{}", Arrays.asList(colNames), clazz);

        //为列名生成索引，索引值为列编号
        Map<String, Integer> rowMeta = new HashMap<>();
        for (int i = 0; i < colNames.length; i++) {
            String colName = colNames[i];
            rowMeta.put(colName, i);
        }
        LOG.debug("[HiveProxy.orm] row meta:{}", rowMeta);

        //获取返回类型的fields
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
        }

        //转化行为对象
        int size = rows.size();
        List targetObjects = new ArrayList(size);
        for (String[] row : rows) {
            Object obj = rowToObj(clazz, fields, rowMeta, row);
            targetObjects.add(obj);
        }

        return targetObjects;
    }
}