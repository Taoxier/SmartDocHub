package com.taoxier.smartdochub.system.util;

import com.taoxier.smartdochub.system.model.entity.User;
import com.taoxier.smartdochub.system.model.entity.UserRole;
import com.taoxier.smartdochub.system.service.UserService;
import com.taoxier.smartdochub.system.service.UserRoleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * 用户创建工具类，用于在开发阶段方便测试
 */
@Configuration
@ComponentScan(basePackages = "com.taoxier.smartdochub")
public class UserCreator {

    public static void main(String[] args) {
        // 创建 Spring 上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(UserCreator.class);

        try {
            // 获取所需的服务
            UserService userService = context.getBean(UserService.class);
            UserRoleService userRoleService = context.getBean(UserRoleService.class);
            PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

            System.out.println("Services loaded successfully!");

            // 配置要创建的用户信息
            String username = "admin";
            String password = "123456";
            String nickname = "超级管理员";
            String email = "1803173059@qq.com";
            String mobile = "13246005132";
            Long deptId = 1L;
            Long roleId = 1L; // 超级管理员角色

            // 创建用户
            User user = new User();
            user.setUsername(username);
            user.setNickname(nickname);
            user.setGender(1);
            user.setPassword(passwordEncoder.encode(password));
            user.setDeptId(deptId);
            user.setMobile(mobile);
            user.setStatus(1);
            user.setEmail(email);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDeleted(0);

            System.out.println("User object created!");

            boolean saved = userService.save(user);
            if (saved) {
                System.out.println("用户创建成功！");
                System.out.println("用户名: " + username);
                System.out.println("密码: " + password);
                System.out.println("邮箱: " + email);
                System.out.println("用户ID: " + user.getId());

                // 关联角色
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                boolean roleSaved = userRoleService.save(userRole);
                if (roleSaved) {
                    System.out.println("角色关联成功！");
                } else {
                    System.out.println("角色关联失败！");
                }
            } else {
                System.out.println("用户创建失败！");
            }
        } catch (Exception e) {
            System.err.println("创建用户时发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭上下文
            context.close();
        }
    }
}