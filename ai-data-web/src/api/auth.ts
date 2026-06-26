import { http, type ApiResult } from './http'

export interface LoginUser {
  id: string
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

export interface AuthMenuItem {
  id: string
  parentId: string
  permissionName: string
  permissionCode: string | null
  permissionType: 'MENU'
  routePath: string | null
  componentPath: string | null
  icon: string | null
  sortNo: number
  status: number
  createdTime: string
  updatedTime: string
  children: AuthMenuItem[]
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

// 获取当前登录用户可见菜单，菜单结构由后端权限表裁剪后返回。
export async function fetchCurrentMenus(): Promise<AuthMenuItem[]> {
  const response = await http.get<ApiResult<AuthMenuItem[]>>('/auth/menus')

  if (response.data.code !== 200) {
    throw new Error(response.data.message || '获取菜单失败')
  }

  return response.data.data
}
