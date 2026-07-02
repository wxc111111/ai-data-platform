<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>业务系统</h2>
        <p>维护外部业务系统地址、认证方式和调用基础配置。</p>
      </div>
      <el-button
        v-if="authStore.hasPermission('system:business-system:add')"
        type="primary"
        :icon="Plus"
        @click="openCreateDialog"
      >
        新增业务系统
      </el-button>
    </header>

    <section class="filter-bar">
      <el-input v-model="query.systemName" clearable placeholder="系统名称" />
      <el-input v-model="query.systemCode" clearable placeholder="系统编码" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchSystems">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-table v-loading="loading" :data="systems" class="data-table" row-key="id">
      <el-table-column prop="systemName" label="系统名称" min-width="160" />
      <el-table-column prop="systemCode" label="系统编码" min-width="140" />
      <el-table-column prop="baseUrl" label="基础地址" min-width="260" show-overflow-tooltip />
      <el-table-column label="认证方式" width="140">
        <template #default="{ row }">
          <el-tag>{{ authTypeLabel(row.authType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="可见角色" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ roleNames(row.roleIds) }}
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('system:business-system:update')"
            link
            type="primary"
            :icon="Edit"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:business-system:update')"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:business-system:delete')"
            link
            type="danger"
            :icon="Delete"
            @click="removeSystem(row)"
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
        @size-change="loadSystems"
        @current-change="loadSystems"
      />
    </footer>

    <el-dialog v-model="dialogVisible" :title="editingSystem ? '编辑业务系统' : '新增业务系统'" width="680px">
      <el-form ref="formRef" :model="systemForm" :rules="rules" label-width="104px">
        <el-form-item label="系统名称" prop="systemName">
          <el-input v-model="systemForm.systemName" />
        </el-form-item>
        <el-form-item label="系统编码" prop="systemCode">
          <el-input v-model="systemForm.systemCode" />
        </el-form-item>
        <el-form-item label="基础地址" prop="baseUrl">
          <el-input v-model="systemForm.baseUrl" placeholder="https://example.com" />
        </el-form-item>
        <el-form-item label="认证方式" prop="authType">
          <el-select v-model="systemForm.authType" class="full-select" @change="handleAuthTypeChange">
            <el-option v-for="option in authTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>

        <template v-if="systemForm.authType === 'API_KEY'">
          <el-form-item label="Header 名称">
            <el-input v-model="authConfigForm.headerName" placeholder="X-API-Key" />
          </el-form-item>
          <el-form-item label="API Key">
            <el-input v-model="authConfigForm.apiKey" type="password" show-password />
          </el-form-item>
        </template>

        <template v-if="systemForm.authType === 'BEARER_TOKEN'">
          <el-form-item label="Token">
            <el-input v-model="authConfigForm.token" type="password" show-password />
          </el-form-item>
        </template>

        <template v-if="systemForm.authType === 'BASIC'">
          <el-form-item label="用户名">
            <el-input v-model="authConfigForm.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="authConfigForm.password" type="password" show-password />
          </el-form-item>
        </template>

        <template v-if="systemForm.authType === 'CUSTOM_HEADER'">
          <el-form-item label="Header 名称">
            <el-input v-model="authConfigForm.headerName" />
          </el-form-item>
          <el-form-item label="Header 值">
            <el-input v-model="authConfigForm.headerValue" type="password" show-password />
          </el-form-item>
        </template>

        <el-form-item label="连接超时" prop="connectTimeout">
          <el-input-number v-model="systemForm.connectTimeout" :min="1" :max="120000" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="读取超时" prop="readTimeout">
          <el-input-number v-model="systemForm.readTimeout" :min="1" :max="300000" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="可见角色">
          <el-select
            v-model="systemForm.roleIds"
            class="full-select"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="不选择则仅管理员可见"
          >
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="systemForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="systemForm.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSystem">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createBusinessSystem,
  deleteBusinessSystem,
  fetchBusinessSystem,
  fetchBusinessSystems,
  updateBusinessSystem,
  updateBusinessSystemStatus,
  type BusinessSystemItem
} from '@/api/business-systems'
import { fetchRoleOptions, type RoleOption } from '@/api/roles'
import { useAuthStore } from '@/stores/auth'

type AuthType = 'NONE' | 'API_KEY' | 'BEARER_TOKEN' | 'BASIC' | 'CUSTOM_HEADER'

const loading = ref(false)
const submitting = ref(false)
const systems = ref<BusinessSystemItem[]>([])
const roles = ref<RoleOption[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const editingSystem = ref<BusinessSystemItem | null>(null)
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const authTypeOptions: Array<{ label: string; value: AuthType }> = [
  { label: '无认证', value: 'NONE' },
  { label: 'API Key', value: 'API_KEY' },
  { label: 'Bearer Token', value: 'BEARER_TOKEN' },
  { label: 'Basic Auth', value: 'BASIC' },
  { label: '自定义 Header', value: 'CUSTOM_HEADER' }
]

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  systemName: '',
  systemCode: '',
  status: '' as number | ''
})

const systemForm = reactive({
  systemCode: '',
  systemName: '',
  baseUrl: '',
  authType: 'NONE' as AuthType,
  connectTimeout: 5000,
  readTimeout: 10000,
  status: 1,
  description: '',
  roleIds: [] as string[]
})

const authConfigForm = reactive({
  headerName: '',
  apiKey: '',
  token: '',
  username: '',
  password: '',
  headerValue: ''
})

const rules: FormRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  systemCode: [{ required: true, message: '请输入系统编码', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入基础地址', trigger: 'blur' }],
  authType: [{ required: true, message: '请选择认证方式', trigger: 'change' }]
}

onMounted(() => {
  void loadRoleOptions()
  void loadSystems()
})

// 加载可选角色，用于配置业务系统的数据可见范围。
async function loadRoleOptions() {
  try {
    roles.value = await fetchRoleOptions()
  } catch (error) {
    showError(error, '加载角色选项失败')
  }
}

// 加载业务系统列表，并把空查询条件转换成后端可忽略的参数。
async function loadSystems() {
  loading.value = true
  try {
    const page = await fetchBusinessSystems({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      systemName: query.systemName || undefined,
      systemCode: query.systemCode || undefined,
      status: query.status
    })
    systems.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    showError(error, '加载业务系统失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮会回到第一页，避免旧页码导致看不到新结果。
function searchSystems() {
  query.pageNo = 1
  void loadSystems()
}

// 重置查询条件并重新加载列表。
function resetQuery() {
  query.pageNo = 1
  query.systemName = ''
  query.systemCode = ''
  query.status = ''
  void loadSystems()
}

// 打开新增弹窗并恢复默认表单。
function openCreateDialog() {
  editingSystem.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑前先查询详情，确保认证配置是最新数据。
async function openEditDialog(system: BusinessSystemItem) {
  try {
    const detail = await fetchBusinessSystem(system.id)
    editingSystem.value = detail
    Object.assign(systemForm, {
      systemCode: detail.systemCode,
      systemName: detail.systemName,
      baseUrl: detail.baseUrl,
      authType: detail.authType as AuthType,
      connectTimeout: detail.connectTimeout,
      readTimeout: detail.readTimeout,
      status: detail.status,
      description: detail.description || '',
      roleIds: detail.roleIds || []
    })
    parseAuthConfig(detail.authConfig)
    dialogVisible.value = true
  } catch (error) {
    showError(error, '加载业务系统详情失败')
  }
}

// 提交前把认证配置表单转换成 JSON 字符串。
async function submitSystem() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = {
      ...systemForm,
      authConfig: buildAuthConfig()
    }
    if (editingSystem.value) {
      await updateBusinessSystem(editingSystem.value.id, payload)
    } else {
      await createBusinessSystem(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadSystems()
  } catch (error) {
    showError(error, '保存业务系统失败')
  } finally {
    submitting.value = false
  }
}

// 根据当前状态切换启用或禁用。
async function toggleStatus(system: BusinessSystemItem) {
  const nextStatus = system.status === 1 ? 0 : 1
  try {
    await updateBusinessSystemStatus(system.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadSystems()
  } catch (error) {
    showError(error, '更新状态失败')
  }
}

// 删除前二次确认，避免误删系统接入配置。
async function removeSystem(system: BusinessSystemItem) {
  try {
    await ElMessageBox.confirm(`确认删除业务系统 ${system.systemName}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteBusinessSystem(system.id)
    ElMessage.success('已删除')
    await loadSystems()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error, '删除业务系统失败')
    }
  }
}

// 认证方式变化时清空旧认证字段，避免保存错类型配置。
function handleAuthTypeChange() {
  resetAuthConfig()
}

// 回到新增业务系统的默认表单状态。
function resetForm() {
  Object.assign(systemForm, {
    systemCode: '',
    systemName: '',
    baseUrl: '',
    authType: 'NONE',
    connectTimeout: 5000,
    readTimeout: 10000,
    status: 1,
    description: '',
    roleIds: []
  })
  resetAuthConfig()
}

// 清空认证配置子表单。
function resetAuthConfig() {
  Object.assign(authConfigForm, {
    headerName: '',
    apiKey: '',
    token: '',
    username: '',
    password: '',
    headerValue: ''
  })
}

// 编辑回填时解析后端保存的认证配置 JSON，解析失败时使用空配置。
function parseAuthConfig(authConfig: string | null) {
  resetAuthConfig()
  if (!authConfig) {
    return
  }
  try {
    Object.assign(authConfigForm, JSON.parse(authConfig))
  } catch {
    ElMessage.warning('认证配置格式异常，请重新保存')
  }
}

// 根据认证方式组装后端保存的 JSON 字符串，无认证时不保存配置。
function buildAuthConfig() {
  if (systemForm.authType === 'NONE') {
    return null
  }
  const configMap: Record<AuthType, Record<string, string> | null> = {
    NONE: null,
    API_KEY: {
      headerName: authConfigForm.headerName,
      apiKey: authConfigForm.apiKey
    },
    BEARER_TOKEN: {
      token: authConfigForm.token
    },
    BASIC: {
      username: authConfigForm.username,
      password: authConfigForm.password
    },
    CUSTOM_HEADER: {
      headerName: authConfigForm.headerName,
      headerValue: authConfigForm.headerValue
    }
  }
  return JSON.stringify(configMap[systemForm.authType])
}

// 展示认证方式中文文案。
function authTypeLabel(authType: string) {
  return authTypeOptions.find((option) => option.value === authType)?.label || authType
}

// 把角色 ID 展示成角色名称；未分配角色时只有管理员能查看。
function roleNames(roleIds: string[] | undefined) {
  if (!roleIds?.length) {
    return '仅管理员'
  }
  const roleMap = new Map(roles.value.map((role) => [role.id, role.roleName]))
  return roleIds.map((roleId) => roleMap.get(roleId) || roleId).join('、')
}

// 后端 LocalDateTime 可能带 T，这里统一展示为空格分隔。
function formatDateTime(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}

// 统一展示接口错误，优先使用后端返回消息。
function showError(error: unknown, fallback = '操作失败') {
  const message = error instanceof Error ? error.message : fallback
  ElMessage.error(message)
}
</script>
