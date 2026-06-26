import { http, type ApiResult } from './http'

export interface PageResult<T> {
  total: number | string
  pageNo: number
  pageSize: number
  records: T[]
}

export interface RoleOption {
  id: string
  roleCode: string
  roleName: string
}

export interface RoleItem {
  id: string
  roleCode: string
  roleName: string
  status: number
  description: string | null
  createdTime: string
  updatedTime: string
}

export interface RoleQuery {
  pageNo: number
  pageSize: number
  roleCode?: string
  status?: number | ''
}

export interface RolePayload {
  roleCode: string
  roleName: string
  status: number
  description?: string
}

// 查询启用角色选项，供用户管理分配角色使用。
export async function fetchRoleOptions(): Promise<RoleOption[]> {
  const response = await http.get<ApiResult<RoleOption[]>>('/roles/options')
  assertSuccess(response.data)
  return response.data.data
}

// 分页查询角色列表，供角色管理页面使用。
export async function fetchRoles(query: RoleQuery): Promise<PageResult<RoleItem>> {
  const response = await http.get<ApiResult<PageResult<RoleItem>>>('/roles', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

// 创建角色。
export async function createRole(payload: RolePayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/roles', payload)
  assertSuccess(response.data)
}

// 更新角色基础信息。
export async function updateRole(id: string, payload: RolePayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/roles/${id}`, payload)
  assertSuccess(response.data)
}

// 更新角色状态。
export async function updateRoleStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/roles/${id}/status`, { status })
  assertSuccess(response.data)
}

// 删除角色。
export async function deleteRole(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/roles/${id}`)
  assertSuccess(response.data)
}

// 查询角色已授权权限 ID。
export async function fetchRolePermissionIds(id: string): Promise<string[]> {
  const response = await http.get<ApiResult<string[]>>(`/roles/${id}/permissions`)
  assertSuccess(response.data)
  return response.data.data
}

// 覆盖保存角色授权。
export async function assignRolePermissions(id: string, permissionIds: string[]): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/roles/${id}/permissions`, { permissionIds })
  assertSuccess(response.data)
}

// 统一解析后端 Result，保留后端业务提示。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
