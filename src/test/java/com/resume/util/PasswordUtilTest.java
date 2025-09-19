package com.resume.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类测试
 */
@DisplayName("密码工具类测试")
class PasswordUtilTest {

    @Test
    @DisplayName("测试密码加密")
    void testEncode() {
        String rawPassword = "testPassword123";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        // 由于当前实现是明文存储，所以应该返回原文
        assertEquals(rawPassword, encodedPassword);
        assertNotNull(encodedPassword);
    }

    @Test
    @DisplayName("测试密码验证 - 正确密码")
    void testMatchesCorrectPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        boolean result = PasswordUtil.matches(rawPassword, encodedPassword);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试密码验证 - 错误密码")
    void testMatchesWrongPassword() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        boolean result = PasswordUtil.matches(wrongPassword, encodedPassword);
        assertFalse(result);
    }

    @Test
    @DisplayName("测试密码验证 - 空密码")
    void testMatchesEmptyPassword() {
        String rawPassword = "";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        boolean result = PasswordUtil.matches(rawPassword, encodedPassword);
        assertTrue(result);
    }

    @Test
    @DisplayName("测试密码验证 - null密码")
    void testMatchesNullPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        boolean result = PasswordUtil.matches(null, encodedPassword);
        assertFalse(result);
    }

    @Test
    @DisplayName("测试密码验证 - 空字符串密码")
    void testMatchesEmptyStringPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = PasswordUtil.encode(rawPassword);
        
        boolean result = PasswordUtil.matches("", encodedPassword);
        assertFalse(result);
    }

    @Test
    @DisplayName("测试密码验证 - null编码密码")
    void testMatchesNullEncodedPassword() {
        String rawPassword = "testPassword123";
        
        boolean result = PasswordUtil.matches(rawPassword, null);
        assertFalse(result);
    }
}
