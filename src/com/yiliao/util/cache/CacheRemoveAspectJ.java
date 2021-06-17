package com.yiliao.util.cache;


import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import com.yiliao.util.BaseUtil;

//@Slf4j
@Aspect
@Component
public class CacheRemoveAspectJ extends AspectJ {
	
	private Logger log = LoggerFactory.getLogger(getClass());

    private CacheRemove cacheRemove;

    /**
     * 设置缓存名
     */
    public void setCache(CacheRemove cacheRemove) {
        this.cacheRemove = cacheRemove;
    }

    @Pointcut("@annotation(com.yiliao.util.cache.CacheRemove)")
    public void methodCacheRemovePointcut(){}

    @Around("methodCacheRemovePointcut()")
    public Object methodCacheRemovePointcut(ProceedingJoinPoint pjp) throws Throwable{
    	
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
        
        CacheRemove cacheRemove = method.getAnnotation(CacheRemove.class);  
    	
        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();  
        String [] parameterNames=u.getParameterNames(method);
        
        
        Object result = null;
        String cacheKey = null;
        String pattern = null;
        if (null != cacheRemove ) {
            try {
                cacheKey = cacheRemove.cacheKey();
                pattern = cacheRemove.pattern();
                //如果用了表达式的则单独处理
                if(cacheKey.startsWith("#") && cacheKey.endsWith("#")){
                    cacheKey = getArgumentsValue(cacheKey,parameterNames,arguments);
                }
                if (!BaseUtil.isEmpty(cacheKey)) {
                    redisUtil.removePattern(cacheKey);
                    log.info("cached,cache remove,key:" + cacheKey);
                }
                //如果用了表达式的则单独处理
                if(pattern.startsWith("#") && pattern.endsWith("#")){
                    pattern = getArgumentsValue(pattern,parameterNames,arguments);
                }
                if (!BaseUtil.isEmpty(pattern)) {
                    redisUtil.removePattern(pattern);
                    log.info("cached,cache remove pattern,key:" + pattern);
                }
                result = pjp.proceed();
            } catch (Exception e) {
                log.error("$$remove_key:" + cacheKey + ":$$remove_pattern:"+ pattern + e.getMessage()+"*", e);
                result = pjp.proceed();
            }

        } else {
            result = pjp.proceed();
        }
        return result;
    }
}
