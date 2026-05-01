import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateBCryptPassword {
    public static void main(String[] args) {
        // 要加密的密码
        String password = "123456";
        
        // 创建 BCrypt 密码编码器
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成密码哈希
        String hashedPassword = encoder.encode(password);
        
        // 输出结果
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt 哈希: " + hashedPassword);
        
        // 验证密码
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("密码验证结果: " + matches);
    }
}