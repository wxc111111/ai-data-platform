<template>
  <section class="user-page">
    <header class="page-toolbar">
      <div>
        <h2>登录日志</h2>
        <p>查看后台用户登录成功和失败记录。</p>
      </div>
    </header>

    <section class="filter-bar audit-filter-bar login-log-filter">
      <el-input v-model="query.username" clearable placeholder="用户名" />
      <el-select v-model="query.loginStatus" clearable placeholder="登录状态">
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
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="loginIp" label="登录 IP" min-width="140" />
      <el-table-column label="状态" width="96">
        <template #default="{ row }">
          <el-tag :type="row.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
            {{ row.loginStatus === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="message" label="结果说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="userAgent" label="User-Agent" min-width="260" show-overflow-tooltip />
      <el-table-column label="登录时间" min-width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.loginTime) }}
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
import { fetchLoginLogs, type LoginLogItem } from '@/api/audit'
import { formatDateTime } from '@/utils/date'

const loading = ref(false)
const logs = ref<LoginLogItem[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  username: '',
  loginStatus: ''
})

// 加载登录日志列表，并把时间范围转换成后端查询参数。
async function loadLogs() {
  loading.value = true
  try {
    const page = await fetchLoginLogs({
      ...query,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1]
    })
    logs.value = page.records
    total.value = Number(page.total)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载登录日志失败')
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
  query.loginStatus = ''
  timeRange.value = null
  loadLogs()
}

onMounted(loadLogs)
</script>
