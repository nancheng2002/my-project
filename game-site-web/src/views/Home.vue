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
        <el-button text @click="goLogin">登录</el-button>
        <el-button :type="isLoggedIn ? 'primary' : 'success'" @click="goToAdmin">
          {{ isLoggedIn ? '进入后台' : '登录后管理' }}
        </el-button>
      </template>
    </AppTopbar>

    <section class="hero">
      <div class="hero-text">
        <div class="pill">用视觉包装你的创意</div>
        <h1>发现好玩的游戏，也能在这里优雅地管理它们</h1>
        <p class="hero-desc">
          主页在登录前也充满内容：精选游戏、灵感卡片、灵活的搜索入口。
          登录后即可进入后台，上传作品、编辑素材，让你的游戏更出彩。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="explore">浏览精选</el-button>
          <el-button size="large" @click="goToAdmin">后台管理</el-button>
        </div>
        <div class="stats">
          <div v-for="stat in stats" :key="stat.label" class="stat-item">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
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
          <p class="card-title">灵感集锦</p>
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
          <h2>首页展示不再空白，让灵感先行</h2>
          <p class="section-desc">登录前即可看到精选内容，登录后才能管理后台数据。</p>
        </div>
        <el-button type="primary" @click="goToAdmin">立即登录管理</el-button>
      </div>
      <div class="game-grid">
        <article v-for="game in curatedGames" :key="game.title" class="game-card">
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

    <section class="section cta">
      <div class="cta-inner">
        <div>
          <p class="section-eyebrow">需要管理？</p>
          <h2>登录后即可进入后台，发布或维护你的游戏</h2>
          <p class="section-desc">
            统一的后台界面便于你管理素材、更新文案、查看搜索表现。
            现在就登录，让首页与后台一起焕然一新。
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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppTopbar from '../components/AppTopbar.vue'

const router = useRouter()
const keyword = ref('')
const isLoggedIn = ref(false)

const stats = [
  { label: '优选游戏', value: '32+' },
  { label: '高分评价', value: '4.8/5' },
  { label: '创作者', value: '10+ 团队' },
]

const highlights = [
  { title: '灵感合集', desc: '给首页添加更多故事感的展示卡片。', emoji: '✨', color: 'linear-gradient(135deg, #ffefd5, #ffd7e5)' },
  { title: '轻盈留白', desc: '留白与色块结合，让视线更聚焦。', emoji: '🎨', color: 'linear-gradient(135deg, #dff1ff, #e6e9ff)' },
  { title: '后台守护', desc: '未登录前也能浏览，但后台必须登录。', emoji: '🛡️', color: 'linear-gradient(135deg, #e2fbd7, #e4f4ff)' },
]

const curatedGames = [
  { title: '星际旅人', desc: '穿越星海收集能量碎片，体验沉浸式音画。', tags: ['宇宙', '叙事', '探索'], mood: '沉浸', cover: 'linear-gradient(135deg, #6b8bff, #b07bff)' },
  { title: '霓虹疾驰', desc: '赛博朋克风格的街头追逐，用速度点燃夜色。', tags: ['动作', '竞速', '合成波'], mood: '速度', cover: 'linear-gradient(135deg, #ff8fb1, #ffb18f)' },
  { title: '奇想花园', desc: '解谜与模拟经营结合，打造你的奇趣花园。', tags: ['解谜', '经营', '轻松'], mood: '治愈', cover: 'linear-gradient(135deg, #7ddac7, #6fb1ff)' },
  { title: '古堡迷踪', desc: '在重重机关与暗道中找出真相，考验策略思维。', tags: ['策略', '解谜', '悬疑'], mood: '策略', cover: 'linear-gradient(135deg, #f0d776, #f7a07c)' },
  { title: '像素勇者', desc: '复古像素风冒险，和伙伴一起打怪升级。', tags: ['像素', '冒险', '合作'], mood: '经典', cover: 'linear-gradient(135deg, #6fd0ff, #8d99ff)' },
  { title: '光影之诗', desc: '在光影交织的舞台上解锁音乐节奏谜题。', tags: ['音乐', '节奏', '艺术'], mood: '律动', cover: 'linear-gradient(135deg, #ffd6e8, #c5c7ff)' },
]

const onSearch = (kw) => {
  keyword.value = kw
  if (kw) {
    ElMessage({ type: 'success', message: `已为你预备搜索：${kw}` })
  }
}

const goToAdmin = () => {
  if (isLoggedIn.value) {
    router.push('/admin/games')
  } else {
    ElMessage.info('请先登录后再进入后台管理')
    router.push('/login')
  }
}

const goLogin = () => {
  router.push('/login')
}

const explore = () => {
  const el = document.getElementById('game-grid')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}

onMounted(() => {
  isLoggedIn.value = localStorage.getItem('isLoggedIn') === 'true'
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
