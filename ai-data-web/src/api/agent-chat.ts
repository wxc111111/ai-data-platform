import { http, type ApiResult } from './http'

export interface AgentUsedSkill {
  skillId: string
  skillCode: string
  skillName: string
  input: Record<string, unknown>
  output: string
}

export interface AgentChatSession {
  id: string
  title: string
  lastMessageTime: string
  createdTime: string
}

export interface AgentChatMessage {
  id: string
  sessionId: string
  messageRole: 'USER' | 'ASSISTANT'
  content: string
  usedSkills: AgentUsedSkill[]
  createdTime: string
}

export interface AgentChatResponse {
  sessionId: string
  answer: string
  usedSkills: AgentUsedSkill[]
}

export interface AgentChatStreamEvent {
  type: 'session' | 'delta' | 'done' | 'error'
  sessionId: string | null
  content: string | null
  usedSkills: AgentUsedSkill[]
}

// 查询当前用户的 AI 问答历史会话。
export async function fetchAgentChatSessions(): Promise<AgentChatSession[]> {
  const response = await http.get<ApiResult<AgentChatSession[]>>('/agent-chat/sessions')
  assertSuccess(response.data)
  return response.data.data
}

// 查询指定会话的历史消息。
export async function fetchAgentChatMessages(sessionId: string): Promise<AgentChatMessage[]> {
  const response = await http.get<ApiResult<AgentChatMessage[]>>(`/agent-chat/sessions/${sessionId}/messages`)
  assertSuccess(response.data)
  return response.data.data
}

// 非流式发送用户消息，保留给兼容场景使用。
export async function sendAgentChatMessage(sessionId: string | null, message: string): Promise<AgentChatResponse> {
  const response = await http.post<ApiResult<AgentChatResponse>>('/agent-chat/chat', { sessionId, message })
  assertSuccess(response.data)
  return response.data.data
}

// 流式发送用户消息，逐个解析服务端 SSE data 事件。
export async function streamAgentChatMessage(
  sessionId: string | null,
  message: string,
  onEvent: (event: AgentChatStreamEvent) => void
): Promise<void> {
  const response = await fetch('/api/agent-chat/chat/stream', {
    method: 'POST',
    headers: buildStreamHeaders(),
    body: JSON.stringify({ sessionId, message })
  })

  if (!response.ok || !response.body) {
    throw new Error('AI 问答流式请求失败')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    buffer = consumeSseBuffer(buffer, onEvent)
  }

  consumeSseBuffer(buffer + '\n\n', onEvent)
}

// 复用登录后保存的 Sa-Token 请求头。
function buildStreamHeaders(): HeadersInit {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'text/event-stream'
  }
  const tokenName = localStorage.getItem('ai-data-token-name')
  const tokenValue = localStorage.getItem('ai-data-token-value')
  if (tokenName && tokenValue) {
    headers[tokenName] = `Bearer ${tokenValue}`
  }
  return headers
}

// 消费完整的 SSE 事件块，返回未读完的半包内容。
function consumeSseBuffer(buffer: string, onEvent: (event: AgentChatStreamEvent) => void): string {
  let rest = buffer
  let separatorIndex = findSseSeparator(rest)
  while (separatorIndex >= 0) {
    const block = rest.slice(0, separatorIndex)
    rest = rest.slice(separatorIndex + separatorLength(rest, separatorIndex))
    emitSseBlock(block, onEvent)
    separatorIndex = findSseSeparator(rest)
  }
  return rest
}

// 兼容 LF 和 CRLF 两种 SSE 分隔。
function findSseSeparator(buffer: string): number {
  const lfIndex = buffer.indexOf('\n\n')
  const crlfIndex = buffer.indexOf('\r\n\r\n')
  if (lfIndex < 0) {
    return crlfIndex
  }
  if (crlfIndex < 0) {
    return lfIndex
  }
  return Math.min(lfIndex, crlfIndex)
}

// 根据实际分隔符长度裁剪缓冲区。
function separatorLength(buffer: string, index: number): number {
  return buffer.slice(index, index + 4) === '\r\n\r\n' ? 4 : 2
}

// 解析 SSE data 行，后端只发送 JSON data。
function emitSseBlock(block: string, onEvent: (event: AgentChatStreamEvent) => void) {
  const normalized = block.replace(/\r\n/g, '\n')
  const data = normalized
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).trimStart())
    .join('\n')
  if (!data) {
    return
  }
  const event = JSON.parse(data) as AgentChatStreamEvent
  if (event.type === 'error') {
    throw new Error(event.content || 'AI 问答失败')
  }
  onEvent(event)
}

// 统一处理后端业务错误。
function assertSuccess<T>(result: ApiResult<T>) {
  if (result.code !== 200) {
    throw new Error(result.message || '操作失败')
  }
}
