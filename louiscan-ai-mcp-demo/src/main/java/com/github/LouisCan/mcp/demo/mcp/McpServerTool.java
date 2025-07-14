package com.github.LouisCan.mcp.demo.mcp;

import com.github.LouisCan.mcp.server.sdk.annotation.McpFunction;
import com.github.LouisCan.mcp.server.sdk.annotation.McpParam;
import com.github.LouisCan.mcp.server.sdk.annotation.McpServerEndpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@McpServerEndpoint(value = "/mcp", version = "1.0.0", name = "天气查询服务")
public class McpServerTool {

    @McpFunction(name = "getWeather", description = "获取天气信息")
    public String getWeather(@McpParam(name = "city", description = "城市名称", required = true) String city) {
        return String.format("%s: 晴天，温度25℃", city);
    }

    @McpFunction(name = "getSpeciality", description = "获取城市特产")
    public String getSpeciality(@McpParam(name = "city", description = "城市名称", required = true) String city) {
        return String.format("%s特产是小笼包", city);
    }

    @McpFunction(name = "getServerInfo", description = "获取服务器信息")
    public List<ServerInfo> getServerInfo(
            @McpParam(name = "ipAddress", description = "服务器 IP 地址", required = false) String ipAddress
    ) {
        // 模拟数据，实际使用时应从数据库或配置文件中获取
        List<ServerInfo> serverInfos = new ArrayList<>();

        serverInfos.add(new ServerInfo("192.168.100.101", "混合云MQ-01", "/ Disk space is critically low (used > 95%), reach 95.8326 %"));
        serverInfos.add(new ServerInfo("192.168.100.102", "AOC环境监控ES", "网卡流量 p2p2: 大于 (> 500 M 超过30m)"));

        if (ipAddress != null) {
            return serverInfos.stream()
                    .filter(info -> info.getIpAddress().equals(ipAddress))
                    .collect(Collectors.toList());
        }

        return serverInfos;
    }

    public static class ServerInfo {
        private String ipAddress;
        private String appName;
        private String alarmInfo;

        public ServerInfo(String ipAddress, String appName, String alarmInfo) {
            this.ipAddress = ipAddress;
            this.appName = appName;
            this.alarmInfo = alarmInfo;
        }

        // Getters and Setters
        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getAlarmInfo() {
            return alarmInfo;
        }

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", appName='" + appName + '\'' +
                    ", alarmInfo='" + alarmInfo + '\'' +
                    '}';
        }

        public void setAlarmInfo(String alarmInfo) {
            this.alarmInfo = alarmInfo;
        }


    }

}
