import { http, type ApiResult } from './http'
import type { PageResult } from './users'

export interface BusinessSystemItem {
  id: string
  systemCode: string
  systemName: string
  baseUrl: string
  authType: string
  authConfig: string | null
  connectTimeout: number
  readTimeout: number
  status: number
  description: string | null
  createdBy: string | null
  updatedBy: string | null
  roleIds: string[]
  createdTime: string
  updatedTime: string
}

export interface BusinessSystemQuery {
  pageNo: number
  pageSize: number
  systemName?: string
  systemCode?: string
  status?: number | ''
}

export interface BusinessSystemPayload {
  systemCode: string
  systemName: string
  baseUrl: string
  authType: string
  authConfig?: string | null
  connectTimeout: number
  readTimeout: number
  status: number
  description?: string
  roleIds: string[]
}

// 查询业务系统分页列表，供管理页面表格展示。
export async function fetchBusinessSystems(query: BusinessSystemQuery): Promise<PageResult<BusinessSystemItem>> {
  const response = await http.get<ApiResult<PageResult<BusinessSystemItem>>>('/business-systems', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 查询业务系统详情，用于编辑前回填完整认证配置。
export async function fetchBusinessSystem(id: string): Promise<BusinessSystemItem> {
  const response = await http.get<ApiResult<BusinessSystemItem>>(`/business-systems/${id}`)
  assertSuccess(response.data)
  return response.data.data
}

// 新增业务系统配置。
export async function createBusinessSystem(payload: BusinessSystemPayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/business-systems', payload)
  assertSuccess(response.data)
}

// 更新业务系统配置。
export async function updateBusinessSystem(id: string, payload: BusinessSystemPayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/business-systems/${id}`, payload)
  assertSuccess(response.data)
}

// 启用或禁用业务系统。
export async function updateBusinessSystemStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/business-systems/${id}/status`, { status })
  assertSuccess(response.data)
}

// 删除业务系统。
export async function deleteBusinessSystem(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/business-systems/${id}`)
  assertSuccess(response.data)
}

// 统一处理后端业务错误，保持和现有 API 文件一致的异常模型。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
