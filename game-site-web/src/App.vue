<template>
  <div class="app-shell">
    <div class="app-bg"></div>
    <main class="app-main">
      <header class="app-nav">
        <div class="nav-brand">
          <div class="nav-logo"></div>
          <div>
            <div class="nav-title">游戏网站</div>
            <div class="nav-sub">发现 · 试玩 · 管理</div>
          </div>
        </div>
        <div class="nav-actions">
          <router-link class="nav-link" to="/">首页</router-link>
          <template v-if="isAdmin">
            <router-link class="nav-link" to="/admin/games">后台管理</router-link>
          </template>
          <el-button
            v-else
            type="primary"
            link
            size="small"
            @click="goLoginWithNotice"
          >
            管理员登录
          </el-button>
          <el-button
            v-if="!isLoggedIn"
            type="primary"
            size="small"
            @click="goLogin"
          >
            登录
          </el-button>
        </div>
      </header>

      <section class="app-surface">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = ref(null)

const isLoggedIn = computed(() => !!user.value)
const isAdmin = computed(() => String(user.value?.role || '').toLowerCase() === 'admin')

function readUser() {
  try {
    const cached = localStorage.getItem('userInfo')
    user.value = cached ? JSON.parse(cached) : null
  } catch (e) {
    user.value = null
  }
}

function goLogin() {
  router.push('/login')
}

function goLoginWithNotice() {
  ElMessage.info('后台需要管理员账号登录')
  goLogin()
}

onMounted(() => {
  readUser()
  window.addEventListener('storage', (evt) => {
    if (evt.key === 'userInfo' || evt.key === 'isLoggedIn') {
      readUser()
    }
  })
})
</script>

<style scoped>
.app-shell {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(91, 140, 255, 0.08), rgba(255, 255, 255, 0));
}

.app-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 10%, rgba(138, 91, 255, 0.08), transparent 35%),
    radial-gradient(circle at 80% 0%, rgba(75, 107, 255, 0.08), transparent 32%);
  filter: blur(20px);
  pointer-events: none;
}

.app-main {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 20px 48px;
}

.app-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 12px 45px rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  position: sticky;
  top: 16px;
  z-index: 10;
  backdrop-filter: blur(10px);
}

.nav-brand {
  display: flex;
  gap: 12px;
  align-items: center;
  user-select: none;
}

.nav-logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #5b8cff, #8a5bff);
  box-shadow: 0 10px 26px rgba(91, 140, 255, 0.35);
}

.nav-title {
  font-weight: 800;
  font-size: 17px;
}

.nav-sub {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  font-weight: 600;
}

.nav-link {
  padding: 8px 12px;
  border-radius: 10px;
  color: #0f172a;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: rgba(91, 140, 255, 0.12);
  color: #3b5bff;
}

.app-surface {
  margin-top: 24px;
  background: #ffffff;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 16px 60px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

@media (max-width: 820px) {
  .app-nav {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .nav-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
