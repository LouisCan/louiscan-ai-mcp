## MCP-Java-SDK（jdk8 + Springboot2.x）基于streamableHttp MCP 协议


`louiscan-ai-mcp-spring-boot-starter` 是一个专为简化 AI 可调用接口服务开发而设计的 Spring Boot 工具包。它通过 MCP (Machine Callable Protocol) 协议，帮助开发者快速构建可被 AI 调用的服务端点，极大提升了集成和部署效率。该工具包采用注解驱动的设计模式，支持自动注册服务、生成工具列表以及处理工具调用等功能，让开发者能够专注于业务逻辑的实现，而不必为底层通信和协议处理耗费精力。无论是个人开发者还是企业级用户，都可以借助此工具包轻松打造高效、可靠的 AI 接口服务。

# louiscan-ai-mcp-spring-boot-starter 项目说明文档

## 1. 项目概述
`louiscan-ai-mcp-spring-boot-starter` 是一个基于 Spring Boot 的 AI 工具包，旨在提供 MCP (Machine Callable Protocol) 服务端点的快速集成能力。该模块简化了 AI 可调用接口服务的开发和部署。

### 目标用户
- 开发者
- AI 应用开发者

### 核心功能
- 提供 MCP 服务端点的自动配置和注册功能。
- 支持创建可被 AI 调用的接口服务。

## 2. 技术架构

### 架构图
```
+-------------------+
|   Spring Boot     |
|   Application     |
+-------------------+
        |
        v
+-------------------+
|   @McpServerEndpoint |
|   (服务端点注解)    |
+-------------------+
        |
        v
+-------------------+
|   McpStreamableHttpHandler |
|   (HTTP 处理器)    |
+-------------------+
        |
        v
+-------------------+
|   @McpFunction      |
|   (功能方法注解)    |
+-------------------+
        |
        v
+-------------------+
|   @McpParam         |
|   (参数元数据注解)  |
+-------------------+
```


### 设计模式
- **自动配置模式**：通过 `McpServerEndpointAutoRegistry` 实现 MCP 服务端点的自动注册。
- **注解驱动模式**：使用 `@McpServerEndpoint`, `@McpFunction`, 和 `@McpParam` 注解来简化服务定义。

### 关键技术决策
- 使用 **Spring Boot 2.7.18** 实现快速启动和配置。
- 使用 **Maven** 进行依赖管理。

## 3. 技术选型
- **前端**: 未提供。
- **后端**: Spring Boot 2.7.18。
- **数据库**: 未提供。
- **其他依赖**: JDK 8 或更高版本。

## 4. 开发环境

### 必需工具
- **JDK 8 或更高版本**
- **Maven**

### 构建命令
```bash
mvn clean install
```


### 本地开发
- 克隆仓库并导入到 IDE 中运行 Spring Boot 应用。

### 部署
- 示例应用包含 Dockerfile 和 docker-compose.yml 文件，可用于容器化部署。

## 5. 技术约束
- **代码规范**: 遵循 Java 编码规范。
- **性能要求**: 未提供。
- **安全要求**: MCP 服务受项目本身的鉴权系统影响，需根据实际情况配置。
- **已知问题**: 依赖需要手动安装至本地仓库。

## 6. 项目结构

### 目录结构
```
louiscan-ai-mcp-spring-boot-starter/
├── src
│   └── main
│       └── java
│           └── com
│               └── github
│                   └── LouisCan
│                       └── mcp
│                           └── server
│                               └── sdk
│                                   ├── annotation
│                                   │   ├── McpFunction.java
│                                   │   ├── McpParam.java
│                                   │   └── McpServerEndpoint.java
│                                   └── support
│                                       ├── McpConstants.java
│                                       ├── McpFunctionInfo.java
│                                       ├── McpServerEndpointAutoRegistry.java
│                                       └── McpStreamableHttpHandler.java
└── pom.xml
```


## 7. 依赖关系

### Maven 依赖
```xml
        <dependency>
            <groupId>com.github.LouisCan</groupId>
            <artifactId>louiscan-ai-mcp-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
```


## 8. 核心类说明

### `McpFunction.java`

```java
/**
 * @McpFunction 注解用于标识 MCP 服务中的具体功能方法。
 * 每个被此注解标记的方法都将作为可被外部调用的功能点注册到 MCP 服务中。
 */
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
```


### `McpParam.java`
```java
/**
 * @McpParam 注解用于定义方法参数的元数据信息。
 * 这些信息包括参数名称、描述、可选值以及是否必需等，
 * 以便在生成工具列表时提供详细的参数说明。
 */
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
```


### `McpServerEndpoint.java`
```java
/**
 * @McpServerEndpoint 注解用于标识一个类作为 MCP 服务端点。
 * 被此注解标记的类将自动注册为 Spring Bean，并映射到指定的 HTTP 接口路径。
 */
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
```





## 9. 总结
`louiscan-ai-mcp-spring-boot-starter` 模块提供了一套完整的工具和服务，使得开发者能够轻松地集成和部署 MCP 服务。通过注解驱动的方式，开发者可以快速定义和注册服务端点，从而专注于业务逻辑的实现。
