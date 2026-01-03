<template>
  <div class="home">
    <AppTopbar
      title="游戏网站"
      sub-title="精选游戏 · 一站式管理"
      :show-search="true"
      :keyword="keyword"
      search-placeholder="搜索游戏、标签或作者"
      @search="onSearch"
      @update:keyword="(v) => (keyword = v)"
    >
      <template #actions>
        <el-button v-if="!isLoggedIn" text @click="goLogin">登录</el-button>
        <el-button v-else text type="danger" @click="logout">退出登录</el-button>
        <el-button :type="isAdmin ? 'primary' : 'success'" @click="goToAdmin">
          {{ isAdmin ? '进入后台' : '登录后管理' }}
        </el-button>
      </template>
    </AppTopbar>

    <section class="hero">
      <div class="hero-text">
        <div class="pill">真实游戏 · 数据打通</div>
        <h1>王者荣耀、原神等热门游戏信息，登录后马上可查</h1>
        <p class="hero-desc">
          首页在登录前就能看到精选内容，但所有交互都会校验登录状态。
          登录后才能打开后台模块，且只有管理员账号可以进入后台管理。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="() => requireLogin('登录后才能查看详情', explore)">
            浏览精选
          </el-button>
          <el-button size="large" @click="goToAdmin">后台管理</el-button>
        </div>
        <div class="stats">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalGames }}</div>
            <div class="stat-label">数据库游戏总数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.publishedGames }}</div>
            <div class="stat-label">上架游戏</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.categories }}</div>
            <div class="stat-label">分类数量</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ stats.offlineGames }}</div>
            <div class="stat-label">下架/审核中</div>
          </div>
        </div>
      </div>
      <div class="hero-card">
        <div class="card-header">
          <div class="dot" />
          <div class="dot" />
          <div class="dot" />
        </div>
        <div class="card-body">
          <p class="card-title">实时灵感卡片</p>
          <div class="card-grid">
            <div v-for="feature in highlights" :key="feature.title" class="feature-card">
              <div class="feature-icon" :style="{ background: feature.color }">{{ feature.emoji }}</div>
              <div>
                <p class="feature-title">{{ feature.title }}</p>
                <p class="feature-desc">{{ feature.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section id="game-grid" class="section">
      <div class="section-head">
        <div>
          <p class="section-eyebrow">精选推荐</p>
          <h2>真实游戏卡片（从数据库推荐）</h2>
          <p class="section-desc">点击任何卡片都会校验登录，未登录会先跳转到登录页。</p>
        </div>
        <el-button type="primary" @click="goToAdmin">立即登录管理</el-button>
      </div>
      <div class="game-grid">
        <article
          v-for="game in curatedGames"
          :key="game.title"
          class="game-card"
          @click="() => handleGameClick(game)"
        >
          <div class="game-thumb" :style="{ background: game.cover }">
            <div class="tag">{{ game.mood }}</div>
          </div>
          <div class="game-meta">
            <h3>{{ game.title }}</h3>
            <p class="game-desc">{{ game.desc }}</p>
            <div class="tags">
              <span v-for="tag in game.tags" :key="tag" class="chip">{{ tag }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>

    <section class="section admin-preview" id="console">
      <div class="section-head">
        <div>
          <p class="section-eyebrow">后台模块预览</p>
          <h2>左侧按模块分栏，右侧展示实时数据与图表</h2>
          <p class="section-desc">所有模块点击都会要求登录，后台入口仅管理员可进入。</p>
        </div>
        <el-button type="primary" @click="goToAdmin">进入后台</el-button>
      </div>
      <div class="preview-body">
        <aside class="preview-menu">
          <div
            v-for="module in managementModules"
            :key="module.key"
            class="menu-item"
            :class="{ active: activeModule === module.key }"
            @click="() => selectModule(module.key)"
          >
            <div class="menu-title">{{ module.title }}</div>
            <div class="menu-desc">{{ module.desc }}</div>
          </div>
        </aside>

        <div class="preview-panel">
          <div class="panel-head">
            <div>
              <p class="section-eyebrow">{{ activeModuleMeta.title }}</p>
              <h3>{{ activeModuleMeta.subtitle }}</h3>
              <p class="panel-desc">{{ activeModuleMeta.tips }}</p>
            </div>
            <el-tag :type="isAdmin ? 'success' : 'info'">{{ isAdmin ? '管理员' : '登录用户预览' }}</el-tag>
          </div>

          <div class="metrics-grid">
            <div class="metric-card">
              <p class="metric-label">游戏总数</p>
              <p class="metric-value">{{ stats.totalGames }}</p>
              <p class="metric-sub">含王者荣耀、原神等真实数据</p>
            </div>
            <div class="metric-card">
              <p class="metric-label">已上架</p>
              <p class="metric-value highlight">{{ stats.publishedGames }}</p>
              <p class="metric-sub">可在首页展示并推荐</p>
            </div>
            <div class="metric-card">
              <p class="metric-label">分类</p>
              <p class="metric-value">{{ stats.categories }}</p>
              <p class="metric-sub">结合 Game Category 表实时统计</p>
            </div>
            <div class="metric-card">
              <p class="metric-label">审核/下架</p>
              <p class="metric-value">{{ stats.offlineGames }}</p>
              <p class="metric-sub">需要管理员处理</p>
            </div>
          </div>

          <div class="chart-row">
            <div class="chart-card">
              <div class="chart-title">按分类分布</div>
              <div class="bars">
                <div v-for="item in categoryBars" :key="item.label" class="bar-line">
                  <span class="bar-label">{{ item.label }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: item.width, background: item.color }"></div>
                  </div>
                  <span class="bar-value">{{ item.value }}</span>
                </div>
              </div>
            </div>
            <div class="chart-card">
              <div class="chart-title">按平台分布</div>
              <div class="platform-chips">
                <div v-for="item in platformBreakdown" :key="item.label" class="chip-tile">
                  <div class="chip-count">{{ item.value }}</div>
                  <div class="chip-label">{{ item.label }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="list-card">
            <div class="list-head">
              <div>
                <div class="chart-title">最近更新的游戏</div>
                <p class="panel-desc">点击行会校验登录并尝试打开对应详情</p>
              </div>
              <el-button text type="primary" @click="goToAdmin">去后台编辑</el-button>
            </div>
            <div class="recent-list">
              <div
                v-for="game in recentGames"
                :key="game.title + game.updatedAt"
                class="recent-item"
                @click="() => handleGameClick(game)"
              >
                <div class="recent-meta">
                  <div class="recent-title">{{ game.title }}</div>
                  <div class="recent-sub">{{ game.category }} · {{ game.platform || '未知平台' }}</div>
                </div>
                <div class="recent-time">{{ game.updatedAt || '刚刚' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section cta">
      <div class="cta-inner">
        <div>
          <p class="section-eyebrow">需要管理？</p>
          <h2>登录后即可进入后台，发布或维护你的游戏</h2>
          <p class="section-desc">
            首页所有交互都会引导到登录，后台操作仅管理员可用。
            游戏数据、分类、平台分布全部来自数据库实时读取。
          </p>
        </div>
        <div class="cta-actions">
          <el-button type="primary" size="large" @click="goLogin">去登录</el-button>
          <el-button size="large" @click="goToAdmin">进入后台</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppTopbar from '../components/AppTopbar.vue'
import { homeApi } from '@/api/home'

const router = useRouter()
const keyword = ref('')
const isLoggedIn = ref(false)
const userInfo = ref(null)
const isAdmin = computed(() => String(userInfo.value?.role || '').toLowerCase() === 'admin')

const stats = reactive({
  totalGames: 0,
  publishedGames: 0,
  offlineGames: 0,
  categories: 0
})

const categoryBreakdown = ref([])
const platformBreakdown = ref([])
const recentGames = ref([])
const curatedGames = ref([])
const activeModule = ref('dashboard')

const managementModules = [
  { key: 'dashboard', title: '仪表盘 (Dashboard)', desc: '活跃度、更新频率与趋势概览' },
  { key: 'games', title: '游戏管理 (Game Management)', desc: '增删改查，查看真实游戏数据' },
  { key: 'users', title: '用户管理 (User Management)', desc: '查看注册、登录与角色' },
  { key: 'orders', title: '订单管理 (Order Management)', desc: '订单状态、支付漏斗' },
  { key: 'finance', title: '财务管理 (Finance Management)', desc: '收入支出与提现' },
  { key: 'content', title: '内容管理 (Content Management)', desc: '文章资讯与活动公告' },
  { key: 'events', title: '活动管理 (Event Management)', desc: '活动参与与奖励发放' },
  { key: 'permission', title: '权限管理 (Permission Management)', desc: '管理员、客服、财务的权限粒度' },
  { key: 'settings', title: '设置 (Settings)', desc: '系统、游戏、通知配置' },
  { key: 'support', title: '帮助与支持 (Help & Support)', desc: 'FAQ、反馈、工单' }
]

const highlights = [
  { title: '热门游戏直接取数', desc: '首页卡片展示的是数据库中的游戏记录。', emoji: '🎮', color: 'linear-gradient(135deg, #ffe6b8, #ffd3e2)' },
  { title: '模块化管理', desc: '左侧导航列出全部后台栏目，随时切换。', emoji: '🧭', color: 'linear-gradient(135deg, #dff1ff, #e6e9ff)' },
  { title: '权限校验', desc: '所有点击都先校验登录，后台只允许管理员。', emoji: '✅', color: 'linear-gradient(135deg, #d9f4e7, #e5f8ff)' },
  { title: '实时统计', desc: '分类/平台分布、最近更新全部来自数据库。', emoji: '📈', color: 'linear-gradient(135deg, #e0e7ff, #f3e8ff)' },
]

const moduleMetaMap = {
  dashboard: {
    title: '仪表盘 (Dashboard)',
    subtitle: '关键数据、活跃趋势、最近游戏动态',
    tips: '下方图表基于游戏表与分类表实时统计，可作为后台仪表盘的示例。'
  },
  games: {
    title: '游戏管理 (Game Management)',
    subtitle: '真实游戏列表与管理入口',
    tips: '点击任意游戏都会先校验登录，再根据权限进入详情或后台编辑。'
  },
  users: {
    title: '用户管理 (User Management)',
    subtitle: '角色、登录时间、封禁状态',
    tips: '登录状态与角色会写入 localStorage，并在路由守卫中校验权限。'
  },
  orders: {
    title: '订单管理 (Order Management)',
    subtitle: '支付状态、查询筛选与漏斗',
    tips: '可在此扩展支付/订单相关接口，示例保持导航与权限流程。'
  },
  finance: {
    title: '财务管理 (Finance Management)',
    subtitle: '收入支出、提现审核',
    tips: '配合权限管理限制敏感操作，只允许管理员查看。'
  },
  content: {
    title: '内容管理 (Content Management)',
    subtitle: '文章、资讯与公告的发布与管理',
    tips: '可直接复用游戏管理的表单/列表交互，保持一致的体验。'
  },
  events: {
    title: '活动管理 (Event Management)',
    subtitle: '活动报名、奖励发放与效果分析',
    tips: '结合分类/平台统计，评估活动对不同渠道的影响。'
  },
  permission: {
    title: '权限管理 (Permission Management)',
    subtitle: '管理员、客服、财务等角色配置',
    tips: '后台路由带 requiresAdmin 元信息，非管理员自动拦截。'
  },
  settings: {
    title: '设置 (Settings)',
    subtitle: '系统、游戏、邮件通知等配置',
    tips: '建议将全局配置拆为分组表单，复用 Element Plus 组件。'
  },
  support: {
    title: '帮助与支持 (Help & Support)',
    subtitle: 'FAQ、反馈与工单',
    tips: '支持中心入口同样要求登录，避免匿名滥用。'
  }
}

const fallbackGames = [
  {
    id: null,
    title: '王者荣耀',
    desc: '腾讯 MOBA 经典，实时对战、排位上分、峡谷战报。',
    tags: ['MOBA', '竞技', '5V5'],
    mood: '热门',
    cover: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  },
  {
    id: null,
    title: '原神',
    desc: '开放世界冒险，提瓦特大陆的沉浸探索与元素战斗。',
    tags: ['开放世界', '冒险', '多人'],
    mood: '冒险',
    cover: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)'
  },
  {
    id: null,
    title: '绝地求生',
    desc: '战术竞技射击，跳伞、搜刮、吃鸡的紧张节奏。',
    tags: ['射击', '战术', '团队'],
    mood: '竞技',
    cover: 'linear-gradient(135deg, #cfd9df 0%, #f5f7fa 100%)'
  },
  {
    id: null,
    title: '英雄联盟',
    desc: '英雄池深度、策略运营、全球顶级电竞赛事。',
    tags: ['MOBA', '电竞', '策略'],
    mood: '经典',
    cover: 'linear-gradient(135deg, #d4fc79 0%, #96e6a1 100%)'
  }
]

function parseUser() {
  isLoggedIn.value = localStorage.getItem('isLoggedIn') === 'true'
  try {
    const cached = localStorage.getItem('userInfo')
    userInfo.value = cached ? JSON.parse(cached) : null
  } catch (e) {
    userInfo.value = null
  }
}

function requireLogin(message, cb) {
  if (!isLoggedIn.value) {
    ElMessage.info(message || '请先登录')
    router.push('/login')
    return
  }
  cb?.()
}

const onSearch = (kw) => {
  requireLogin('登录后才能搜索游戏', () => {
    keyword.value = kw
    if (kw) {
      ElMessage({ type: 'success', message: `已为你预备搜索：${kw}` })
    }
  })
}

const goToAdmin = () => {
  if (!isLoggedIn.value) {
    ElMessage.info('请先登录后再进入后台管理')
    router.push('/login')
    return
  }
  if (!isAdmin.value) {
    ElMessage.warning('当前登录账号不是管理员，无法进入后台')
    return
  }
  router.push('/admin/games')
}

const goLogin = () => {
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('isLoggedIn')
  localStorage.removeItem('userInfo')
  parseUser()
  ElMessage.success('已退出登录')
}

const explore = () => {
  const el = document.getElementById('game-grid')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

function selectModule(key) {
  requireLogin('请先登录后再浏览各后台栏目', () => {
    activeModule.value = key
  })
}

function handleGameClick(game) {
  requireLogin('请先登录后再查看游戏详情', () => {
    if (game.id) {
      router.push(`/games/${game.id}`)
    } else {
      ElMessage.info('示例卡片展示，登录后可查看数据库中的真实游戏详情')
    }
  })
}

function normalizeCover(raw) {
  if (!raw) return 'linear-gradient(135deg, #e0e7ff, #f8fafc)'
  if (raw.startsWith('http') || raw.startsWith('/')) return `url(${raw})`
  return raw
}

async function loadRecommend() {
  try {
    const list = await homeApi.recommend()
    if (Array.isArray(list) && list.length) {
      curatedGames.value = list.map((g, idx) => ({
        id: g.id,
        title: g.title,
        desc: g.intro || '点击可进入详情或后台管理',
        tags: [g.platform || '多平台', g.categoryId ? `分类 #${g.categoryId}` : '未分类'],
        mood: idx % 2 === 0 ? '推荐' : '热门',
        cover: normalizeCover(g.coverUrl)
      }))
      return
    }
    curatedGames.value = fallbackGames
  } catch (e) {
    curatedGames.value = fallbackGames
  }
}

async function loadDashboard() {
  try {
    const data = await homeApi.dashboard()
    stats.totalGames = Number(data?.totalGames || 0)
    stats.publishedGames = Number(data?.publishedGames || 0)
    stats.offlineGames = Number(data?.offlineGames || 0)
    stats.categories = Number(data?.categories || 0)

    categoryBreakdown.value = Array.isArray(data?.categoryBreakdown) ? data.categoryBreakdown : []
    platformBreakdown.value = Array.isArray(data?.platformBreakdown) ? data.platformBreakdown : []
    recentGames.value = Array.isArray(data?.recentGames) ? data.recentGames : []

    // 如果接口返回 featured，就用它覆盖推荐区
    if (Array.isArray(data?.featured) && data.featured.length) {
      curatedGames.value = data.featured.map((g, idx) => ({
        id: g.id,
        title: g.title,
        desc: g.platform ? `${g.platform} · 真实数据库记录` : '数据库中录入的游戏',
        tags: [g.platform || '多平台', g.categoryId ? `分类 #${g.categoryId}` : '未分类'],
        mood: idx % 2 === 0 ? '精选' : '推荐',
        cover: normalizeCover(g.coverUrl)
      }))
    }
  } catch (e) {
    // 保持默认值，便于展示
  }
}

const categoryBars = computed(() => {
  const list = categoryBreakdown.value || []
  if (!list.length) return []
  const max = Math.max(...list.map((x) => Number(x.value || 0))) || 1
  const palette = ['#5b8cff', '#8a5bff', '#5be5ff', '#50c878', '#f59e0b']
  return list.map((item, idx) => ({
    ...item,
    width: `${(Number(item.value || 0) / max) * 100}%`,
    color: palette[idx % palette.length]
  }))
})

const activeModuleMeta = computed(() => moduleMetaMap[activeModule.value] || moduleMetaMap.dashboard)

onMounted(async () => {
  parseUser()
  await loadDashboard()
  await loadRecommend()
})
</script>

<style scoped>
.home {
  padding: 0 28px 40px;
}

.hero {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 26px;
  padding: 28px;
  background: linear-gradient(135deg, rgba(91, 140, 255, 0.12), rgba(255, 255, 255, 0));
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.hero-text h1 {
  margin: 10px 0 12px;
  font-size: clamp(26px, 4vw, 34px);
  line-height: 1.3;
}

.hero-desc {
  color: #475569;
  margin: 0 0 18px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 14px;
}

.stat-item {
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  padding: 12px 14px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.stat-value {
  font-size: 20px;
  font-weight: 800;
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.hero-card {
  background: #0f172a;
  border-radius: 18px;
  color: #e2e8f0;
  box-shadow: 0 16px 55px rgba(15, 23, 42, 0.3);
  overflow: hidden;
}

.card-header {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #94a3b8;
}

.card-body {
  padding: 18px;
}

.card-title {
  margin: 0 0 10px;
  color: #cbd5e1;
  letter-spacing: 0.5px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.feature-card {
  display: flex;
  gap: 12px;
  align-items: center;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 12px;
}

.feature-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-size: 18px;
}

.feature-title {
  margin: 0;
  font-weight: 700;
  color: #f8fafc;
}

.feature-desc {
  margin: 4px 0 0;
  color: #cbd5e1;
  font-size: 13px;
}

.section {
  padding: 28px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.section-eyebrow {
  margin: 0;
  color: #4b6bff;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.section h2 {
  margin: 4px 0 8px;
}

.section-desc {
  margin: 0;
  color: #475569;
}

.game-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.game-card {
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 14px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.game-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 55px rgba(15, 23, 42, 0.12);
}

.game-thumb {
  position: relative;
  height: 140px;
  background-size: cover;
  background-position: center;
}

.tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #0f172a;
  font-weight: 700;
  font-size: 12px;
}

.game-meta {
  padding: 14px;
}

.game-desc {
  margin: 6px 0 12px;
  color: #475569;
}

.tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  background: #f1f5f9;
  border-radius: 10px;
  padding: 6px 10px;
  font-size: 12px;
  color: #0f172a;
}

.admin-preview {
  background: linear-gradient(135deg, rgba(91, 140, 255, 0.07), rgba(138, 91, 255, 0.04));
  border: 1px solid rgba(15, 23, 42, 0.05);
  border-radius: 16px;
}

.preview-body {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
}

.preview-menu {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
  overflow: hidden;
}

.menu-item {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.04);
  cursor: pointer;
  transition: all 0.15s ease;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:hover {
  background: rgba(91, 140, 255, 0.08);
}

.menu-item.active {
  background: rgba(91, 140, 255, 0.12);
  border-left: 4px solid #5b8cff;
  padding-left: 12px;
}

.menu-title {
  font-weight: 700;
}

.menu-desc {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.preview-panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.08);
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.panel-desc {
  margin: 4px 0 0;
  color: #6b7280;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin: 12px 0 16px;
}

.metric-card {
  border: 1px solid rgba(15, 23, 42, 0.05);
  border-radius: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafc, #ffffff);
}

.metric-label {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.metric-value {
  margin: 6px 0 4px;
  font-size: 24px;
  font-weight: 800;
}

.metric-value.highlight {
  color: #5b8cff;
}

.metric-sub {
  margin: 0;
  color: #94a3b8;
  font-size: 12px;
}

.chart-row {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 12px;
}

.chart-card {
  border: 1px solid rgba(15, 23, 42, 0.05);
  border-radius: 12px;
  padding: 12px;
  background: #f8fafc;
}

.chart-title {
  margin: 0 0 10px;
  font-weight: 700;
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-line {
  display: grid;
  grid-template-columns: 120px 1fr 40px;
  align-items: center;
  gap: 8px;
}

.bar-label {
  font-size: 13px;
  color: #475569;
}

.bar-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
}

.bar-value {
  font-weight: 700;
  color: #0f172a;
}

.platform-chips {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 10px;
}

.chip-tile {
  background: white;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.05);
  padding: 10px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);
}

.chip-count {
  font-weight: 800;
  font-size: 20px;
}

.chip-label {
  color: #64748b;
  margin-top: 4px;
}

.list-card {
  margin-top: 12px;
  border: 1px solid rgba(15, 23, 42, 0.05);
  border-radius: 12px;
  background: #ffffff;
  padding: 12px;
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid rgba(15, 23, 42, 0.05);
  transition: all 0.15s ease;
  cursor: pointer;
}

.recent-item:hover {
  background: rgba(91, 140, 255, 0.08);
}

.recent-title {
  font-weight: 700;
}

.recent-sub {
  color: #6b7280;
  font-size: 13px;
}

.recent-time {
  color: #475569;
  font-size: 13px;
}

.cta {
  background: linear-gradient(135deg, rgba(91, 140, 255, 0.12), rgba(138, 91, 255, 0.08));
  border-top: 1px solid rgba(15, 23, 42, 0.04);
}

.cta-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.cta-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pill {
  display: inline-flex;
  padding: 6px 12px;
  background: #eef2ff;
  color: #4b6bff;
  border-radius: 999px;
  font-weight: 700;
  font-size: 13px;
}

@media (max-width: 900px) {
  .preview-body {
    grid-template-columns: 1fr;
  }

  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .home {
    padding: 0 16px 28px;
  }

  .hero,
  .section {
    padding: 20px;
  }
}
</style>
