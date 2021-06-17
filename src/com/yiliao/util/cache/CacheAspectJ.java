package com.yiliao.util.cache;


import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

//@Slf4j
@Aspect
@Component
@Order(2)
public class CacheAspectJ extends AspectJ {
	
	private Logger log = LoggerFactory.getLogger(getClass());

    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    @Pointcut("@annotation(com.yiliao.util.cache.Cache)")
    public void methodCachePointcut() {
    }


    

    @Around("methodCachePointcut()")
    public Object methodCacheHold(ProceedingJoinPoint pjp) throws Throwable {
    	 // 拦截的实体类  
        Object target = pjp.getTarget();  
        // 拦截的方法名称  
        String methodName = pjp.getSignature().getName();  
        // 拦截的方法参数  
        Object[] argsa = pjp.getArgs();  
        // 拦截的放参数类型  
        Class[] parameterTypes = ((MethodSignature) pjp.getSignature()).getMethod().getParameterTypes();  
        
        Method method = target.getClass().getMethod(methodName, parameterTypes);  
        
        Object[] arguments = pjp.getArgs();
        
        Cache cache = method.getAnnotation(Cache.class);  
        
      //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();  
        String [] parameterNames=u.getParameterNames(method);
        
        Class<?> T = method.getReturnType();
        
        Object result = null;
        String cacheKey = null;
        boolean setArguments = true;
        if (null != cache) {
            try {
                cacheKey = cache.cacheKey();
                setArguments = cache.setArguments();
                long expire = cache.expire();
                
                //如果page=1 则返回 不尽兴缓存
                if(parameterNames!=null&&parameterNames.length>0) {
                	int i=0;
                	for (String parameterName : parameterNames) {
                		 Object object =null;
                		if(arguments[i] instanceof Map) {
            				 Map map=(Map)arguments[i];
            				  object = map.get("page");
                		}else {
                			if((parameterName.equals("page"))){
                			  object=arguments[i];
                			}
                		}
                		if(object!=null&&!object.toString().equals("1")) {
   					 		return result = pjp.proceed();
        				}
                		i++;
					}
                }
                if (cacheKey == null || cacheKey.equals("")) {
                    String className = pjp.getTarget().getClass().getSimpleName();
                    cacheKey = className + "." + methodName;
                }
                //如果用了表达式的则单独处理
                if (cacheKey.startsWith("#") && cacheKey.endsWith("#")) {
                   
                    cacheKey = getArgumentsValue(cacheKey, parameterNames, arguments);
                }
                if (setArguments) {
                    cacheKey = getCacheKey(cacheKey, arguments);
                }
                
                Object value = redisUtil.get(cacheKey);
                if (value == null) {
                    result = pjp.proceed();
                    if (result != null) {
                        log.info("cached,cache not null,key:" + cacheKey);
                        /*if (T == Map.class || T == List.class || T == PageInfo.class || result instanceof BaseBean) {
                            if (expire == 0l) {
                                redisUtil.set(cacheKey, JSON.toJSONString(result));
                            } else {
                                redisUtil.set(cacheKey, JSON.toJSONString(result), expire);
                            }
                        } else {
                            if (expire == 0l) {
                                redisUtil.set(cacheKey, result);
                            } else {
                                redisUtil.set(cacheKey, result, expire);
                            }
                        }*/
                        try{
                            if (expire == 0l) {
                                redisUtil.set(cacheKey, JSON.toJSONString(result));
                            } else {
                                redisUtil.set(cacheKey, JSON.toJSONString(result), expire);
                            }
                        }catch (Exception e){
                            if (expire == 0l) {
                                redisUtil.set(cacheKey, result);
                            } else {
                                redisUtil.set(cacheKey, result, expire);
                            }
                        }
                    } else {
                        log.info("cached,no cache null,key:" + cacheKey);
                    }
                } else {
                    /*if (T == Map.class || T == List.class || T == PageInfo.class || T == BaseBean.class) {
                        result = JSON.parseObject(value.toString(), T);
                    } else {
                        result = value;
                    }*/
                    try{
                        result = JSON.parseObject(value.toString(), T);
                    } catch (Exception e) {
                        result = value;
                    }
                    //redisUtil.expire(cacheKey,expire);//重置过期时间
                    log.info("cached,key:" + cacheKey);
                }
            } catch (Exception e) {
                log.error("$$key:" + cacheKey + ":$$" + e.getMessage(), e);
                result = pjp.proceed();
            }

        } else {
            result = pjp.proceed();
        }
        
        return result;
    }
}
