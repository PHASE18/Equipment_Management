package com.equipment.management.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CRUD 模块权限约定：{module}:list / :view / :add / :edit / :delete
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrudPermission {

    /** 返回权限编码使用的业务模块名。 */
    String module();
}
