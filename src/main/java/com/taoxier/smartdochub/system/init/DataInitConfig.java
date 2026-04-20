package com.taoxier.smartdochub.system.init;

import com.taoxier.smartdochub.common.constant.SystemConstants;
import com.taoxier.smartdochub.system.model.entity.User;
import com.taoxier.smartdochub.system.model.entity.UserRole;
import com.taoxier.smartdochub.system.service.UserService;
import com.taoxier.smartdochub.system.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * 数据初始化配置
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig {

    private final UserService userService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner dataInitRunner() {
        return args -> {
            initAdminUser();
        };
    }

    /**
     * 初始化默认管理员用户
     */
    private void initAdminUser() {
        // 检查是否已存在管理员用户
        User adminUser = userService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "admin")
        );

        if (adminUser == null) {
            // 创建管理员用户
            User user = new User();
            user.setUsername("admin");
            user.setNickname("超级管理员");
            user.setGender(1);
            user.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
            user.setDeptId(1L);
            user.setMobile("13800138000");
            user.setStatus(1);
            user.setEmail("admin@example.com");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDeleted(0);

            boolean saved = userService.save(user);
            if (saved) {
                log.info("默认管理员用户创建成功，用户名: admin, 密码: {}", SystemConstants.DEFAULT_PASSWORD);

                // 关联超级管理员角色
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(1L); // 超级管理员角色ID
                userRoleService.save(userRole);
                log.info("管理员用户角色关联成功");
            } else {
                log.error("默认管理员用户创建失败");
            }
        } else {
            log.info("管理员用户已存在，跳过初始化");
        }
    }
}