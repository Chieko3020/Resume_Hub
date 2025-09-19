package com.resume.util;

import org.springframework.stereotype.Component;

/**
 * 密码工具类（明文模式，非生产环境使用）
 */
@Component
public class PasswordUtil {

    /**
     * 明文“加密”：直接返回原文
     */
    public static String encode(String rawPassword) {
        return rawPassword == null ? null : rawPassword;
    }

    /**
     * 明文校验：字符串相等
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null && encodedPassword == null) return true;
        if (rawPassword == null || encodedPassword == null) return false;
        return rawPassword.equals(encodedPassword);
    }
}
