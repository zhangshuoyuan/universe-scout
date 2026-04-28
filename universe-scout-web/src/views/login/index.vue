<template>
  <main class="login-page">
    <section class="intro">
      <div class="mark">US</div>
      <h1>宇宙球探</h1>
      <p>面向 NBA2K 模拟宇宙的数据资产平台</p>
      <div class="features">
        <span>图片入库</span>
        <span>批次追踪</span>
        <span>解析准备</span>
      </div>
    </section>

    <form class="login-box" @submit.prevent="handleLogin">
      <div class="form-title">
        <h2>管理员登录</h2>
        <p>使用 Day 1 初始化账号进入系统</p>
      </div>
      <label>
        <span>用户名</span>
        <input v-model="form.username" autocomplete="username" />
      </label>
      <label>
        <span>密码</span>
        <input v-model="form.password" type="password" autocomplete="current-password" />
      </label>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      <button type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '进入系统' }}
      </button>
    </form>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: 'admin',
  password: 'admin123456',
})

async function handleLogin() {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await login(form)
    if (data.code === 0 && data.data) {
      userStore.setUser(data.data.token, data.data.username, data.data.roleCode)
      await router.push('/dashboard')
      return
    }
    errorMessage.value = data.message || '登录失败'
  } catch {
    errorMessage.value = '无法连接后端服务，请先启动 Spring Boot。'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(320px, 1fr) minmax(340px, 440px);
  align-items: center;
  gap: 48px;
  padding: 56px max(32px, 8vw);
  background:
    linear-gradient(135deg, rgb(16 42 67 / 94%), rgb(29 111 184 / 86%)),
    url("https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=1600&q=80") center/cover;
}

.intro {
  color: #ffffff;
}

.mark {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  background: #f7c948;
  color: #102a43;
  border-radius: 12px;
  font-size: 20px;
  font-weight: 900;
}

h1 {
  margin: 26px 0 10px;
  font-size: 46px;
}

.intro p {
  max-width: 520px;
  margin: 0;
  color: #d9e6f2;
  font-size: 20px;
  line-height: 1.7;
}

.features {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 26px;
}

.features span {
  padding: 8px 12px;
  background: rgb(255 255 255 / 12%);
  border: 1px solid rgb(255 255 255 / 18%);
  border-radius: 999px;
  font-weight: 700;
}

.login-box {
  display: grid;
  gap: 18px;
  padding: 30px;
  background: rgb(255 255 255 / 96%);
  border: 1px solid rgb(255 255 255 / 56%);
  border-radius: 8px;
  box-shadow: 0 24px 70px rgb(8 20 35 / 28%);
}

.form-title h2 {
  margin: 0;
  font-size: 24px;
}

.form-title p {
  margin: 6px 0 0;
  color: var(--muted);
}

label {
  display: grid;
  gap: 8px;
  color: #334155;
  font-size: 14px;
  font-weight: 700;
}

input {
  height: 44px;
  padding: 0 12px;
  color: #111827;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  outline: none;
}

input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgb(29 111 184 / 14%);
}

button {
  height: 44px;
  color: #ffffff;
  background: var(--primary);
  border: 0;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

button:hover {
  background: var(--primary-dark);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.error {
  margin: 0;
  color: var(--danger);
  font-size: 14px;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 28px 18px;
  }

  h1 {
    font-size: 36px;
  }
}
</style>
