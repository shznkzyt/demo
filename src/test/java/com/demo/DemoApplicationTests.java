package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

    /** 校验 Spring 应用上下文可以正常启动。 */
    @Test
    void contextLoads() {
    }

}
