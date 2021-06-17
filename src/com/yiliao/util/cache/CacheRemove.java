package com.yiliao.util.cache;


import java.lang.annotation.*;

/**
 * The interface Auth passport.
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheRemove {
    /**
     * cache key，需删除的key，默认是空
     */
    String cacheKey() default "";

    /**
     * pattern 批量删除条件，默认是空
     * @return
     */
    String pattern() default  "";
}
