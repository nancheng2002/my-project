import { createRouter, createWebHistory } from "vue-router";
import Home from "../views/Home.vue";
import GameDetail from "../views/GameDetail.vue";
import GamesAdmin from "../views/admin/GamesAdmin.vue";
import Login from '../views/Login.vue';

const routes = [
    { path: "/", component: Home },
    { path: "/games/:id", component: GameDetail },
    { path: "/admin/games", component: GamesAdmin, meta: { requiresAuth: true } }, // 需要登录保护
    { path: "/login", component: Login },
    { path: "/:pathMatch(.*)*", redirect: "/" },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

// 路由守卫：检查用户是否已登录
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !localStorage.getItem('isLoggedIn')) {
        next('/login'); // 未登录跳转到登录页面
    } else {
        next(); // 放行
    }
});

export default router;
