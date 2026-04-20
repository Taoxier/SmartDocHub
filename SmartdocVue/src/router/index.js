import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/welcome',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        },
        // 客户端路由（普通用户端）
        {
            path: '/',
            name: 'client',
            component: () => import('@/layout/MainLayout.vue'),
            redirect: '/home',
            children: [
                {
                    path: 'home',
                    name: 'home',
                    component: () => import('@/views/dashboard/Dashboard.vue'),
                    meta: { title: '首页推荐' }
                }, {
                    path: 'doc/list',
                    name: 'doc-list',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '文档列表' }
                }, {
                    path: 'doc/detail/:id',
                    name: 'doc-detail',
                    component: () => import('@/views/document/DocumentDetail.vue'),
                    meta: { title: '文档详情' }
                }, {
                    path: 'categories',
                    name: 'categories',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '分类浏览' }
                }, {
                    path: 'tags',
                    name: 'tags',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '标签浏览' }
                }, {
                    path: 'search',
                    name: 'search',
                    component: () => import('@/views/document/AdvancedSearch.vue'),
                    meta: { title: '高级搜索' }
                }, {
                    path: 'upload',
                    name: 'upload',
                    component: () => import('@/views/document/UploadDocument.vue'),
                    meta: { title: '上传文档' }
                }, {
                    path: 'user/center',
                    name: 'user-center',
                    component: () => import('@/views/profile/Profile.vue'),
                    meta: { title: '个人中心' }
                }, {
                    path: 'user/uploaded',
                    name: 'user-uploaded',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '我的上传' }
                }, {
                    path: 'user/favorites',
                    name: 'user-favorites',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '我的收藏' }
                }, {
                    path: 'user/history',
                    name: 'user-history',
                    component: () => import('@/views/document/DocumentList.vue'),
                    meta: { title: '浏览历史' }
                }, {
                    path: 'user/profile',
                    name: 'user-profile',
                    component: () => import('@/views/profile/ProfilePage.vue'),
                    meta: { title: '个人资料' }
                }, {
                    path: 'doc/kg-explore',
                    name: 'kg-explore',
                    component: () => import('@/views/document/KgExplore.vue'),
                    meta: { title: '知识图谱探索' }
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    console.log('路由守卫执行，当前路径:', to.fullPath);
    const isUnauthorized = unauthorized()
    console.log('是否未授权:', isUnauthorized);

    if (to.path.startsWith('/welcome') || to.path === '/') {
        if (!isUnauthorized) {
            console.log('已登录，跳转到首页');
            next('/home')
        } else {
            console.log('正常导航');
            next()
        }
    } else if (isUnauthorized) {
        console.log('未登录，跳转到登录页');
        next('/welcome')
    } else {
        console.log('正常导航');
        next()
    }
})

export default router
