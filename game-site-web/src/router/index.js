import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";
import Home from "../views/Home.vue";
import GameDetail from "../views/GameDetail.vue";
import GamesAdmin from "../views/admin/GamesAdmin.vue";
import Login from '../views/Login.vue';

const routes = [
    { path: "/", component: Home },
    { path: "/games/:id", component: GameDetail },
    { path: "/admin/games", component: GamesAdmin, meta: { requiresAuth: true, requiresAdmin: true } },
    { path: "/login", component: Login },
    { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 路由守卫：检查用户是否已登录以及管理员权限
router.beforeEach((to, from, next) => {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    const storedUser = localStorage.getItem('userInfo');
    let role = '';
    try {
        role = storedUser ? (JSON.parse(storedUser)?.role || '') : '';
    } catch (e) {
        role = '';
    }
    const isAdmin = String(role).toLowerCase() === 'admin';

    if (to.meta.requiresAuth && !isLoggedIn) {
        next('/login');
        return;
    }

    if (to.meta.requiresAdmin && !isAdmin) {
        if (!isLoggedIn) {
            ElMessage.info('请先登录管理员账号再进入后台');
            next('/login');
        } else {
            ElMessage.warning('仅管理员可进入后台管理');
            next('/');
        }
        return;
    }

    next();
});

export default router;
