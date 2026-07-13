package com.equipment.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口/功能权限校验，与 sys_permission.permission_code 对应
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需权限编码，类级注解时作为默认权限
     */
    String value() default "";

    /**
     * 满足任一权限即可访问
     */
    String[] any() default {};
}
