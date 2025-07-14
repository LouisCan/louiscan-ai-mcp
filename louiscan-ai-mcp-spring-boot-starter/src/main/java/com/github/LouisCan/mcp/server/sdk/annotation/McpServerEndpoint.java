package com.github.LouisCan.mcp.server.sdk.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface McpServerEndpoint {

    /**
     * MCP服务接口地址
     * @return 服务端点路径，如"/api/mcp"
     */
    String value();

    /**
     * MCP服务名称
     * @return 服务名称，用于标识服务
     */
    String name() default "";

    /**
     * MCP服务版本
     * @return 服务版本号
     */
    String version() default "";
}
