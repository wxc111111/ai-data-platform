<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>用户管理</h2>
        <p>维护后台登录账号、状态和角色授权。</p>
      </div>
      <el-button v-if="authStore.hasPermission('system:user:add')" type="primary" :icon="Plus" @click="openCreateDialog">
        新增用户
      </el-button>
    </header>

    <section class="filter-bar">
      <el-input v-model="query.username" clearable placeholder="用户名" />
      <el-input v-model="query.mobile" clearable placeholder="手机号" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchUsers">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-table v-loading="loading" :data="users" class="data-table" row-key="id">
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="mobile" label="手机号" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色" min-width="180">
        <template #default="{ row }">
          <span>{{ roleNames(row.roleIds) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('system:user:update')"
            link
            type="primary"
            :icon="Edit"
            :disabled="isAdminUser(row)"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:user:update')"
            link
            type="primary"
            :icon="UserFilled"
            :disabled="isAdminUser(row)"
            @click="openRoleDialog(row)"
          >
            角色
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:user:update')"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            :disabled="isAdminUser(row)"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:user:delete')"
            link
            type="danger"
            :icon="Delete"
            :disabled="isAdminUser(row)"
            @click="removeUser(row)"
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
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </footer>

    <el-dialog v-model="userDialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="84px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="Boolean(editingUser)" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="userForm.mobile" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="!editingUser" label="角色">
          <el-select v-model="userForm.roleIds" multiple collapse-tags collapse-tags-tooltip>
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitUser">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="460px">
      <el-select v-model="roleForm.roleIds" multiple class="full-select" collapse-tags collapse-tags-tooltip>
        <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
      </el-select>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRoles">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { fetchRoleOptions, type RoleOption } from '@/api/roles'
import { useAuthStore } from '@/stores/auth'
import {
  assignUserRoles,
  createUser,
  deleteUser,
  fetchUsers,
  updateUser,
  updateUserStatus,
  type UserItem
} from '@/api/users'

const loading = ref(false)
const submitting = ref(false)
const users = ref<UserItem[]>([])
const roles = ref<RoleOption[]>([])
const total = ref(0)
const userDialogVisible = ref(false)
const roleDialogVisible = ref(false)
const editingUser = ref<UserItem | null>(null)
const roleTarget = ref<UserItem | null>(null)
const userFormRef = ref<FormInstance>()
const authStore = useAuthStore()

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  mobile: '',
  status: '' as number | ''
})

const userForm = reactive({
  username: '',
  password: '',
  nickname: '',
  mobile: '',
  email: '',
  status: 1,
  roleIds: [] as string[]
})

const roleForm = reactive({
  roleIds: [] as string[]
})

const userRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(async () => {
  try {
    await Promise.all([loadRoles(), loadUsers()])
  } catch (error) {
    showError(error)
  }
})

async function loadUsers() {
  loading.value = true
  try {
    const page = await fetchUsers({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      username: query.username || undefined,
      mobile: query.mobile || undefined,
      status: query.status
    })
    users.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    roles.value = await fetchRoleOptions()
  } catch (error) {
    showError(error)
  }
}

function searchUsers() {
  query.pageNo = 1
  void loadUsers()
}

function resetQuery() {
  query.pageNo = 1
  query.username = ''
  query.mobile = ''
  query.status = ''
  void loadUsers()
}

function openCreateDialog() {
  editingUser.value = null
  Object.assign(userForm, {
    username: '',
    password: '',
    nickname: '',
    mobile: '',
    email: '',
    status: 1,
    roleIds: []
  })
  userDialogVisible.value = true
}

function openEditDialog(user: UserItem) {
  if (isAdminUser(user)) {
    ElMessage.warning('默认管理员不能操作')
    return
  }
  editingUser.value = user
  Object.assign(userForm, {
    username: user.username,
    password: '',
    nickname: user.nickname || '',
    mobile: user.mobile || '',
    email: user.email || '',
    status: user.status,
    roleIds: [...user.roleIds]
  })
  userDialogVisible.value = true
}

async function submitUser() {
  await userFormRef.value?.validate()
  submitting.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.id, {
        nickname: userForm.nickname,
        mobile: userForm.mobile,
        email: userForm.email,
        status: userForm.status
      })
    } else {
      await createUser({ ...userForm })
    }
    ElMessage.success('保存成功')
    userDialogVisible.value = false
    await loadUsers()
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

function openRoleDialog(user: UserItem) {
  if (isAdminUser(user)) {
    ElMessage.warning('默认管理员不能操作')
    return
  }
  roleTarget.value = user
  roleForm.roleIds = [...user.roleIds]
  roleDialogVisible.value = true
}

async function submitRoles() {
  if (!roleTarget.value) {
    return
  }
  submitting.value = true
  try {
    await assignUserRoles(roleTarget.value.id, roleForm.roleIds)
    ElMessage.success('角色已更新')
    roleDialogVisible.value = false
    await loadUsers()
  } catch (error) {
    showError(error)
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(user: UserItem) {
  if (isAdminUser(user)) {
    ElMessage.warning('默认管理员不能操作')
    return
  }
  const nextStatus = user.status === 1 ? 0 : 1
  try {
    await updateUserStatus(user.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadUsers()
  } catch (error) {
    showError(error)
  }
}

async function removeUser(user: UserItem) {
  if (isAdminUser(user)) {
    ElMessage.warning('默认管理员不能操作')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除用户 ${user.username}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteUser(user.id)
    ElMessage.success('已删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error)
    }
  }
}

function roleNames(roleIds: string[]) {
  if (!roleIds.length) {
    return '-'
  }
  const roleMap = new Map(roles.value.map((role) => [role.id, role.roleName]))
  return roleIds.map((roleId) => roleMap.get(roleId) || roleId).join('、')
}

// 默认管理员是系统内置账号，只允许查看，不允许在用户管理页被修改。
function isAdminUser(user: UserItem) {
  return user.id === '1'
}

function showError(error: unknown) {
  const message = error instanceof Error ? error.message : '操作失败'
  ElMessage.error(message)
}
</script>
