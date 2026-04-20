package com.taoxier.smartdochub;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * @Author taoxier
 * @Date 2025/10/14 下午8:24
 * @描述
 */
public class PasswordEncryptionTest {
    /**
     * 测试方法：输入原始密码，输出加密后的密码
     * 直接运行此类即可生成加密密码
     */
    public static void main(String[] args) {
        // 在这里修改你需要加密的原始密码
        String rawPassword = "123456";

        // 加密密码（工作因子10，与数据库中存储格式保持一致）
        String encryptedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

        // 打印结果
        System.out.println(encryptedPassword);
    }
}
