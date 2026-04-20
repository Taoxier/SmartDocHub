package com.taoxier.smartdochub.core.security.service;

import com.taoxier.smartdochub.core.security.model.SysUserDetails;
import com.taoxier.smartdochub.core.security.model.UserAuthCredentials;
import com.taoxier.smartdochub.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 系统用户认证 DetailsService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            UserAuthCredentials userAuthCredentials = userService.getAuthCredentialsByUsername(username);
            if (userAuthCredentials == null) {
                throw new UsernameNotFoundException(username);
            }
            // 添加调试日志
            log.info("用户 {} 认证信息: 用户名={}, 密码长度={}, 状态={}", 
                    username, 
                    userAuthCredentials.getUsername(), 
                    userAuthCredentials.getPassword() != null ? userAuthCredentials.getPassword().length() : 0, 
                    userAuthCredentials.getStatus());
            return new SysUserDetails(userAuthCredentials);
        } catch (Exception e) {
            // 记录异常日志
            log.error("认证异常:{} - {}", username, e.getMessage());
            // 抛出异常
            throw e;
        }
    }
}
