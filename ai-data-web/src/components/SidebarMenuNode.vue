<template>
  <el-sub-menu v-if="hasChildren" :index="menuIndex">
    <template #title>
      <el-icon>
        <component :is="resolvedIcon" />
      </el-icon>
      <span>{{ item.permissionName }}</span>
    </template>
    <SidebarMenuNode v-for="child in item.children" :key="child.id" :item="child" />
  </el-sub-menu>

  <el-menu-item v-else :index="menuIndex">
    <el-icon>
      <component :is="resolvedIcon" />
    </el-icon>
    <span>{{ item.permissionName }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import type { AuthMenuItem } from '@/api/auth'

defineOptions({
  name: 'SidebarMenuNode'
})

const props = defineProps<{
  item: AuthMenuItem
}>()

const iconMap = ElementPlusIcons as Record<string, Component>

// 菜单索引用路由路径优先，分组菜单没有路由时用权限节点 ID 保持唯一。
const menuIndex = computed(() => props.item.routePath || `menu-${props.item.id}`)

// 后端只返回当前用户可见的菜单子树，前端仅按是否有子节点决定渲染层级。
const hasChildren = computed(() => props.item.children.length > 0)

// 图标名称来自后端权限表，缺失或未注册时使用通用菜单图标兜底。
const resolvedIcon = computed(() => {
  if (props.item.icon && iconMap[props.item.icon]) {
    return iconMap[props.item.icon]
  }

  return iconMap.Menu
})
</script>
