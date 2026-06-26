<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>角色管理</h2>
        <p>维护后台角色、状态和后续权限授权基础数据。</p>
      </div>
      <el-button v-if="authStore.hasPermission('system:role:add')" type="primary" :icon="Plus" @click="openCreateDialog">
        新增角色
      </el-button>
    </header>

    <section class="filter-bar role-filter-bar">
      <el-input v-model="query.roleCode" clearable placeholder="角色编码" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchRoles">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-table v-loading="loading" :data="roles" class="data-table" row-key="id">
      <el-table-column prop="roleCode" label="角色编码" min-width="150" />
      <el-table-column prop="roleName" label="角色名称" min-width="160" />
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
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
            v-if="authStore.hasPermission('system:role:update')"
            link
            type="primary"
            :icon="Edit"
            :disabled="isAdminRole(row)"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:role:update')"
            link
            type="primary"
            :icon="Key"
            :disabled="isAdminRole(row)"
            @click="openPermissionDialog(row)"
          >
            授权
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:role:update')"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            :disabled="isAdminRole(row)"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:role:delete')"
            link
            type="danger"
            :icon="Delete"
            :disabled="isAdminRole(row)"
            @click="removeRole(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <footer class="table-footer">
      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRoles"
        @current-change="loadRoles"
      />
    </footer>

    <el-dialog v-model="roleDialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="520px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="84px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="例如 operator" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="例如 运营人员" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permissionDialogVisible" title="角色授权" width="560px">
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        node-key="id"
        show-checkbox
        default-expand-all
        :props="{ label: 'permissionName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPermissions">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Key, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { fetchPermissionTree, type PermissionItem } from '@/api/permissions'
import { useAuthStore } from '@/stores/auth'
import {
  assignRolePermissions,
  createRole,
  deleteRole,
  fetchRolePermissionIds,
  fetchRoles,
  updateRole,
  updateRoleStatus,
  type RoleItem
} from '@/api/roles'

const loading = ref(false)
const submitting = ref(false)
const roles = ref<RoleItem[]>([])
const total = ref(0)
const roleDialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const editingRole = ref<RoleItem | null>(null)
const permissionTarget = ref<RoleItem | null>(null)
const permissionTree = ref<PermissionItem[]>([])
const roleFormRef = ref<FormInstance>()
const permissionTreeRef = ref()
const authStore = useAuthStore()

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  roleCode: '',
  status: '' as number | ''
})

const roleForm = reactive({
  roleCode: '',
  roleName: '',
  status: 1,
  description: ''
})

const roleRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

// 页面初始化时加载角色列表。
onMounted(() => {
  void loadRoles()
})

// 加载角色分页数据，错误提示直接透出后端业务消息。
async function loadRoles() {
  loading.value = true
  try {
    const page = await fetchRoles({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      roleCode: query.roleCode || undefined,
      status: query.status
    })
    roles.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

// 查询时回到第一页，避免筛选后停留在不存在的页码。
function searchRoles() {
  query.pageNo = 1
  void loadRoles()
}

// 重置筛选条件并重新加载列表。
function resetQuery() {
  query.pageNo = 1
  query.roleCode = ''
  query.status = ''
  void loadRoles()
}

// 打开新增角色弹窗并清空表单。
function openCreateDialog() {
  editingRole.value = null
  Object.assign(roleForm, {
    roleCode: '',
    roleName: '',
    status: 1,
    description: ''
  })
  roleDialogVisible.value = true
}

// 打开编辑角色弹窗，默认管理员角色只允许查看不能修改。
function openEditDialog(role: RoleItem) {
  if (isAdminRole(role)) {
    ElMessage.warning('默认管理员角色不能操作')
    return
  }
  editingRole.value = role
  Object.assign(roleForm, {
    roleCode: role.roleCode,
    roleName: role.roleName,
    status: role.status,
    description: role.description || ''
  })
  roleDialogVisible.value = true
}

// 提交新增或编辑表单。
async function submitRole() {
  await roleFormRef.value?.validate()
  submitting.value = true
  try {
    if (editingRole.value) {
      await updateRole(editingRole.value.id, { ...roleForm })
    } else {
      await createRole({ ...roleForm })
    }
    ElMessage.success('保存成功')
    roleDialogVisible.value = false
    await loadRoles()
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

// 打开角色授权弹窗，默认管理员角色不允许手工覆盖授权。
async function openPermissionDialog(role: RoleItem) {
  if (isAdminRole(role)) {
    ElMessage.warning('默认管理员角色不能操作')
    return
  }
  permissionTarget.value = role
  permissionDialogVisible.value = true
  submitting.value = true
  try {
    const [tree, permissionIds] = await Promise.all([fetchPermissionTree(), fetchRolePermissionIds(role.id)])
    permissionTree.value = tree
    await nextTick()
    permissionTreeRef.value?.setCheckedKeys(permissionIds)
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

// 保存角色授权，后端会刷新该角色下用户的权限缓存。
async function submitPermissions() {
  if (!permissionTarget.value) {
    return
  }
  submitting.value = true
  try {
    const checkedKeys = (permissionTreeRef.value?.getCheckedKeys(false) || []).map(String)
    const halfCheckedKeys = (permissionTreeRef.value?.getHalfCheckedKeys() || []).map(String)
    await assignRolePermissions(permissionTarget.value.id, [...checkedKeys, ...halfCheckedKeys])
    ElMessage.success('授权已更新')
    permissionDialogVisible.value = false
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

// 启用或禁用角色，默认管理员角色不可调整。
async function toggleStatus(role: RoleItem) {
  if (isAdminRole(role)) {
    ElMessage.warning('默认管理员角色不能操作')
    return
  }
  const nextStatus = role.status === 1 ? 0 : 1
  try {
    await updateRoleStatus(role.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadRoles()
  } catch (error) {
    showError(error)
  }
}

// 删除角色前弹出确认，后端会继续校验是否已分配给用户。
async function removeRole(role: RoleItem) {
  if (isAdminRole(role)) {
    ElMessage.warning('默认管理员角色不能操作')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除角色 ${role.roleName}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteRole(role.id)
    ElMessage.success('已删除')
    await loadRoles()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error)
    }
  }
}

// 默认管理员角色是系统内置角色，只允许查看。
function isAdminRole(role: RoleItem) {
  return role.id === '1' || role.roleCode === 'admin'
}

// 统一展示业务错误。
function showError(error: unknown) {
  const message = error instanceof Error ? error.message : '操作失败'
  ElMessage.error(message)
}
</script>
