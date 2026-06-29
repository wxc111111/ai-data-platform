import { http, type ApiResult } from './http'
import type { BusinessApiTestResponse } from './business-apis'
import type { PageResult } from './users'

export interface SkillParameterItem {
  id?: string
  skillId?: string
  parameterName: string
  parameterType: string
  required: number
  description?: string | null
  apiParameterName: string
  defaultValue?: string | null
  valueSource: 'CALLER' | 'CONSTANT'
  sortNo: number
  createdTime?: string
  updatedTime?: string
}

export interface SkillItem {
  id: string
  skillCode: string
  skillName: string
  description: string
  apiId: string
  apiName: string | null
  apiCode: string | null
  permissionCode: string | null
  timeoutMs: number
  maxResultCount: number
  status: number
  versionNo: number
  createdTime: string
  updatedTime: string
  parameters: SkillParameterItem[]
}

export interface SkillQuery {
  pageNo: number
  pageSize: number
  skillName?: string
  skillCode?: string
  status?: number | ''
}

export interface SkillPayload {
  skillCode: string
  skillName: string
  description: string
  apiId: string
  permissionCode?: string | null
  timeoutMs?: number | null
  maxResultCount?: number | null
  status: number
  parameters: SkillParameterItem[]
}

// 查询 Skill 分页列表。
export async function fetchSkills(query: SkillQuery): Promise<PageResult<SkillItem>> {
  const response = await http.get<ApiResult<PageResult<SkillItem>>>('/skills', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 查询 Skill 详情，包含参数映射。
export async function fetchSkill(id: string): Promise<SkillItem> {
  const response = await http.get<ApiResult<SkillItem>>(`/skills/${id}`)
  assertSuccess(response.data)
  return response.data.data
}

// 新增 Skill 配置。
export async function createSkill(payload: SkillPayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/skills', payload)
  assertSuccess(response.data)
}

// 更新 Skill 配置和参数映射。
export async function updateSkill(id: string, payload: SkillPayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/skills/${id}`, payload)
  assertSuccess(response.data)
}

// 启用或禁用 Skill。
export async function updateSkillStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/skills/${id}/status`, { status })
  assertSuccess(response.data)
}

// 删除 Skill 配置。
export async function deleteSkill(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/skills/${id}`)
  assertSuccess(response.data)
}

// 发起 Skill 在线测试。
export async function testSkill(id: string, parameterValues: Record<string, unknown>): Promise<BusinessApiTestResponse> {
  const response = await http.post<ApiResult<BusinessApiTestResponse>>(`/skills/${id}/test`, { parameterValues })
  assertSuccess(response.data)
  return response.data.data
}

// 统一处理后端业务错误。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
