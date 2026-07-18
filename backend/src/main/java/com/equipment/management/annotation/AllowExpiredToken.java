package com.equipment.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 允许在 Token 过期后的宽限期内访问（用于 Token 续期接口）
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/** 标记允许在刷新宽限期内读取过期令牌的接口。 */
public @interface AllowExpiredToken {
}
