<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>业务接口管理</h2>
        <p>维护业务系统接口地址、请求方法、参数定义，并支持在线测试。</p>
      </div>
      <el-button
        v-if="authStore.hasPermission('system:business-api:add')"
        type="primary"
        :icon="Plus"
        @click="openCreateDialog"
      >
        新增业务接口
      </el-button>
    </header>

    <section class="filter-bar business-api-filter">
      <el-select v-model="query.systemId" clearable filterable placeholder="业务系统">
        <el-option v-for="system in systemOptions" :key="system.id" :label="system.systemName" :value="system.id" />
      </el-select>
      <el-input v-model="query.apiName" clearable placeholder="接口名称" />
      <el-input v-model="query.apiCode" clearable placeholder="接口编码" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchApis">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-table v-loading="loading" :data="apis" class="data-table" row-key="id">
      <el-table-column prop="apiName" label="接口名称" min-width="160" />
      <el-table-column prop="apiCode" label="接口编码" min-width="160" />
      <el-table-column prop="systemName" label="所属系统" min-width="150" />
      <el-table-column label="请求" min-width="260" show-overflow-tooltip>
        <template #default="{ row }">
          <el-tag size="small">{{ row.requestMethod }}</el-tag>
          <span class="api-path">{{ row.requestPath }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="authStore.hasPermission('system:business-api:test')"
            link
            type="success"
            :icon="VideoPlay"
            @click="openTestDialog(row)"
          >
            测试
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:business-api:update')"
            link
            type="primary"
            :icon="Edit"
            @click="openEditDialog(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:business-api:update')"
            link
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
            v-if="authStore.hasPermission('system:business-api:delete')"
            link
            type="danger"
            :icon="Delete"
            @click="removeApi(row)"
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
        @size-change="loadApis"
        @current-change="loadApis"
      />
    </footer>

    <el-dialog v-model="dialogVisible" :title="editingApi ? '编辑业务接口' : '新增业务接口'" width="880px">
      <el-form ref="formRef" :model="apiForm" :rules="rules" label-width="112px">
        <el-form-item label="业务系统" prop="systemId">
          <el-select v-model="apiForm.systemId" class="full-select" filterable>
            <el-option v-for="system in systemOptions" :key="system.id" :label="system.systemName" :value="system.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="接口名称" prop="apiName">
          <el-input v-model="apiForm.apiName" />
        </el-form-item>
        <el-form-item label="接口编码" prop="apiCode">
          <el-input v-model="apiForm.apiCode" />
        </el-form-item>
        <el-form-item label="请求路径" prop="requestPath">
          <el-input v-model="apiForm.requestPath" placeholder="/users/{userId}" />
        </el-form-item>
        <el-form-item label="请求方法" prop="requestMethod">
          <el-select v-model="apiForm.requestMethod" class="full-select">
            <el-option v-for="method in requestMethods" :key="method" :label="method" :value="method" />
          </el-select>
        </el-form-item>
        <el-form-item label="Content-Type">
          <el-input v-model="apiForm.contentType" placeholder="application/json" />
        </el-form-item>
        <el-form-item label="响应数据路径">
          <el-input v-model="apiForm.responseDataPath" placeholder="data.records" />
        </el-form-item>
        <el-form-item label="连接超时">
          <el-input-number v-model="apiForm.connectTimeout" :min="1" :max="120000" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="读取超时">
          <el-input-number v-model="apiForm.readTimeout" :min="1" :max="300000" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="apiForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="apiForm.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>

      <section class="parameter-toolbar">
        <strong>参数定义</strong>
        <el-button :icon="Plus" @click="addParameter">新增参数</el-button>
      </section>
      <el-table :data="apiForm.parameters" border row-key="sortNo">
        <el-table-column label="名称" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.parameterName" />
          </template>
        </el-table-column>
        <el-table-column label="位置" width="120">
          <template #default="{ row }">
            <el-select v-model="row.parameterLocation">
              <el-option v-for="item in parameterLocations" :key="item" :label="item" :value="item" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <el-select v-model="row.parameterType">
              <el-option v-for="item in parameterTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="90">
          <template #default="{ row }">
            <el-switch v-model="row.required" :active-value="1" :inactive-value="0" />
          </template>
        </el-table-column>
        <el-table-column label="默认值" min-width="140">
          <template #default="{ row }">
            <el-input v-model="row.defaultValue" />
          </template>
        </el-table-column>
        <el-table-column label="说明" min-width="160">
          <template #default="{ row }">
            <el-input v-model="row.description" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ $index }">
            <el-button link type="danger" :icon="Delete" @click="removeParameter($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitApi">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialogVisible" title="接口在线测试" width="920px" class="api-test-dialog">
      <section v-if="testingApi" class="test-detail-panel">
        <div class="test-detail-grid">
          <div class="test-detail-item">
            <span>接口名称</span>
            <strong>{{ testingApi.apiName }}</strong>
          </div>
          <div class="test-detail-item">
            <span>所属系统</span>
            <strong>{{ testingApi.systemName || '-' }}</strong>
          </div>
          <div class="test-detail-item full">
            <span>请求路径</span>
            <code>{{ testingApi.requestMethod }} {{ testingApi.fullRequestUrl || testingApi.requestPath }}</code>
          </div>
          <div class="test-detail-item">
            <span>Content-Type</span>
            <strong>{{ testingApi.contentType || 'application/json' }}</strong>
          </div>
          <div class="test-detail-item">
            <span>响应数据路径</span>
            <strong>{{ testingApi.responseDataPath || '-' }}</strong>
          </div>
        </div>
      </section>

      <section class="test-section">
        <div class="test-section-title">
          <strong>请求参数</strong>
          <span>{{ testingApi?.parameters.length || 0 }} 个参数</span>
        </div>
        <el-table v-if="testingApi?.parameters.length" :data="testingApi.parameters" border>
          <el-table-column prop="parameterName" label="参数名" min-width="150" />
          <el-table-column prop="parameterLocation" label="位置" width="96" />
          <el-table-column prop="parameterType" label="类型" width="110" />
          <el-table-column label="必填" width="80">
            <template #default="{ row }">
              {{ formatRequired(row.required) }}
            </template>
          </el-table-column>
          <el-table-column label="默认值" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.defaultValue || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="测试值" min-width="220">
            <template #default="{ row }">
              <el-input v-model="testValues[row.parameterName]" :placeholder="testPlaceholder(row)" />
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="当前接口未定义请求参数" />
      </section>

      <section class="test-section">
        <div class="test-section-title">
          <strong>请求预览</strong>
          <span>根据当前测试值实时生成</span>
        </div>
        <div class="preview-grid">
          <div class="response-box">
            <span>请求地址</span>
            <pre>{{ requestPreview.url }}</pre>
          </div>
          <div class="response-box">
            <span>请求 Header</span>
            <pre>{{ requestPreview.headers }}</pre>
          </div>
          <div class="response-box">
            <span>请求 Body</span>
            <pre>{{ requestPreview.body }}</pre>
          </div>
        </div>
      </section>

      <section class="test-section">
        <div class="test-section-title">
          <strong>返回数据</strong>
          <span v-if="testResult">状态码 {{ testResult.statusCode }}，耗时 {{ testResult.costMs }} ms</span>
          <span v-else-if="testError">测试失败</span>
          <span v-else>发起测试后展示返回内容</span>
        </div>
        <el-alert v-if="testError" class="test-error-alert" type="error" :closable="false" :description="testError" show-icon />
        <el-empty v-else-if="!testResult" description="暂无返回数据" />
        <template v-else>
          <div class="response-box">
            <span>提取数据</span>
            <pre>{{ formatResult(testResult.extractedData) }}</pre>
          </div>
          <div class="response-box">
            <span>原始响应</span>
            <pre>{{ formatResponseBody(testResult.body) }}</pre>
          </div>
        </template>
      </section>

      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="testing" @click="submitTest">发起测试</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Delete, Edit, Plus, Refresh, Search, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createBusinessApi,
  deleteBusinessApi,
  fetchBusinessApi,
  fetchBusinessApis,
  testBusinessApi,
  updateBusinessApi,
  updateBusinessApiStatus,
  type BusinessApiItem,
  type BusinessApiParameterItem,
  type BusinessApiPayload,
  type BusinessApiTestResponse
} from '@/api/business-apis'
import { fetchBusinessSystems, type BusinessSystemItem } from '@/api/business-systems'
import { useAuthStore } from '@/stores/auth'

type ParameterLocation = BusinessApiParameterItem['parameterLocation']

const loading = ref(false)
const submitting = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const apis = ref<BusinessApiItem[]>([])
const total = ref(0)
const systemOptions = ref<BusinessSystemItem[]>([])
const editingApi = ref<BusinessApiItem | null>(null)
const testingApi = ref<BusinessApiItem | null>(null)
const testResult = ref<BusinessApiTestResponse | null>(null)
const testError = ref('')
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const requestMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
const parameterLocations: ParameterLocation[] = ['PATH', 'QUERY', 'HEADER', 'BODY']
const parameterTypes = ['STRING', 'INTEGER', 'LONG', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'ARRAY', 'OBJECT']

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  systemId: '',
  apiName: '',
  apiCode: '',
  status: '' as number | ''
})

const apiForm = reactive<BusinessApiPayload>({
  systemId: '',
  apiCode: '',
  apiName: '',
  requestPath: '',
  requestMethod: 'GET',
  contentType: 'application/json',
  connectTimeout: null,
  readTimeout: null,
  responseDataPath: '',
  status: 1,
  description: '',
  parameters: []
})

const testValues = reactive<Record<string, string>>({})
const requestPreview = computed(() => buildRequestPreview())

const rules: FormRules = {
  systemId: [{ required: true, message: '请选择业务系统', trigger: 'change' }],
  apiName: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  apiCode: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  requestPath: [{ required: true, message: '请输入请求路径', trigger: 'blur' }],
  requestMethod: [{ required: true, message: '请选择请求方法', trigger: 'change' }]
}

onMounted(() => {
  void loadSystemOptions()
  void loadApis()
})

// 加载业务系统下拉选项，业务接口必须归属到一个业务系统。
async function loadSystemOptions() {
  try {
    const page = await fetchBusinessSystems({ pageNo: 1, pageSize: 100, status: 1 })
    systemOptions.value = page.records
  } catch (error) {
    showError(error, '加载业务系统失败')
  }
}

// 加载业务接口分页列表。
async function loadApis() {
  loading.value = true
  try {
    const page = await fetchBusinessApis({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      systemId: query.systemId || undefined,
      apiName: query.apiName || undefined,
      apiCode: query.apiCode || undefined,
      status: query.status
    })
    apis.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    showError(error, '加载业务接口失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮回到第一页，避免旧页码影响筛选结果。
function searchApis() {
  query.pageNo = 1
  void loadApis()
}

// 重置筛选条件并重新加载列表。
function resetQuery() {
  query.pageNo = 1
  query.systemId = ''
  query.apiName = ''
  query.apiCode = ''
  query.status = ''
  void loadApis()
}

// 打开新增弹窗并恢复默认接口表单。
function openCreateDialog() {
  editingApi.value = null
  resetForm()
  dialogVisible.value = true
}

// 编辑前查询详情，确保参数定义是最新数据。
async function openEditDialog(api: BusinessApiItem) {
  try {
    const detail = await fetchBusinessApi(api.id)
    editingApi.value = detail
    Object.assign(apiForm, {
      systemId: detail.systemId,
      apiCode: detail.apiCode,
      apiName: detail.apiName,
      requestPath: detail.requestPath,
      requestMethod: detail.requestMethod,
      contentType: detail.contentType || 'application/json',
      connectTimeout: detail.connectTimeout,
      readTimeout: detail.readTimeout,
      responseDataPath: detail.responseDataPath || '',
      status: detail.status,
      description: detail.description || '',
      parameters: detail.parameters.map(copyParameter)
    })
    dialogVisible.value = true
  } catch (error) {
    showError(error, '加载接口详情失败')
  }
}

// 保存接口配置和参数定义。
async function submitApi() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = normalizePayload()
    if (editingApi.value) {
      await updateBusinessApi(editingApi.value.id, payload)
    } else {
      await createBusinessApi(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadApis()
  } catch (error) {
    showError(error, '保存业务接口失败')
  } finally {
    submitting.value = false
  }
}

// 根据当前状态切换启用或禁用。
async function toggleStatus(api: BusinessApiItem) {
  const nextStatus = api.status === 1 ? 0 : 1
  try {
    await updateBusinessApiStatus(api.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadApis()
  } catch (error) {
    showError(error, '更新状态失败')
  }
}

// 删除前二次确认，避免误删接口配置。
async function removeApi(api: BusinessApiItem) {
  try {
    await ElMessageBox.confirm(`确认删除业务接口 ${api.apiName}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteBusinessApi(api.id)
    ElMessage.success('已删除')
    await loadApis()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error, '删除业务接口失败')
    }
  }
}

// 新增一行参数定义，默认作为查询参数。
function addParameter() {
  apiForm.parameters.push({
    parameterName: '',
    parameterLocation: 'QUERY',
    parameterType: 'STRING',
    required: 0,
    defaultValue: '',
    description: '',
    sortNo: apiForm.parameters.length + 1
  })
}

// 删除指定参数行，并重排排序号。
function removeParameter(index: number) {
  apiForm.parameters.splice(index, 1)
  apiForm.parameters.forEach((parameter, parameterIndex) => {
    parameter.sortNo = parameterIndex + 1
  })
}

// 打开在线测试弹窗并根据参数定义初始化测试值。
async function openTestDialog(api: BusinessApiItem) {
  try {
    const detail = await fetchBusinessApi(api.id)
    testingApi.value = detail
    testResult.value = null
    testError.value = ''
    Object.keys(testValues).forEach((key) => delete testValues[key])
    detail.parameters.forEach((parameter) => {
      testValues[parameter.parameterName] = parameter.defaultValue || ''
    })
    testDialogVisible.value = true
  } catch (error) {
    showError(error, '加载接口测试配置失败')
  }
}

// 提交在线测试参数并展示原始响应。
async function submitTest() {
  if (!testingApi.value) {
    return
  }
  testing.value = true
  testError.value = ''
  testResult.value = null
  try {
    testResult.value = await testBusinessApi(testingApi.value.id, { ...testValues })
  } catch (error) {
    testError.value = getErrorMessage(error, '接口测试失败')
  } finally {
    testing.value = false
  }
}

// 恢复新增接口的默认表单。
function resetForm() {
  Object.assign(apiForm, {
    systemId: '',
    apiCode: '',
    apiName: '',
    requestPath: '',
    requestMethod: 'GET',
    contentType: 'application/json',
    connectTimeout: null,
    readTimeout: null,
    responseDataPath: '',
    status: 1,
    description: '',
    parameters: []
  })
}

// 保存前清理空字符串，避免把无意义空值提交到后端。
function normalizePayload(): BusinessApiPayload {
  return {
    ...apiForm,
    contentType: apiForm.contentType || null,
    responseDataPath: apiForm.responseDataPath || null,
    description: apiForm.description || '',
    parameters: apiForm.parameters
      .filter((parameter) => parameter.parameterName.trim())
      .map((parameter, index) => ({
        ...parameter,
        parameterName: parameter.parameterName.trim(),
        defaultValue: parameter.defaultValue || null,
        description: parameter.description || null,
        sortNo: index + 1
      }))
  }
}

// 复制参数对象，避免编辑表格直接引用详情响应。
function copyParameter(parameter: BusinessApiParameterItem): BusinessApiParameterItem {
  return {
    id: parameter.id,
    apiId: parameter.apiId,
    parameterName: parameter.parameterName,
    parameterLocation: parameter.parameterLocation,
    parameterType: parameter.parameterType,
    required: parameter.required,
    defaultValue: parameter.defaultValue || '',
    description: parameter.description || '',
    sortNo: parameter.sortNo
  }
}

// 根据参数定义生成测试输入提示。
function testPlaceholder(parameter: BusinessApiParameterItem) {
  return `${parameter.parameterLocation} / ${parameter.parameterType}${parameter.required === 1 ? ' / 必填' : ''}`
}

// 根据当前测试值生成请求预览，认证 Header 由后端执行时自动附加，前端不展示密钥。
function buildRequestPreview() {
  const api = testingApi.value
  if (!api) {
    return { url: '-', headers: '-', body: '-' }
  }
  let url = api.fullRequestUrl || api.requestPath
  const queryItems: string[] = []
  const headerItems: Record<string, string> = {}
  const bodyItems: Record<string, string> = {}

  api.parameters.forEach((parameter) => {
    const value = resolveTestValue(parameter)
    if (!value) {
      return
    }
    const encodedName = encodeURIComponent(parameter.parameterName)
    const encodedValue = encodeURIComponent(value)
    switch (parameter.parameterLocation) {
      case 'PATH':
        url = url.replace(`{${parameter.parameterName}}`, encodedValue)
        break
      case 'QUERY':
        queryItems.push(`${encodedName}=${encodedValue}`)
        break
      case 'HEADER':
        headerItems[parameter.parameterName] = value
        break
      case 'BODY':
        bodyItems[parameter.parameterName] = value
        break
      default:
        break
    }
  })

  if (queryItems.length > 0) {
    url = `${url}${url.includes('?') ? '&' : '?'}${queryItems.join('&')}`
  }
  if (Object.keys(bodyItems).length > 0 && canSendBody(api.requestMethod)) {
    headerItems['Content-Type'] = api.contentType || 'application/json'
  }
  headerItems['系统认证'] = '后端自动附加，前端不展示密钥'

  return {
    url: `${api.requestMethod} ${url}`,
    headers: formatHeaderPreview(headerItems),
    body: Object.keys(bodyItems).length > 0 && canSendBody(api.requestMethod) ? JSON.stringify(bodyItems, null, 2) : '-'
  }
}

// 测试值为空时沿用参数默认值，预览结果与后端执行规则保持一致。
function resolveTestValue(parameter: BusinessApiParameterItem) {
  const value = testValues[parameter.parameterName]
  return value === undefined || value === '' ? parameter.defaultValue || '' : value
}

// GET 和 DELETE 请求不展示 Body，保持与后端实际发送规则一致。
function canSendBody(method: string) {
  return !['GET', 'DELETE'].includes(method)
}

// Header 预览使用多行文本，避免表格内长 Header 造成布局抖动。
function formatHeaderPreview(headers: Record<string, string>) {
  const entries = Object.entries(headers)
  return entries.length === 0 ? '-' : entries.map(([key, value]) => `${key}: ${value}`).join('\n')
}

// 在线测试参数表统一展示必填状态，避免直接暴露数字标识。
function formatRequired(required: number) {
  return required === 1 ? '是' : '否'
}

// 返回数据可能是对象、数组或基础类型，统一转成便于阅读的文本。
function formatResult(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (typeof value === 'string') {
    return value
  }
  return JSON.stringify(value, null, 2)
}

// 原始响应是字符串，优先按 JSON 格式化，非 JSON 内容原样显示。
function formatResponseBody(body: string | null | undefined) {
  if (!body) {
    return '-'
  }
  try {
    return JSON.stringify(JSON.parse(body), null, 2)
  } catch {
    return body
  }
}

// 后端 LocalDateTime 可能带 T，这里统一展示为空格分隔。
function formatDateTime(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}

// 将接口错误统一转成文案，弹窗内失败详情和全局提示共用同一套规则。
function getErrorMessage(error: unknown, fallback = '操作失败') {
  return error instanceof Error && error.message ? error.message : fallback
}

// 统一展示接口错误，优先使用后端返回消息。
function showError(error: unknown, fallback = '操作失败') {
  ElMessage.error(getErrorMessage(error, fallback))
}
</script>
