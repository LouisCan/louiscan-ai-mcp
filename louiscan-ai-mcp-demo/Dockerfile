
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/smart-ai-demo-1.0.0.jar ./app.jar


EXPOSE 8601


ENTRYPOINT ["java", "-jar", "app.jar"]
