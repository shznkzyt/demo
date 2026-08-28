package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 学生管理系统的 Spring Boot 启动类。
 * {@link SpringBootApplication} 会启用自动配置、组件扫描和配置类支持。
 */
@SpringBootApplication
public class DemoApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * SpringApplication 会创建应用上下文、加载配置并启动内嵌 Web 服务器。
     *
     * @param args 命令行启动参数，可用于覆盖应用配置
     */
    public static void main(String[] args) {
        // 以当前类作为主要配置源启动整个 Spring Boot 应用。
        SpringApplication.run(DemoApplication.class, args);
    }

}
