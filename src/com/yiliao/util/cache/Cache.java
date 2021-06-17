package com.yiliao.util.cache;


import java.lang.annotation.*;

/**
 * The interface Auth passport.
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    /**
     * cache key，可以空，默认是class+method
     */
    String cacheKey() default "";

    /**
 	* 是否将请求参数设置到缓存key里
     * @return
     */
    boolean setArguments() default true;

    /**
     * Cache过期时间，单位秒，默认3小时, 0表示永不过期.
     */
    long expire() default 180;
}
