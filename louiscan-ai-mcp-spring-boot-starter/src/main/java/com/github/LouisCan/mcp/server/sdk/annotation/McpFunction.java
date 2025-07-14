package com.github.LouisCan.mcp.server.sdk.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface McpFunction {

    /**
     * 函数名称
     */
    String name();

    /**
     * 函数描述
     */
    String description();
}
