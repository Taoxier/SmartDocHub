import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized, isAdmin } from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }
            ]
        }, 
        // 管理端路由（后台管理系统）
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/layout/MainLayout.vue'),
            redirect: '/admin/dashboard',
            meta: { requiresAdmin: true },
            children: [
                {
                    path: 'dashboard',
                    name: 'admin-dashboard',
                    component: () => import('@/views/admin/Dashboard.vue'),
                    meta: { title: '仪表盘' }
                }, {
                    path: 'docs',
                    name: 'admin-docs',
                    component: () => import('@/views/admin/DocumentManagement.vue'),
                    meta: { title: '文档管理' }
                }, {
                    path: 'users',
                    name: 'admin-users',
                    component: () => import('@/views/admin/UserManagement.vue'),
                    meta: { title: '用户管理' }
                }, {
                    path: 'roles',
                    name: 'admin-roles',
                    component: () => import('@/views/system/RoleList.vue'),
                    meta: { title: '角色管理' }
                }, {
                    path: 'role-edit/:id?',
                    name: 'admin-roleEdit',
                    component: () => import('@/views/system/RoleEdit.vue'),
                    meta: { title: '编辑角色' }
                }, {
                    path: 'config',
                    name: 'admin-config',
                    component: () => import('@/views/system/Settings.vue'),
                    meta: { title: '系统配置' }
                }, {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/CategoryManagement.vue'),
          meta: { title: '分类管理' }
        }, {
          path: 'tags',
          name: 'admin-tags',
          component: () => import('@/views/admin/TagManagement.vue'),
          meta: { title: '标签管理' }
        }, {
          path: 'tasks',
          name: 'admin-tasks',
          component: () => import('@/views/admin/TaskManagement.vue'),
          meta: { title: '任务管理' }
        }, {
          path: 'comments',
          name: 'admin-comments',
          component: () => import('@/views/admin/CommentManagement.vue'),
          meta: { title: '评论管理' }
        }, {
          path: 'logs',
          name: 'admin-logs',
          component: () => import('@/views/system/LogList.vue'),
          meta: { title: '日志管理' }
        }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    console.log('路由守卫执行，当前路径:', to.fullPath);
    const isUnauthorized = unauthorized()
    console.log('是否未授权:', isUnauthorized);
    
    // 处理登录状态
    if(to.name.startsWith('welcome') && !isUnauthorized) {
        console.log('已登录，跳转到管理端首页');
        next('/admin/dashboard')
    } else if(to.fullPath.startsWith('/admin') && isUnauthorized) {
        console.log('未登录，跳转到登录页');
        next('/')
    } 
    // 处理管理员权限
    else if(to.meta.requiresAdmin && !isAdmin()) {
        console.log('非管理员，无权访问管理端');
        next('/')
    } else {
        console.log('正常导航');
        next()
    }
})

export default router