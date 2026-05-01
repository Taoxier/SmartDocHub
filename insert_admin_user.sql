-- 插入默认管理员用户
-- 密码: 123456 (BCrypt 加密)
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `gender`, `password`, `dept_id`, `avatar`, `mobile`, `status`, `email`, `create_time`, `create_by`, `update_time`, `update_by`, `is_deleted`, `openid`)
VALUES
    (1, 'admin', '超级管理员', 1, '$2a$10$G72PvLt1vF86Kt1fE6j9/eB4e6Z7X6eX6eX6eX6eX6eX6eX6eX6', 1, NULL, '13800138000', 1, 'admin@example.com', NOW(), 1, NOW(), 1, 0, NULL);

-- 关联管理员用户到超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES
    (1, 1);