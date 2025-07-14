package com.github.LouisCan.mcp.server.sdk.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class McpConstants {
    public static final String JSON_RPC_VERSION = "2.0";
    public static final String JSON_RPC_ID = "id";
    public static final String JSON_RPC_RESULT = "result";
    public static final String JSON_RPC_ERROR = "error";
    public static final String JSON_RPC_CODE = "code";
    public static final String JSON_RPC_PARAMS = "params";
    public static final String JSON_RPC_MESSAGE = "message";

    public static final String METHOD_INITIALIZE = "initialize";
    public static final String METHOD_TOOLS_LIST = "tools/list";
    public static final String METHOD_TOOLS_CALL = "tools/call";

    public static final String PROTOCOL_VERSION = "2024-11-05";

    public static final int ERROR_CODE_METHOD_NOT_FOUND = -531;
    public static final int ERROR_CODE_INVALID_PARAMS = -532;
    public static final int ERROR_CODE_INTERNAL_ERROR = -533;

    public static final String ERROR_MSG_UNSUPPORTED_METHOD = "本服务器不支持 %s 方法";
    public static final String ERROR_MSG_MISSING_PARAM = "缺少必需参数: %s";
    public static final String ERROR_MSG_TOOL_EXECUTION = "工具 %s 执行时发生异常%s";
}
