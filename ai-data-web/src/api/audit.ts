import { http, type ApiResult } from './http'

export interface PageResult<T> {
  total: number | string
  pageNo: number
  pageSize: number
  records: T[]
}

export interface AuditPageQuery {
  pageNo: number
  pageSize: number
  username?: string
  status?: string
  loginStatus?: string
  requestPath?: string
  startTime?: string
  endTime?: string
}

export interface LoginLogItem {
  id: string
  userId: string | null
  username: string | null
  loginIp: string | null
  userAgent: string | null
  loginStatus: string
  message: string | null
  loginTime: string
}

export interface OperationLogItem {
  id: string
  requestId: string | null
  userId: string | null
  username: string | null
  moduleName: string | null
  operationName: string | null
  requestMethod: string | null
  requestPath: string | null
  requestIp: string | null
  requestParams: string | null
  status: string | null
  durationMs: number | string | null
  errorMessage: string | null
  createdTime: string
}

// 查询登录日志分页列表。
export async function fetchLoginLogs(query: AuditPageQuery): Promise<PageResult<LoginLogItem>> {
  const response = await http.get<ApiResult<PageResult<LoginLogItem>>>('/audit/login-logs', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 查询操作日志分页列表。
export async function fetchOperationLogs(query: AuditPageQuery): Promise<PageResult<OperationLogItem>> {
  const response = await http.get<ApiResult<PageResult<OperationLogItem>>>('/audit/operation-logs', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 统一解析后端 Result，保留后端业务提示。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
