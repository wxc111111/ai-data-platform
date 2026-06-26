import { http, type ApiResult } from './http'

export interface PermissionItem {
  id: string
  parentId: string
  permissionName: string
  permissionCode: string | null
  permissionType: 'MENU' | 'BUTTON' | 'API'
  routePath: string | null
  componentPath: string | null
  icon: string | null
  sortNo: number
  status: number
  createdTime: string
  updatedTime: string
  children: PermissionItem[]
}

export interface PermissionPayload {
  parentId: string
  permissionName: string
  permissionCode?: string
  permissionType: 'MENU' | 'BUTTON' | 'API'
  routePath?: string
  componentPath?: string
  icon?: string
  sortNo: number
  status: number
}

// 查询权限树，供权限管理和角色授权复用。
export async function fetchPermissionTree(): Promise<PermissionItem[]> {
  const response = await http.get<ApiResult<PermissionItem[]>>('/permissions/tree')
  assertSuccess(response.data)
  return response.data.data
}

// 新增权限节点。
export async function createPermission(payload: PermissionPayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/permissions', payload)
  assertSuccess(response.data)
}

// 更新权限节点。
export async function updatePermission(id: string, payload: PermissionPayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/permissions/${id}`, payload)
  assertSuccess(response.data)
}

// 更新权限状态。
export async function updatePermissionStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/permissions/${id}/status`, { status })
  assertSuccess(response.data)
}

// 删除权限节点。
export async function deletePermission(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/permissions/${id}`)
  assertSuccess(response.data)
}

// 统一解析后端 Result，保留后端业务提示。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
