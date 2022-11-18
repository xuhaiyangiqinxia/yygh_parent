package com.atguigu.yygh.hosp.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequestHelper {

    /**
     * 获取时间戳
     * @return
     */
    public static long getTimestamp() {
        return new Date().getTime();
    }

    public static Map<String, Object> switchMap(Map<String, String[]> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
            resultMap.put(param.getKey(), param.getValue()[0]);
        }
        return resultMap;
    }
}
