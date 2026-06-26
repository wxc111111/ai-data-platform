<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>菜单、权限维护</h2>
        <p>维护菜单、按钮和接口权限，角色授权时从这里选择。</p>
      </div>
      <el-button
        v-if="authStore.hasPermission('system:permission:add')"
        type="primary"
        :icon="Plus"
        @click="openCreateDialog()"
      >
        新增菜单/权限
      </el-button>
    </header>

    <el-table
      v-loading="loading"
      :data="permissions"
      class="data-table"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="permissionName" label="菜单/权限名称" min-width="180" />
      <el-table-column prop="permissionCode" label="权限编码" min-width="210" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag>{{ typeText(row.permissionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="routePath" label="路由" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sortNo" label="排序" width="80" />
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('system:permission:add')"
            link
            type="primary"
            :icon="Plus"
            @click="openCreateDialog(row)"
          >
            新增下级
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:permission:update')"
            link
            type="primary"
            :icon="Edit"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:permission:update')"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:permission:delete')"
            link
            type="danger"
            :icon="Delete"
            @click="removePermission(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="permissionDialogVisible" :title="editingPermission ? '编辑菜单/权限' : '新增菜单/权限'" width="620px">
      <el-form ref="permissionFormRef" :model="permissionForm" :rules="permissionRules" label-width="96px">
        <el-form-item label="父级节点">
          <el-tree-select
            v-model="permissionForm.parentId"
            :data="parentOptions"
            :props="{ label: 'permissionName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="根节点"
          />
        </el-form-item>
        <el-form-item label="名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" />
        </el-form-item>
        <el-form-item label="类型" prop="permissionType">
          <el-segmented v-model="permissionForm.permissionType" :options="permissionTypeOptions" />
        </el-form-item>
        <el-form-item label="权限编码">
          <el-input v-model="permissionForm.permissionCode" placeholder="例如 system:user:list" />
        </el-form-item>
        <el-form-item label="前端路由">
          <el-input v-model="permissionForm.routePath" placeholder="例如 /home/users" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="permissionForm.componentPath" placeholder="例如 views/UserManageView.vue" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="permissionForm.icon" placeholder="例如 User" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permissionForm.sortNo" :min="0" :max="999999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="permissionForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  createPermission,
  deletePermission,
  fetchPermissionTree,
  updatePermission,
  updatePermissionStatus,
  type PermissionItem,
  type PermissionPayload
} from '@/api/permissions'

const loading = ref(false)
const submitting = ref(false)
const permissions = ref<PermissionItem[]>([])
const permissionDialogVisible = ref(false)
const editingPermission = ref<PermissionItem | null>(null)
const permissionFormRef = ref<FormInstance>()
const authStore = useAuthStore()

const permissionTypeOptions = [
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
  { label: '接口', value: 'API' }
]

const permissionForm = reactive<PermissionPayload>({
  parentId: '0',
  permissionName: '',
  permissionCode: '',
  permissionType: 'API',
  routePath: '',
  componentPath: '',
  icon: '',
  sortNo: 0,
  status: 1
})

const permissionRules: FormRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

// 编辑时过滤当前节点，避免父级选择自己。
const parentOptions = computed(() => filterSelf(permissions.value, editingPermission.value?.id))

// 页面初始化加载权限树。
onMounted(() => {
  void loadPermissions()
})

// 加载权限树。
async function loadPermissions() {
  loading.value = true
  try {
    permissions.value = await fetchPermissionTree()
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗，可从某个节点下新增子权限。
function openCreateDialog(parent?: PermissionItem) {
  editingPermission.value = null
  Object.assign(permissionForm, {
    parentId: parent?.id || '0',
    permissionName: '',
    permissionCode: '',
    permissionType: 'API',
    routePath: '',
    componentPath: '',
    icon: '',
    sortNo: 0,
    status: 1
  })
  permissionDialogVisible.value = true
}

// 打开编辑弹窗并回填权限数据。
function openEditDialog(permission: PermissionItem) {
  editingPermission.value = permission
  Object.assign(permissionForm, {
    parentId: permission.parentId || '0',
    permissionName: permission.permissionName,
    permissionCode: permission.permissionCode || '',
    permissionType: permission.permissionType,
    routePath: permission.routePath || '',
    componentPath: permission.componentPath || '',
    icon: permission.icon || '',
    sortNo: permission.sortNo,
    status: permission.status
  })
  permissionDialogVisible.value = true
}

// 提交新增或编辑权限。
async function submitPermission() {
  await permissionFormRef.value?.validate()
  submitting.value = true
  try {
    const payload = { ...permissionForm, parentId: permissionForm.parentId || '0' }
    if (editingPermission.value) {
      await updatePermission(editingPermission.value.id, payload)
    } else {
      await createPermission(payload)
    }
    ElMessage.success('保存成功')
    permissionDialogVisible.value = false
    await loadPermissions()
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

// 启用或禁用权限，后端会刷新相关用户缓存。
async function toggleStatus(permission: PermissionItem) {
  const nextStatus = permission.status === 1 ? 0 : 1
  try {
    await updatePermissionStatus(permission.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadPermissions()
  } catch (error) {
    showError(error)
  }
}

// 删除权限前确认，存在子权限或角色授权时后端会拒绝。
async function removePermission(permission: PermissionItem) {
  try {
    await ElMessageBox.confirm(`确认删除权限 ${permission.permissionName}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deletePermission(permission.id)
    ElMessage.success('已删除')
    await loadPermissions()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error)
    }
  }
}

// 递归过滤当前节点，防止编辑时选择自身作为父级。
function filterSelf(items: PermissionItem[], selfId?: string): PermissionItem[] {
  return items
    .filter((item) => item.id !== selfId)
    .map((item) => ({
      ...item,
      children: filterSelf(item.children || [], selfId)
    }))
}

// 权限类型展示文案。
function typeText(type: PermissionItem['permissionType']) {
  return permissionTypeOptions.find((option) => option.value === type)?.label || type
}

// 统一展示业务错误。
function showError(error: unknown) {
  const message = error instanceof Error ? error.message : '操作失败'
  ElMessage.error(message)
}
</script>
