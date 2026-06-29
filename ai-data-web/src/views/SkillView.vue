<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>Skill 管理</h2>
        <p>将稳定的业务接口封装成可发布、可测试的业务能力。</p>
      </div>
      <el-button v-if="authStore.hasPermission('system:skill:add')" type="primary" :icon="Plus" @click="openCreateDialog">
        新增 Skill
      </el-button>
    </header>

    <section class="filter-bar role-filter-bar">
      <el-input v-model="query.skillName" clearable placeholder="Skill 名称" />
      <el-input v-model="query.skillCode" clearable placeholder="Skill 编码" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="searchSkills">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </section>

    <el-table v-loading="loading" :data="skills" class="data-table" row-key="id">
      <el-table-column prop="skillName" label="Skill 名称" min-width="160" />
      <el-table-column prop="skillCode" label="Skill 编码" min-width="160" />
      <el-table-column label="关联接口" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.apiName || '-' }}
          <span v-if="row.apiCode" class="api-path">({{ row.apiCode }})</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="能力说明" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatDateTime(row.updatedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="authStore.hasPermission('system:skill:test')" link type="success" :icon="VideoPlay" @click="openTestDialog(row)">
            测试
          </el-button>
          <el-button v-if="authStore.hasPermission('system:skill:update')" link type="primary" :icon="Edit" @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button v-if="authStore.hasPermission('system:skill:update')" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button v-if="authStore.hasPermission('system:skill:delete')" link type="danger" :icon="Delete" @click="removeSkill(row)">
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
        @size-change="loadSkills"
        @current-change="loadSkills"
      />
    </footer>

    <el-dialog v-model="dialogVisible" :title="editingSkill ? '编辑 Skill' : '新增 Skill'" width="920px">
      <el-form ref="formRef" :model="skillForm" :rules="rules" label-width="112px">
        <el-form-item label="Skill 名称" prop="skillName">
          <el-input v-model="skillForm.skillName" />
        </el-form-item>
        <el-form-item label="Skill 编码" prop="skillCode">
          <el-input v-model="skillForm.skillCode" placeholder="query_user_info" />
        </el-form-item>
        <el-form-item label="关联接口" prop="apiId">
          <el-select v-model="skillForm.apiId" class="full-select" filterable @change="syncApiParameters">
            <el-option v-for="api in apiOptions" :key="api.id" :label="`${api.apiName} (${api.apiCode})`" :value="api.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="能力说明" prop="description">
          <el-input v-model="skillForm.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="权限编码">
          <el-input v-model="skillForm.permissionCode" placeholder="可选，例如 skill:query-user" />
        </el-form-item>
        <el-form-item label="超时时间">
          <el-input-number v-model="skillForm.timeoutMs" :min="1" :max="300000" :step="1000" controls-position="right" />
        </el-form-item>
        <el-form-item label="最大返回数">
          <el-input-number v-model="skillForm.maxResultCount" :min="1" :max="10000" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="skillForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <section class="parameter-toolbar">
        <strong>参数映射</strong>
        <el-button :icon="Plus" @click="addParameter">新增参数</el-button>
      </section>
      <el-table :data="skillForm.parameters" border row-key="sortNo">
        <el-table-column label="Skill 参数" min-width="140">
          <template #default="{ row }"><el-input v-model="row.parameterName" /></template>
        </el-table-column>
        <el-table-column label="类型" width="130">
          <template #default="{ row }">
            <el-select v-model="row.parameterType">
              <el-option v-for="item in parameterTypes" :key="item" :label="item" :value="item" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="必填" width="86">
          <template #default="{ row }"><el-switch v-model="row.required" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="接口参数" min-width="150">
          <template #default="{ row }">
            <el-select v-model="row.apiParameterName" filterable allow-create>
              <el-option v-for="item in selectedApiParameters" :key="item.parameterName" :label="item.parameterName" :value="item.parameterName" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-select v-model="row.valueSource">
              <el-option label="调用方" value="CALLER" />
              <el-option label="固定值" value="CONSTANT" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="默认/固定值" min-width="140">
          <template #default="{ row }"><el-input v-model="row.defaultValue" /></template>
        </el-table-column>
        <el-table-column label="说明" min-width="160">
          <template #default="{ row }"><el-input v-model="row.description" /></template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ $index }"><el-button link type="danger" :icon="Delete" @click="removeParameter($index)">删除</el-button></template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitSkill">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialogVisible" title="Skill 在线测试" width="820px" class="api-test-dialog">
      <section v-if="testingSkill" class="test-detail-panel">
        <div class="test-detail-grid">
          <div class="test-detail-item">
            <span>Skill 名称</span>
            <strong>{{ testingSkill.skillName }}</strong>
          </div>
          <div class="test-detail-item">
            <span>关联接口</span>
            <strong>{{ testingSkill.apiName || '-' }}</strong>
          </div>
          <div class="test-detail-item full">
            <span>能力说明</span>
            <strong>{{ testingSkill.description }}</strong>
          </div>
        </div>
      </section>

      <section class="test-section">
        <div class="test-section-title">
          <strong>测试参数</strong>
          <span>{{ testingSkill?.parameters.length || 0 }} 个参数</span>
        </div>
        <el-table v-if="testingSkill?.parameters.length" :data="testingSkill.parameters" border>
          <el-table-column prop="parameterName" label="参数名" min-width="140" />
          <el-table-column prop="parameterType" label="类型" width="110" />
          <el-table-column label="必填" width="80">
            <template #default="{ row }">{{ row.required === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="apiParameterName" label="接口参数" min-width="130" />
          <el-table-column label="测试值" min-width="220">
            <template #default="{ row }">
              <el-input v-model="testValues[row.parameterName]" :disabled="row.valueSource === 'CONSTANT'" :placeholder="testPlaceholder(row)" />
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="当前 Skill 未定义参数" />
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
import { fetchBusinessApi, fetchBusinessApis, type BusinessApiItem } from '@/api/business-apis'
import {
  createSkill,
  deleteSkill,
  fetchSkill,
  fetchSkills,
  testSkill,
  updateSkill,
  updateSkillStatus,
  type SkillItem,
  type SkillParameterItem,
  type SkillPayload
} from '@/api/skills'
import type { BusinessApiTestResponse } from '@/api/business-apis'
import { useAuthStore } from '@/stores/auth'

const loading = ref(false)
const submitting = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const skills = ref<SkillItem[]>([])
const total = ref(0)
const apiOptions = ref<BusinessApiItem[]>([])
const editingSkill = ref<SkillItem | null>(null)
const testingSkill = ref<SkillItem | null>(null)
const testResult = ref<BusinessApiTestResponse | null>(null)
const testError = ref('')
const formRef = ref<FormInstance>()
const authStore = useAuthStore()

const parameterTypes = ['STRING', 'INTEGER', 'LONG', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'ARRAY', 'OBJECT']

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  skillName: '',
  skillCode: '',
  status: '' as number | ''
})

const skillForm = reactive<SkillPayload>({
  skillCode: '',
  skillName: '',
  description: '',
  apiId: '',
  permissionCode: '',
  timeoutMs: 10000,
  maxResultCount: 100,
  status: 1,
  parameters: []
})

const testValues = reactive<Record<string, string>>({})

const rules: FormRules = {
  skillName: [{ required: true, message: '请输入 Skill 名称', trigger: 'blur' }],
  skillCode: [{ required: true, message: '请输入 Skill 编码', trigger: 'blur' }],
  apiId: [{ required: true, message: '请选择关联接口', trigger: 'change' }],
  description: [{ required: true, message: '请输入能力说明', trigger: 'blur' }]
}

const selectedApiParameters = computed(() => {
  return apiOptions.value.find((api) => api.id === skillForm.apiId)?.parameters || []
})

onMounted(() => {
  void loadApiOptions()
  void loadSkills()
})

// 加载可关联的业务接口，Skill 第一版只绑定一个已启用接口。
async function loadApiOptions() {
  try {
    const page = await fetchBusinessApis({ pageNo: 1, pageSize: 100, status: 1 })
    apiOptions.value = page.records
  } catch (error) {
    showError(error, '加载业务接口失败')
  }
}

// 加载 Skill 列表。
async function loadSkills() {
  loading.value = true
  try {
    const page = await fetchSkills({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      skillName: query.skillName || undefined,
      skillCode: query.skillCode || undefined,
      status: query.status
    })
    skills.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    showError(error, '加载 Skill 失败')
  } finally {
    loading.value = false
  }
}

function searchSkills() {
  query.pageNo = 1
  void loadSkills()
}

function resetQuery() {
  query.pageNo = 1
  query.skillName = ''
  query.skillCode = ''
  query.status = ''
  void loadSkills()
}

function openCreateDialog() {
  editingSkill.value = null
  resetForm()
  dialogVisible.value = true
}

async function openEditDialog(skill: SkillItem) {
  try {
    const detail = await fetchSkill(skill.id)
    editingSkill.value = detail
    Object.assign(skillForm, {
      skillCode: detail.skillCode,
      skillName: detail.skillName,
      description: detail.description,
      apiId: detail.apiId,
      permissionCode: detail.permissionCode || '',
      timeoutMs: detail.timeoutMs,
      maxResultCount: detail.maxResultCount,
      status: detail.status,
      parameters: detail.parameters.map(copyParameter)
    })
    dialogVisible.value = true
  } catch (error) {
    showError(error, '加载 Skill 详情失败')
  }
}

async function submitSkill() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const payload = normalizePayload()
    if (editingSkill.value) {
      await updateSkill(editingSkill.value.id, payload)
    } else {
      await createSkill(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadSkills()
  } catch (error) {
    showError(error, '保存 Skill 失败')
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(skill: SkillItem) {
  const nextStatus = skill.status === 1 ? 0 : 1
  try {
    await updateSkillStatus(skill.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已启用' : '已禁用')
    await loadSkills()
  } catch (error) {
    showError(error, '更新状态失败')
  }
}

async function removeSkill(skill: SkillItem) {
  try {
    await ElMessageBox.confirm(`确认删除 Skill ${skill.skillName}？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteSkill(skill.id)
    ElMessage.success('已删除')
    await loadSkills()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error, '删除 Skill 失败')
    }
  }
}

function addParameter() {
  skillForm.parameters.push({
    parameterName: '',
    parameterType: 'STRING',
    required: 0,
    description: '',
    apiParameterName: '',
    defaultValue: '',
    valueSource: 'CALLER',
    sortNo: skillForm.parameters.length + 1
  })
}

function removeParameter(index: number) {
  skillForm.parameters.splice(index, 1)
  skillForm.parameters.forEach((parameter, parameterIndex) => {
    parameter.sortNo = parameterIndex + 1
  })
}

// 选择业务接口后按接口参数生成默认 Skill 参数映射，减少重复录入。
async function syncApiParameters() {
  const api = apiOptions.value.find((item) => item.id === skillForm.apiId)
  if (!api) {
    return
  }
  const detail = api.parameters.length > 0 ? api : await fetchBusinessApiDetail(api.id)
  skillForm.parameters = detail.parameters.map((parameter, index) => ({
    parameterName: parameter.parameterName,
    parameterType: parameter.parameterType,
    required: parameter.required,
    description: parameter.description || '',
    apiParameterName: parameter.parameterName,
    defaultValue: parameter.defaultValue || '',
    valueSource: 'CALLER',
    sortNo: index + 1
  }))
}

async function fetchBusinessApiDetail(id: string) {
  return fetchBusinessApi(id)
}

async function openTestDialog(skill: SkillItem) {
  try {
    const detail = await fetchSkill(skill.id)
    testingSkill.value = detail
    testResult.value = null
    testError.value = ''
    Object.keys(testValues).forEach((key) => delete testValues[key])
    detail.parameters.forEach((parameter) => {
      testValues[parameter.parameterName] = parameter.defaultValue || ''
    })
    testDialogVisible.value = true
  } catch (error) {
    showError(error, '加载 Skill 测试配置失败')
  }
}

async function submitTest() {
  if (!testingSkill.value) {
    return
  }
  testing.value = true
  testError.value = ''
  testResult.value = null
  try {
    testResult.value = await testSkill(testingSkill.value.id, { ...testValues })
  } catch (error) {
    testError.value = getErrorMessage(error, 'Skill 测试失败')
  } finally {
    testing.value = false
  }
}

function resetForm() {
  Object.assign(skillForm, {
    skillCode: '',
    skillName: '',
    description: '',
    apiId: '',
    permissionCode: '',
    timeoutMs: 10000,
    maxResultCount: 100,
    status: 1,
    parameters: []
  })
}

function normalizePayload(): SkillPayload {
  return {
    ...skillForm,
    permissionCode: skillForm.permissionCode || null,
    parameters: skillForm.parameters
      .filter((parameter) => parameter.parameterName.trim())
      .map((parameter, index) => ({
        ...parameter,
        parameterName: parameter.parameterName.trim(),
        apiParameterName: parameter.apiParameterName.trim(),
        defaultValue: parameter.defaultValue || null,
        description: parameter.description || null,
        sortNo: index + 1
      }))
  }
}

function copyParameter(parameter: SkillParameterItem): SkillParameterItem {
  return {
    ...parameter,
    defaultValue: parameter.defaultValue || '',
    description: parameter.description || '',
    valueSource: parameter.valueSource || 'CALLER'
  }
}

function testPlaceholder(parameter: SkillParameterItem) {
  return parameter.valueSource === 'CONSTANT' ? '固定值由默认值提供' : `${parameter.parameterType}${parameter.required === 1 ? ' / 必填' : ''}`
}

function formatResult(value: unknown) {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  if (typeof value === 'string') {
    return value
  }
  return JSON.stringify(value, null, 2)
}

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

function formatDateTime(value: string | null) {
  return value ? value.replace('T', ' ') : '-'
}

function getErrorMessage(error: unknown, fallback = '操作失败') {
  return error instanceof Error && error.message ? error.message : fallback
}

function showError(error: unknown, fallback = '操作失败') {
  ElMessage.error(getErrorMessage(error, fallback))
}
</script>
