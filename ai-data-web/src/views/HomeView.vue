<template>
  <main class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <span>AI</span>
        <strong>数据服务中台</strong>
      </div>
      <el-menu router :default-active="activeMenu">
        <el-menu-item index="/home/overview">
          <el-icon><House /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-sub-menu v-if="showSystemMenu" index="/home/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="authStore.hasPermission('system:user:menu')" index="/home/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:role:menu')" index="/home/roles">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:permission:menu')" index="/home/permissions">
            <el-icon><Key /></el-icon>
            <span>菜单权限</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:login-log:menu')" index="/home/login-logs">
            <el-icon><Document /></el-icon>
            <span>登录日志</span>
          </el-menu-item>
          <el-menu-item v-if="authStore.hasPermission('system:operation-log:menu')" index="/home/operation-logs">
            <el-icon><Tickets /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <span class="home-eyebrow">AI Data Platform</span>
          <h1>管理后台</h1>
        </div>
        <div class="admin-user">
          <span>{{ authStore.user?.nickname || authStore.user?.username }}</span>
          <el-button @click="handleLogout">退出</el-button>
        </div>
      </header>

      <RouterView />
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, House, Key, Setting, Tickets, User, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 根据当前路由高亮菜单，首页不依赖任何业务权限。
const activeMenu = computed(() => route.path)

// 系统管理是一级菜单，只要拥有任意子菜单权限就展示。
const showSystemMenu = computed(
  () =>
    authStore.hasPermission('system:user:menu') ||
    authStore.hasPermission('system:role:menu') ||
    authStore.hasPermission('system:permission:menu') ||
    authStore.hasPermission('system:login-log:menu') ||
    authStore.hasPermission('system:operation-log:menu')
)

// 清理本地登录状态并返回登录页。
async function handleLogout() {
  authStore.logout()
  await router.push('/login')
}
</script>
