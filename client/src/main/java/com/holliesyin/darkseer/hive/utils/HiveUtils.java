package com.holliesyin.darkseer.hive.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hollies Yin on 2017-12-11.
 */
public class HiveUtils {
    private final static String LINE_SEPARATOR = "\n";
    private final static String FIELD_SEPARATOR = "\t";
    private final static String NULL = "NULL";

    public static List<String[]> parseHiveResult(String ret) {
        List<String[]> list = new ArrayList<String[]>();
        String[] lines = ret.split(LINE_SEPARATOR);
        if (lines == null || lines.length <= 0) {
            return list;
        }

        for (String line : lines) {
            String[] fields = line.split(FIELD_SEPARATOR);
            if (fields == null || fields.length <= 0) {
                continue;
            }
            list.add(fields);
        }
        return list;
    }
}