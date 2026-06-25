import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const tokenName = localStorage.getItem('ai-data-token-name')
  const tokenValue = localStorage.getItem('ai-data-token-value')

  if (tokenName && tokenValue) {
    config.headers[tokenName] = `Bearer ${tokenValue}`
  }

  return config
})

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  requestId?: string
}

export { http }
