package com.taoxier.smartdochub.system.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码验证工具类
 */
public class PasswordValidator {

    public static void main(String[] args) {
        // 数据库中的密码哈希
        String dbPassword = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";

        // 明文密码
        String plainPassword = "123456";

        // 创建密码编码器
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 验证密码
        boolean matches = encoder.matches(plainPassword, dbPassword);

        System.out.println("密码验证结果: " + matches);

        if (matches) {
            System.out.println("密码正确！");
        } else {
            System.out.println("密码错误！");
            // 生成新的密码哈希
            String newHash = encoder.encode(plainPassword);
            System.out.println("新的密码哈希: " + newHash);
        }
    }
}