package com.github.LouisCan.mcp.server.sdk.support;

import com.github.LouisCan.mcp.server.sdk.annotation.McpFunction;
import com.github.LouisCan.mcp.server.sdk.annotation.McpParam;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.List;


@Getter
public class McpFunctionInfo {

    /**
     * 功能名称，对应{@link McpFunction#name()}
     */
    private final String name;

    /**
     * 功能描述，对应{@link McpFunction#description()}
     */
    private final String description;

    /**
     * 功能方法对象
     */
    private final Method method;

    /**
     * 参数信息列表
     */
    private final List<ParamInfo> params;

    /**
     * 构造方法
     * @param name 功能名称
     * @param description 功能描述
     * @param method 方法对象
     * @param params 参数信息列表
     */
    public McpFunctionInfo(String name, String description, Method method, List<ParamInfo> params) {
        this.name = name;
        this.description = description;
        this.method = method;
        this.params = params;
    }

    /**
     * 封装方法参数的元数据信息
     * <p>
     * 包含参数名称、描述、可选值和是否必需等信息，
     * 这些信息来自{@link McpParam}注解
     * </p>
     */
    @Getter
    public static class ParamInfo {

        /**
         * 参数名称，对应{@link McpParam#name()}
         */
        private final String name;

        /**
         * 参数描述，对应{@link McpParam#description()}
         */
        private final String description;

        /**
         * 参数可选值，对应{@link McpParam#enums()}
         */
        private final String[] enums;

        /**
         * 参数是否必需，对应{@link McpParam#required()}
         */
        private final boolean required;

        /**
         * 构造方法
         * @param name 参数名称
         * @param description 参数描述
         * @param enums 参数可选值
         * @param required 是否必需
         */
        public ParamInfo(String name, String description, String[] enums, boolean required) {
            this.name = name;
            this.description = description;
            this.enums = enums;
            this.required = required;
        }
    }
}
