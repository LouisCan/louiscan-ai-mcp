package com.github.LouisCan.mcp.server.sdk.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface McpParam {

    /**
     * 参数名称
     */
    String name();

    /**
     * 参数描述
     */
    String description();

    /**
     * 参数可选值枚举
     */
    String[] enums() default {};

    /**
     * 参数是否必需
     */
    boolean required() default false;
}
