<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>操作日志</h2>
        <p>查看后台接口访问、执行结果和异常信息。</p>
      </div>
    </header>

    <section class="filter-bar audit-filter-bar operation-log-filter">
      <el-input v-model="query.username" clearable placeholder="用户名" />
      <el-input v-model="query.requestPath" clearable placeholder="请求路径" />
      <el-select v-model="query.status" clearable placeholder="执行状态">
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAILED" />
      </el-select>
      <el-date-picker
        v-model="timeRange"
        class="audit-time-range"
        type="datetimerange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
      />
      <div class="filter-actions">
        <el-button type="primary" :icon="Search" @click="searchLogs">查询</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </div>
    </section>

    <el-table v-loading="loading" :data="logs" class="data-table" row-key="id">
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="moduleName" label="模块" min-width="120" />
      <el-table-column prop="requestMethod" label="方法" width="90" />
      <el-table-column prop="requestPath" label="路径" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="durationMs" label="耗时(ms)" width="110" />
      <el-table-column prop="requestIp" label="来源 IP" min-width="140" />
      <el-table-column prop="requestParams" label="请求参数" min-width="220" show-overflow-tooltip />
      <el-table-column prop="errorMessage" label="异常信息" min-width="220" show-overflow-tooltip />
      <el-table-column prop="requestId" label="Request ID" min-width="260" show-overflow-tooltip />
      <el-table-column label="操作时间" min-width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createdTime) }}
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
        @size-change="loadLogs"
        @current-change="loadLogs"
      />
    </footer>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { fetchOperationLogs, type OperationLogItem } from '@/api/audit'
import { formatDateTime } from '@/utils/date'

const loading = ref(false)
const logs = ref<OperationLogItem[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  requestPath: '',
  status: ''
})

// 加载操作日志列表，并把时间范围转换成后端查询参数。
async function loadLogs() {
  loading.value = true
  try {
    const page = await fetchOperationLogs({
      ...query,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1]
    })
    logs.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载操作日志失败')
  } finally {
    loading.value = false
  }
}

// 从第一页重新查询。
function searchLogs() {
  query.pageNo = 1
  loadLogs()
}

// 清空查询条件并重新加载列表。
function resetQuery() {
  query.pageNo = 1
  query.pageSize = 10
  query.username = ''
  query.requestPath = ''
  query.status = ''
  timeRange.value = null
  loadLogs()
}

onMounted(loadLogs)
</script>
