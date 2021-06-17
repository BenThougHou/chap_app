package com.yiliao.util.cache;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yiliao.util.DateUtils;
import com.yiliao.util.RedisUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
public class AspectJ {
    @Autowired
    public RedisUtil redisUtil;

    public static String getCacheKey(String cacheKey,
                                     Object[] arguments) {
        StringBuffer sb = new StringBuffer(cacheKey);
        if ((arguments != null) && (arguments.length != 0)) {
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] instanceof Date) {
                    sb.append(".").append(format((Date) arguments[i]));
                } else if (arguments[i] instanceof Map) {
                    SortedMap sortedMap = new TreeMap((Map) arguments[i]);
                    sb.append(".").append(sortedMap);
                } else {
                    sb.append(".").append(arguments[i]);
                }
            }
        }
        return sb.toString();
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        String s = DateUtils.getStringDate();
        if (s == null) {
            return  DateUtils.dateToString(date);
        }
        return s;
    }

    public static String getArgumentsValue(String cacheKey, String[] parameterNames, Object[] arguments) {
        StringBuffer sb = new StringBuffer();
        List pList = Arrays.asList(parameterNames);
        if ((parameterNames != null) && (parameterNames.length != 0)) {
            int x;
            String[] keys = cacheKey.substring(1, cacheKey.length() - 1).split("&");
            for (int i = 0; i < keys.length; i++) {
                if (pList.contains(keys[i].split("\\.")[0])) {
                    x = pList.indexOf(keys[i].split("\\.")[0]);
                    if (arguments[x] instanceof Date) {
                        sb.append(format((Date) arguments[x]));
                    } else if (arguments[x] instanceof Map) {
                        SortedMap sortedMap = new TreeMap((Map) arguments[x]);
                        if (keys[i].split("\\.").length > 1) {
                            sb.append(String.valueOf(sortedMap.get(keys[i].split("\\.")[1])));
                        } else {
                            sb.append(String.valueOf(sortedMap));
                        }

                    } else if (arguments[x] instanceof HttpServletRequest) {
                        HttpServletRequest request = (HttpServletRequest) arguments[x];
                        String ip = request.getHeader("x-forwarded-for");
                		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                			ip = request.getHeader("Proxy-Client-IP");
                		}
                		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                			ip = request.getHeader("WL-Proxy-Client-IP");
                		}
                		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                			ip = request.getHeader("HTTP_CLIENT_IP");
                		}
                		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                		}
                		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                			ip = request.getRemoteAddr();
                		}
                        sb.append(ip);
                    } else  {
                        sb.append(arguments[x]);
                    }
                } else {
                    sb.append(keys[i]);
                }
            }
        }else {
        	String substring = cacheKey.substring(1, cacheKey.length() - 1);
        	sb.append(substring);
        }
        return sb.toString();
    }

    public static int StringtoInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Map getArgumentsValueToMap(String key, String[] parameterNames, Object[] arguments) {
        Map map = new HashMap();
        List pList = Arrays.asList(parameterNames);
        if ((parameterNames != null) && (parameterNames.length != 0)) {
            int x;
            String[] keys = key.substring(1, key.length() - 1).split("&");
            for (int i = 0; i < keys.length; i++) {
                if (pList.contains(keys[i].split("\\.")[0])) {
                    x = pList.indexOf(keys[i].split("\\.")[0]);
                    if (arguments[x] instanceof Map) {
                        SortedMap sortedMap = new TreeMap((Map) arguments[x]);
                        map.put(keys[i].split("\\.")[1], sortedMap.get(keys[i].split("\\.")[1]));
                    }
                }
            }
        }
        return map;
    }

    public static Map getRestultValue(String key, Map result) {
        if (result == null) return null;
        Map map = new HashMap();
        String[] keys = key.substring(1, key.length() - 1).split("&");
        for (int i = 0; i < keys.length; i++) {
            if (result.containsKey(keys[i].split("\\.")[1])) {
                map.put(keys[i].split("\\.")[1], result.get(keys[i].split("\\.")[1]));
            }
        }
        return map;
    }

    public static Map getResMsgRestultValue(String key, Map result) {
        if (Integer.parseInt(String.valueOf(result.get("code"))) == 0) {
            Map map = (Map) result.get("data");
            if (map != null) {
                Map res = new HashMap();
                String[] keys = key.substring(1, key.length() - 1).split("&");
                for (int i = 0; i < keys.length; i++) {
                    if (map.containsKey(keys[i].split("\\.")[1])) {
                        res.put(keys[i].split("\\.")[1], map.get(keys[i].split("\\.")[1]));
                    }
                }
                return res;
            }
        }
        return null;
    }
}
