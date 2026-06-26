<template>
  <main class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-logo">
        <span>AI</span>
        <strong>数据服务中台</strong>
      </div>
      <el-menu router :default-active="activeMenu">
        <SidebarMenuNode v-for="menu in menus" :key="menu.id" :item="menu" />
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import SidebarMenuNode from '@/components/SidebarMenuNode.vue'
import { fetchCurrentMenus, type AuthMenuItem } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const menus = ref<AuthMenuItem[]>([])

// 根据当前路由高亮后端返回的菜单节点。
const activeMenu = computed(() => route.path)

onMounted(() => {
  void loadMenus()
})

// 从后端加载当前用户有权限的菜单，避免前端维护菜单权限白名单。
async function loadMenus() {
  try {
    menus.value = await fetchCurrentMenus()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '获取菜单失败')
  }
}

// 退出前要求用户二次确认，避免误点导致当前会话被清理。
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前登录吗？', '退出确认', {
      confirmButtonText: '确认退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  authStore.logout()
  await router.push('/login')
}
</script>
