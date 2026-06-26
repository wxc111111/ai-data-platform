import { http, type ApiResult } from './http'

export interface PageResult<T> {
  total: number | string
  pageNo: number
  pageSize: number
  records: T[]
}

export interface UserItem {
  id: string
  username: string
  nickname: string | null
  mobile: string | null
  email: string | null
  status: number
  lastLoginTime: string | null
  createdTime: string
  updatedTime: string
  roleIds: string[]
}

export interface UserQuery {
  pageNo: number
  pageSize: number
  username?: string
  mobile?: string
  status?: number | ''
}

export interface UserPayload {
  username?: string
  password?: string
  nickname?: string
  mobile?: string
  email?: string
  status: number
  roleIds?: string[]
}

export async function fetchUsers(query: UserQuery): Promise<PageResult<UserItem>> {
  const response = await http.get<ApiResult<PageResult<UserItem>>>('/users', { params: query })
  assertSuccess(response.data)
  return response.data.data
}

export async function createUser(payload: UserPayload): Promise<void> {
  const response = await http.post<ApiResult<null>>('/users', payload)
  assertSuccess(response.data)
}

export async function updateUser(id: string, payload: UserPayload): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/users/${id}`, payload)
  assertSuccess(response.data)
}

export async function updateUserStatus(id: string, status: number): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/users/${id}/status`, { status })
  assertSuccess(response.data)
}

export async function deleteUser(id: string): Promise<void> {
  const response = await http.delete<ApiResult<null>>(`/users/${id}`)
  assertSuccess(response.data)
}

export async function assignUserRoles(id: string, roleIds: string[]): Promise<void> {
  const response = await http.put<ApiResult<null>>(`/users/${id}/roles`, { roleIds })
  assertSuccess(response.data)
}

function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
