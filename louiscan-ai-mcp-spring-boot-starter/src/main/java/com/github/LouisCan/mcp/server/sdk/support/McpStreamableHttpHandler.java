package com.github.LouisCan.mcp.server.sdk.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.MethodNotAllowedException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Slf4j
public class McpStreamableHttpHandler {

    private String name;
    private String version;
    private List<McpFunctionInfo> functionInfos = new ArrayList<>();
    private Object targetBean;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理GET请求的方法。
     * 当前实现中，该方法会抛出MethodNotAllowedException，表示GET方法不被允许。
     *
     * @throws MethodNotAllowedException 当GET方法不被允许时抛出
     */
    public ResponseEntity<ObjectNode> handleGet() {
        throw new MethodNotAllowedException(HttpMethod.GET, null);
    }

    public ResponseEntity<ObjectNode> handlePost(@RequestBody String body) throws Exception {
        try {
            ObjectNode request = objectMapper.readValue(body, ObjectNode.class);
            if (request == null || !request.has("id")) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            }
            String id = request.get("id").asText();
            String method = request.get("method").asText();

            switch (method) {
                case McpConstants.METHOD_INITIALIZE:
                    return handleInitialize(id);
                case McpConstants.METHOD_TOOLS_LIST:
                    return handleListTools(id);
                case McpConstants.METHOD_TOOLS_CALL:
                    return handleCallTool(request);
                default:
                    return handleUnsupportedMethod(id, method);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    private ResponseEntity<ObjectNode> handleInitialize(String id) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put(McpConstants.JSON_RPC_VERSION, McpConstants.JSON_RPC_VERSION);
        response.put(McpConstants.JSON_RPC_ID, id);

        ObjectNode result = response.putObject(McpConstants.JSON_RPC_RESULT);
        result.put("protocolVersion", McpConstants.PROTOCOL_VERSION);
        ObjectNode capabilities = result.putObject("capabilities");

        ObjectNode serverInfo = result.putObject("serverInfo");
        serverInfo.put("name", name);
        serverInfo.put("version", version);
        return ResponseEntity.ok(response);
    }


    private ResponseEntity<ObjectNode> handleUnsupportedMethod(String id, String method) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put(McpConstants.JSON_RPC_VERSION, McpConstants.JSON_RPC_VERSION);
        response.put(McpConstants.JSON_RPC_ID, id);

        ObjectNode error = response.putObject(McpConstants.JSON_RPC_ERROR);
        error.put(McpConstants.JSON_RPC_CODE, McpConstants.ERROR_CODE_METHOD_NOT_FOUND);
        error.put(McpConstants.JSON_RPC_MESSAGE, String.format(McpConstants.ERROR_MSG_UNSUPPORTED_METHOD, method));

        return ResponseEntity.badRequest().body(response);
    }


    private ResponseEntity<ObjectNode> handleListTools(String id) {
        log.info("获取工具列表, 请求ID: {}", id);
        ObjectNode response = objectMapper.createObjectNode();
        response.put(McpConstants.JSON_RPC_VERSION, McpConstants.JSON_RPC_VERSION);
        response.put(McpConstants.JSON_RPC_ID, id);

        ObjectNode result = response.putObject(McpConstants.JSON_RPC_RESULT);
        result.putArray("tools");

        // 将functionInfos转换为工具列表
        if (functionInfos != null && !functionInfos.isEmpty()) {
            for (McpFunctionInfo functionInfo : functionInfos) {
                ObjectNode tool = result.withArray("tools").addObject();
                tool.put("name", functionInfo.getName());
                tool.put("description", functionInfo.getDescription());

                // 添加必需参数列表
                if (functionInfo.getParams() != null && !functionInfo.getParams().isEmpty()) {
                    // 添加参数信息
                    ObjectNode paramsNode = tool.putObject("inputSchema");
                    paramsNode.put("type", "object");
                    ObjectNode properties = paramsNode.putObject("properties");
                    List<String> required = new ArrayList<>();

                    for (McpFunctionInfo.ParamInfo paramInfo : functionInfo.getParams()) {
                        if (paramInfo == null) {
                            continue;
                        }
                        ObjectNode paramNode = properties.putObject(paramInfo.getName());
                        paramNode.put("type", "string");
                        paramNode.put("description", paramInfo.getDescription());

                        // 如果有枚举值，添加枚举值
                        if (paramInfo.getEnums() != null) {
                            for (String enumValue : paramInfo.getEnums()) {
                                paramNode.withArray("enum").add(enumValue);
                            }
                        }
                        // 如果是必需参数，添加到必需参数列表
                        if (paramInfo.isRequired()) {
                            required.add(paramInfo.getName());
                        }
                    }
                    // 添加必需参数列表
                    if (!required.isEmpty()) {
                        for (String req : required) {
                            paramsNode.withArray("required").add(req);
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok(response);
    }


    private ResponseEntity<ObjectNode> handleCallTool(ObjectNode request) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put(McpConstants.JSON_RPC_VERSION, McpConstants.JSON_RPC_VERSION);
        response.put(McpConstants.JSON_RPC_ID, request.get("id").asText());

        String toolName = null;
        ObjectNode params = null;

        if (request.has(McpConstants.JSON_RPC_PARAMS) && request.get(McpConstants.JSON_RPC_PARAMS).has("name")) {
            toolName = request.get(McpConstants.JSON_RPC_PARAMS).get("name").asText();

            if (request.get(McpConstants.JSON_RPC_PARAMS).has("arguments")) {
                params = (ObjectNode) request.get(McpConstants.JSON_RPC_PARAMS).get("arguments");
            }
        }

        McpFunctionInfo targetFunction = null;
        for (McpFunctionInfo functionInfo : functionInfos) {
            if (functionInfo.getName().equals(toolName)) {
                targetFunction = functionInfo;
                break;
            }
        }

        if (targetFunction != null && targetBean != null) {
            try {
                Method method = targetFunction.getMethod();
                Parameter[] parameters = method.getParameters();
                Object[] args = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    McpFunctionInfo.ParamInfo paramInfo = targetFunction.getParams().get(i);
                    String paramName = paramInfo.getName();

                    if (params != null && params.has(paramName)) {
                        args[i] = params.get(paramName).asText();
                    } else if (paramInfo.isRequired()) {
                        log.warn("缺少必需参数: {}", paramName);
                        ObjectNode error = response.putObject("error");
                        error.put(McpConstants.JSON_RPC_CODE, McpConstants.ERROR_CODE_INVALID_PARAMS);
                        error.put(McpConstants.JSON_RPC_MESSAGE, "缺少必需参数: " + paramName);
                        return ResponseEntity.ok(response);
                    }
                }

                Object result = method.invoke(targetBean, args);

                ObjectNode resultNode = response.putObject("result");

                List<Map<String, Object>> content = new ArrayList<>();
                Map<String, Object> textContent = new HashMap<>();
                textContent.put("type", "text");
                textContent.put("text", result != null ? result.toString() : "");
                content.add(textContent);

                resultNode.set("content", objectMapper.valueToTree(content));

            } catch (Exception e) {
                log.error("工具 {} 执行时发生异常", toolName, e);
                ObjectNode error = response.putObject("error");
                error.put(McpConstants.JSON_RPC_CODE, McpConstants.ERROR_CODE_INTERNAL_ERROR);
                error.put(McpConstants.JSON_RPC_MESSAGE, "工具 " + toolName + " 执行时发生异常" + e.getMessage());
            }
        } else {
            log.warn("找不到指定的工具: {}", toolName);
            ObjectNode error = response.putObject("error");
            error.put(McpConstants.JSON_RPC_CODE, McpConstants.ERROR_CODE_METHOD_NOT_FOUND);
            error.put(McpConstants.JSON_RPC_MESSAGE, "找不到指定的工具: " + toolName);
        }

        return ResponseEntity.ok(response);
    }
}
