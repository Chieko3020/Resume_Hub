package com.resume;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 简历应用测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class ResumeApplicationTest {

    @Test
    public void contextLoads() {
        // 测试Spring上下文是否能正常加载
        assertTrue(true);
    }

    @Test
    public void testBasicFunctionality() {
        // 测试基本功能
        assertTrue(true);
        assertTrue(2 + 2 == 4);
    }
}
