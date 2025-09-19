package com.resume.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT工具类测试")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("测试生成Token")
    void testGenerateToken() {
        String username = "testuser";
        String role = "user";
        
        String token = jwtUtil.generateToken(username, role);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT token应该包含点分隔符
    }

    @Test
    @DisplayName("测试从Token获取用户名")
    void testGetUsernameFromToken() {
        String username = "testuser";
        String role = "user";
        String token = jwtUtil.generateToken(username, role);
        
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("测试从Token获取角色")
    void testGetRoleFromToken() {
        String username = "testuser";
        String role = "admin";
        String token = jwtUtil.generateToken(username, role);
        
        String extractedRole = jwtUtil.getRoleFromToken(token);
        
        assertEquals(role, extractedRole);
    }

    @Test
    @DisplayName("测试Token验证 - 有效Token")
    void testValidateTokenValid() {
        String username = "testuser";
        String role = "user";
        String token = jwtUtil.generateToken(username, role);
        
        boolean isValid = jwtUtil.validateToken(token, username);
        
        assertTrue(isValid);
    }

    @Test
    @DisplayName("测试Token验证 - 无效Token")
    void testValidateTokenInvalid() {
        String username = "testuser";
        String invalidToken = "invalid.token.here";
        
        // 期望抛出异常或返回false
        try {
            boolean isValid = jwtUtil.validateToken(invalidToken, username);
            assertFalse(isValid);
        } catch (Exception e) {
            // 如果抛出异常也是可以接受的
            assertTrue(e instanceof io.jsonwebtoken.MalformedJwtException || 
                      e instanceof IllegalArgumentException);
        }
    }

    @Test
    @DisplayName("测试Token验证 - 错误用户名")
    void testValidateTokenWrongUsername() {
        String username = "testuser";
        String role = "user";
        String token = jwtUtil.generateToken(username, role);
        
        boolean isValid = jwtUtil.validateToken(token, "wronguser");
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试Token验证 - null Token")
    void testValidateTokenNull() {
        String username = "testuser";
        
        // 期望抛出异常或返回false
        try {
            boolean isValid = jwtUtil.validateToken(null, username);
            assertFalse(isValid);
        } catch (Exception e) {
            // 如果抛出异常也是可以接受的
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    @DisplayName("测试Token验证 - 空字符串Token")
    void testValidateTokenEmpty() {
        String username = "testuser";
        
        // 期望抛出异常或返回false
        try {
            boolean isValid = jwtUtil.validateToken("", username);
            assertFalse(isValid);
        } catch (Exception e) {
            // 如果抛出异常也是可以接受的
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    @DisplayName("测试Token验证 - null用户名")
    void testValidateTokenNullUsername() {
        String username = "testuser";
        String role = "user";
        String token = jwtUtil.generateToken(username, role);
        
        boolean isValid = jwtUtil.validateToken(token, null);
        
        assertFalse(isValid);
    }

    @Test
    @DisplayName("测试不同用户和角色的Token生成")
    void testDifferentUsersAndRoles() {
        // 测试不同用户
        String token1 = jwtUtil.generateToken("user1", "user");
        String token2 = jwtUtil.generateToken("user2", "admin");
        
        assertNotEquals(token1, token2);
        
        // 验证用户名提取
        assertEquals("user1", jwtUtil.getUsernameFromToken(token1));
        assertEquals("user2", jwtUtil.getUsernameFromToken(token2));
        
        // 验证角色提取
        assertEquals("user", jwtUtil.getRoleFromToken(token1));
        assertEquals("admin", jwtUtil.getRoleFromToken(token2));
    }
}
