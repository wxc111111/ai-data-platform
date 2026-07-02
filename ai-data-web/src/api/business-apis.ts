import { http, type ApiResult } from './http'
import type { PageResult } from './users'

export interface BusinessApiParameterItem {
  id?: string
  apiId?: string
  parameterName: string
  parameterLocation: 'PATH' | 'QUERY' | 'HEADER' | 'BODY'
  parameterType: string
  required: number
  defaultValue?: string | null
  description?: string | null
  sortNo: number
  createdTime?: string
  updatedTime?: string
}

export interface BusinessApiItem {
  id: string
  systemId: string
  systemName: string | null
  systemBaseUrl: string | null
  fullRequestUrl: string | null
  apiCode: string
  apiName: string
  requestPath: string
  requestMethod: string
  contentType: string | null
  connectTimeout: number | null
  readTimeout: number | null
  responseDataPath: string | null
  status: number
  description: string | null
  createdBy: string | null
  updatedBy: string | null
  roleIds: string[]
  createdTime: string
  updatedTime: string
  parameters: BusinessApiParameterItem[]
}

export interface BusinessApiQuery {
  pageNo: number
  pageSize: number
  systemId?: string
  apiName?: string
  apiCode?: string
  status?: number | ''
}

export interface BusinessApiPayload {
  systemId: string
  apiCode: string
  apiName: string
  requestPath: string
  requestMethod: string
  contentType?: string | null
  connectTimeout?: number | null
  readTimeout?: number | null
  responseDataPath?: string | null
  status: number
  description?: string
  roleIds: string[]
  parameters: BusinessApiParameterItem[]
}

export interface BusinessApiTestResponse {
  statusCode: number
  headers: Record<string, string[]>
  body: string
  extractedData: unknown
  costMs: number
}

// 查询业务接口分页列表。
export async function fetchBusinessApis(query: BusinessApiQuery): Promise<PageResult<BusinessApiItem>> {
  const response = await http.get<ApiResult<PageResult<BusinessApiItem>>>('/business-apis', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 查询业务接口详情，包含参数定义。
export async function fetchBusinessApi(id: string): Promise<BusinessApiItem> {
  const response = await http.get<ApiResult<BusinessApiItem>>(`/business-apis/${id}`)
  assertSuccess(response.data)
  return response.data.data
}

// 新增业务接口配置。
export async function createBusinessApi(payload: BusinessApiPayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/business-apis', payload)
  assertSuccess(response.data)
}

// 更新业务接口配置和参数定义。
export async function updateBusinessApi(id: string, payload: BusinessApiPayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/business-apis/${id}`, payload)
  assertSuccess(response.data)
}

// 启用或禁用业务接口。
export async function updateBusinessApiStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/business-apis/${id}/status`, { status })
  assertSuccess(response.data)
}

// 删除业务接口配置。
export async function deleteBusinessApi(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/business-apis/${id}`)
  assertSuccess(response.data)
}

// 发起业务接口在线测试。
export async function testBusinessApi(id: string, parameterValues: Record<string, unknown>): Promise<BusinessApiTestResponse> {
  const response = await http.post<ApiResult<BusinessApiTestResponse>>(`/business-apis/${id}/test`, { parameterValues })
  assertSuccess(response.data)
  return response.data.data
}

// 统一处理后端业务错误。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
