import { defineStore } from 'pinia'
import { login as loginApi, type LoginSession, type LoginUser } from '@/api/auth'

interface AuthState {
  tokenName: string
  tokenValue: string
  user: LoginUser | null
  roles: string[]
  permissions: string[]
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    tokenName: localStorage.getItem('ai-data-token-name') || '',
    tokenValue: localStorage.getItem('ai-data-token-value') || '',
    user: readJson<LoginUser>('ai-data-user'),
    roles: readJson<string[]>('ai-data-roles') || [],
    permissions: readJson<string[]>('ai-data-permissions') || []
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.tokenValue)
  },
  actions: {
    async login(username: string, password: string) {
      const session = await loginApi(username, password)
      this.setSession(session)
    },
    setSession(session: LoginSession) {
      this.tokenName = session.tokenName
      this.tokenValue = session.tokenValue
      this.user = session.user
      this.roles = session.roles
      this.permissions = session.permissions

      localStorage.setItem('ai-data-token-name', session.tokenName)
      localStorage.setItem('ai-data-token-value', session.tokenValue)
      localStorage.setItem('ai-data-user', JSON.stringify(session.user))
      localStorage.setItem('ai-data-roles', JSON.stringify(session.roles))
      localStorage.setItem('ai-data-permissions', JSON.stringify(session.permissions))
    },
    logout() {
      this.tokenName = ''
      this.tokenValue = ''
      this.user = null
      this.roles = []
      this.permissions = []

      localStorage.removeItem('ai-data-token-name')
      localStorage.removeItem('ai-data-token-value')
      localStorage.removeItem('ai-data-user')
      localStorage.removeItem('ai-data-roles')
      localStorage.removeItem('ai-data-permissions')
    }
  }
})

function readJson<T>(key: string): T | null {
  const value = localStorage.getItem(key)
  if (!value) {
    return null
  }

  try {
    return JSON.parse(value) as T
  } catch {
    localStorage.removeItem(key)
    return null
  }
}
