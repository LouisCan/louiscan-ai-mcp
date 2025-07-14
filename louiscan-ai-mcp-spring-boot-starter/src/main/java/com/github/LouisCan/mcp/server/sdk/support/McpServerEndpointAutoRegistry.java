package com.github.LouisCan.mcp.server.sdk.support;

/**
 * MCP服务端点自动注册处理器
 * 
 * <p>实现Spring BeanPostProcessor接口，自动扫描并注册带有{@link com.github.LouisCan.mcp.server.sdk.annotation.McpServerEndpoint}注解的类，
 * 将其注册为可通过HTTP访问的MCP服务端点。</p>
 *
 * @author LouisCan
 * @version 1.0.0
 */
import com.github.LouisCan.mcp.server.sdk.annotation.McpFunction;
import com.github.LouisCan.mcp.server.sdk.annotation.McpParam;
import com.github.LouisCan.mcp.server.sdk.annotation.McpServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class McpServerEndpointAutoRegistry implements BeanPostProcessor, ApplicationContextAware, BeanFactoryAware {

    private ApplicationContext applicationContext;
    private BeanFactory        beanFactory;
    private WebMvcProperties   webMvcProperties;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.webMvcProperties = applicationContext.getBean(WebMvcProperties.class);
    }

    /**
     * 设置BeanFactory实例
     *
     * @param beanFactory Spring BeanFactory实例
     * @throws BeansException 如果设置过程中发生错误
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * Bean初始化后处理逻辑，检查并注册MCP服务端点
     *
     * <p>实现BeanPostProcessor接口的核心方法，在bean初始化完成后被调用。
     * 检查bean是否带有{@link com.github.LouisCan.mcp.server.sdk.annotation.McpServerEndpoint}注解，
     * 如果是则收集并注册其中的MCP函数。</p>
     *
     * @param bean 当前处理的bean实例
     * @param beanName bean的名称
     * @return 处理后的bean实例
     * @throws BeansException 如果处理过程中发生错误
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        McpServerEndpoint annotation = beanClass.getAnnotation(McpServerEndpoint.class);
        if (annotation != null) {
            String path = annotation.value();
            try {
                List<McpFunctionInfo> functionInfos = collectMcpFunctions(beanClass);

                BeanDefinitionRegistry beanRegistry = (BeanDefinitionRegistry) beanFactory;
                String handlerBeanName = beanClass.getSimpleName() + "@McpServerEndpoint";

                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                        .genericBeanDefinition(McpStreamableHttpHandler.class);
                beanRegistry.registerBeanDefinition(handlerBeanName, beanDefinitionBuilder.getBeanDefinition());

                McpStreamableHttpHandler handler = (McpStreamableHttpHandler) applicationContext.getBean(handlerBeanName);
                handler.setName(annotation.name());
                handler.setVersion(annotation.version());
                handler.setTargetBean(bean);
                handler.setFunctionInfos(functionInfos);

                registerMcpServerEndpoint(path, handler);

                log.info("Mcp服务接口创建成功: {}", path);
            } catch (Exception e) {
                log.error("Mcp服务接口创建失败: {}", e.getMessage(), e);
            }
        }
        return bean;
    }


    /**
     * 收集类中所有MCP函数信息
     *
     * <p>扫描指定类中所有带有{@link com.github.LouisCan.mcp.server.sdk.annotation.McpFunction}注解的方法，
     * 并为每个方法创建对应的{@link McpFunctionInfo}对象。</p>
     *
     * @param beanClass 要扫描的类对象
     * @return 包含所有MCP函数信息的列表，每个元素对应一个带有@McpFunction注解的方法
     */
    private List<McpFunctionInfo> collectMcpFunctions(Class<?> beanClass) {
        Method[] methods = beanClass.getDeclaredMethods();
        List<McpFunctionInfo> functionInfos = new ArrayList<>();

        for (Method method : methods) {
            McpFunction mcpFunction = method.getAnnotation(McpFunction.class);
            if (mcpFunction != null) {
                List<McpFunctionInfo.ParamInfo> paramInfos = collectFunctionParamInfos(method);
                McpFunctionInfo functionInfo = new McpFunctionInfo(mcpFunction.name(), mcpFunction.description(), method, paramInfos);
                functionInfos.add(functionInfo);
            }
        }
        return functionInfos;
    }


    /**
     * 收集方法参数信息
     *
     * <p>扫描方法参数上的{@link com.github.LouisCan.mcp.server.sdk.annotation.McpParam}注解，
     * 为每个参数创建对应的参数描述信息。</p>
     *
     * @param method 要扫描的方法对象
     * @return 包含所有参数信息的列表，每个元素对应一个方法参数
     */
    private static List<McpFunctionInfo.ParamInfo> collectFunctionParamInfos(Method method) {
        Parameter[] parameters = method.getParameters();
        List<McpFunctionInfo.ParamInfo> paramInfos = new ArrayList<>();

        for (Parameter parameter : parameters) {
            McpParam mcpParam = parameter.getAnnotation(McpParam.class);
            if (mcpParam != null) {
                McpFunctionInfo.ParamInfo paramInfo = new McpFunctionInfo.ParamInfo(
                        mcpParam.name(), mcpParam.description(), mcpParam.enums(), mcpParam.required());
                paramInfos.add(paramInfo);
            }
        }
        return paramInfos;
    }


    /**
     * 注册MCP服务端点到Spring MVC
     *
     * <p>为指定的路径和处理器bean创建GET和POST请求映射，
     * 根据配置的路径匹配策略进行注册。</p>
     *
     * @param path 要注册的端点路径
     * @param bean 处理请求的bean对象
     * @throws NoSuchMethodException 如果找不到处理方法
     */
    private void registerMcpServerEndpoint(String path, Object bean) throws NoSuchMethodException {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

        if (webMvcProperties.getPathmatch().getMatchingStrategy() == WebMvcProperties.MatchingStrategy.PATH_PATTERN_PARSER) {
            config.setPatternParser(new PathPatternParser());
        } else {
            config.setPathMatcher(new AntPathMatcher());
        }
        RequestMappingInfo handleGet = RequestMappingInfo.paths(path).methods(RequestMethod.GET).options(config).build();
        mapping.registerMapping(handleGet, bean, McpStreamableHttpHandler.class.getMethod("handleGet"));

        RequestMappingInfo handlePost = RequestMappingInfo.paths(path).methods(RequestMethod.POST).options(config).build();
        mapping.registerMapping(handlePost, bean, McpStreamableHttpHandler.class.getMethod("handlePost", String.class));
    }
}