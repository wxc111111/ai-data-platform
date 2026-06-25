import { http, type ApiResult } from './http'

export interface LoginUser {
  id: number
  username: string
  nickname: string
}

export interface LoginSession {
  tokenName: string
  tokenValue: string
  user: LoginUser
  roles: string[]
  permissions: string[]
}

export async function login(username: string, password: string): Promise<LoginSession> {
  const response = await http.post<ApiResult<LoginSession>>('/auth/login', {
    username,
    password
  })

  if (response.data.code !== 200) {
    throw new Error(response.data.message || '登录失败')
  }

  return response.data.data
}
