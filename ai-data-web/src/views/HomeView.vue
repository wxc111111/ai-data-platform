<template>
  <main class="home-page">
    <header class="home-header">
      <div>
        <span class="home-eyebrow">AI Data Platform</span>
        <h1>登录成功</h1>
      </div>
      <el-button @click="handleLogout">退出</el-button>
    </header>

    <section class="home-content">
      <h2>{{ authStore.user?.nickname || authStore.user?.username }}</h2>
      <p>后台功能页面将在接口完善后继续建设。</p>
    </section>
  </main>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  authStore.logout()
  await router.push('/login')
}
</script>
