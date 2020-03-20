package com.hawk.configCenter.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.lang.annotation.*;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Scope("refresh")
@Documented
public @interface RefreshScope {
    /**
     * @see Scope#proxyMode()
     * @return proxy mode
     */
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;
}
