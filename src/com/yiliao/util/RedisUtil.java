package com.yiliao.util;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redicache 工具类
 *
 */

public class RedisUtil {
	
    public RedisTemplate redisTemplate;

//    @Autowired(required = false)
//    public void setRedisTemplate(RedisTemplate redisTemplate) {
//        RedisSerializer stringSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(stringSerializer);
//        redisTemplate.setValueSerializer(stringSerializer);
//        redisTemplate.setHashKeySerializer(stringSerializer);
//        redisTemplate.setHashValueSerializer(stringSerializer);
//        this.redisTemplate = redisTemplate;
//    }
    
    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }
    public RedisTemplate getRedisTemplate() {
		return redisTemplate;
	}
	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	/**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern+"*");
        if (keys.size() > 0)
            redisTemplate.delete(keys);
    }
    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }
    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public String get(final String key) {
        Object result = null;
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        if(result==null){
            return null;
        }
        return result.toString();
    }
    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 分钟
     * @return
     */
    public boolean set(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.MINUTES);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 毫秒
     * @return
     */
    public boolean setTime(final String key, Object value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 设置Key 过期时间
     * @param key
     * @param expireTime 分钟
     */
    public void  expire(final String key,Long expireTime){
        if(redisTemplate.hasKey(key)){redisTemplate.expire(key, expireTime, TimeUnit.MINUTES);}
    }
    
    /**
	 * 设置key 过期时间
	 * @param key
	 * @param expireTime 毫秒
	 */
	public void expireSecond(final String key, Long expireTime) {
		if (redisTemplate.hasKey(key)) {
			redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
		}
	}

    public  boolean hmset(String key, Map<String, Object> value,Long expireTime) {
        boolean result = false;
        try {
            redisTemplate.opsForHash().putAll(key, value);
            if(expireTime > 0) {
            	expireSecond(key, expireTime);
            }
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public  Map<String,Object> hmget(String key) {
        Map<String,Object> result =null;
        try {
            result=  redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 
     * @param key
     * @return
     */
    public Object getKeyObject(String key) {
    	
    	return redisTemplate.opsForValue().get(key);
    }
    
    
    /**
     * redis 存值
     * @param key
     * @param object
     */
    public void lset(String key,Object object) {
    	redisTemplate.opsForList().rightPushAll(key, object);
    }
    
    /**
     * redis取值
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<Object> lget(String key,int start,int end){
    	
    	return redisTemplate.opsForList().range(key, start, end);
    }
}
