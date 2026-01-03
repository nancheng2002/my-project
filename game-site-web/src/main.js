// main.js
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

// 路由守卫，检查是否需要认证
router.beforeEach((to, from, next) => {
    if (to.meta.requiresAuth && !localStorage.getItem('isLoggedIn')) {
        // 如果需要认证但用户没有登录，则跳转到登录页面
        next('/login');
    } else {
        next();
    }
});

createApp(App).use(router).use(ElementPlus).mount('#app');
