import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import DashboardHomeView from '@/views/DashboardHomeView.vue'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import NoPermissionView from '@/views/NoPermissionView.vue'
import PermissionManageView from '@/views/PermissionManageView.vue'
import RoleManageView from '@/views/RoleManageView.vue'
import UserManageView from '@/views/UserManageView.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        public: true
      }
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      redirect: '/home/overview',
      children: [
        {
          path: 'overview',
          name: 'home-overview',
          component: DashboardHomeView
        },
        {
          path: 'users',
          name: 'users',
          component: UserManageView,
          meta: {
            permission: 'system:user:menu'
          }
        },
        {
          path: 'roles',
          name: 'roles',
          component: RoleManageView,
          meta: {
            permission: 'system:role:menu'
          }
        },
        {
          path: 'permissions',
          name: 'permissions',
          component: PermissionManageView,
          meta: {
            permission: 'system:permission:menu'
          }
        },
        {
          path: 'no-permission',
          name: 'no-permission',
          component: NoPermissionView
        }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (to.path === '/login' && authStore.isLoggedIn) {
      return '/home'
    }
    return true
  }

  if (!authStore.isLoggedIn) {
    return '/login'
  }

  // 路由级权限控制，避免无角色用户直接输入地址访问功能页面。
  const permission = to.meta.permission
  if (typeof permission === 'string' && !authStore.hasPermission(permission)) {
    return '/home/no-permission'
  }

  return true
})
