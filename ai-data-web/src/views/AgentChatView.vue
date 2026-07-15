<template>
  <section class="agent-chat-page">
    <aside class="chat-sessions">
      <div class="session-toolbar">
        <h2>AI问答</h2>
        <div class="session-actions">
          <el-tooltip content="刷新" placement="bottom">
            <el-button :icon="Refresh" circle @click="loadSessions" />
          </el-tooltip>
          <el-tooltip content="新会话" placement="bottom">
            <el-button type="primary" :icon="Plus" circle @click="startNewSession" />
          </el-tooltip>
        </div>
      </div>

      <el-scrollbar class="session-scroll">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: activeSessionId === session.id }"
          type="button"
          @click="selectSession(session.id)"
        >
          <span>{{ session.title }}</span>
          <small>{{ formatDateTime(session.lastMessageTime) }}</small>
        </button>
        <el-empty v-if="!sessionLoading && sessions.length === 0" description="暂无会话" :image-size="88" />
      </el-scrollbar>
    </aside>

    <section class="chat-main">
      <div class="message-list" v-loading="messageLoading">
        <el-scrollbar ref="messageScrollbar" class="message-scroll">
          <div v-if="messages.length === 0" class="chat-empty">
            <el-icon><ChatLineRound /></el-icon>
            <span>新会话</span>
          </div>

          <article
            v-for="message in messages"
            :key="message.id"
            class="chat-message"
            :class="message.messageRole === 'USER' ? 'from-user' : 'from-agent'"
          >
            <div class="message-bubble">
              <pre v-if="message.messageRole === 'USER'" class="plain-message">{{ message.content }}</pre>
              <!-- AI 回复经过 Markdown 解析和 XSS 过滤后展示，支持流式内容实时更新。 -->
              <div v-else class="markdown-message" v-html="renderMarkdown(message.content)"></div>
              <span v-if="sending && message.id === streamingMessageId && !message.content" class="typing-text">
                正在生成
              </span>
              <div v-if="message.usedSkills.length > 0" class="used-skills">
                <el-tag v-for="skill in message.usedSkills" :key="`${message.id}-${skill.skillCode}`" size="small">
                  {{ skill.skillName || skill.skillCode }}
                </el-tag>
              </div>
            </div>
          </article>
        </el-scrollbar>
      </div>

      <footer class="chat-composer">
        <el-input
          v-model="draft"
          type="textarea"
          resize="none"
          :rows="3"
          maxlength="2000"
          show-word-limit
          placeholder="输入问题"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" :icon="Position" :loading="sending" @click="sendMessage">发送</el-button>
      </footer>
    </section>
  </section>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ChatLineRound, Plus, Position, Refresh } from '@element-plus/icons-vue'
import { ElMessage, type ScrollbarInstance } from 'element-plus'
import {
  fetchAgentChatMessages,
  fetchAgentChatSessions,
  streamAgentChatMessage,
  type AgentChatMessage,
  type AgentChatSession,
  type AgentChatStreamEvent
} from '@/api/agent-chat'
import { formatDateTime } from '@/utils/date'
import { renderMarkdown } from '@/utils/markdown'
import 'highlight.js/styles/github.css'

const sessions = ref<AgentChatSession[]>([])
const messages = ref<AgentChatMessage[]>([])
const activeSessionId = ref<string | null>(null)
const draft = ref('')
const sessionLoading = ref(false)
const messageLoading = ref(false)
const sending = ref(false)
const streamingMessageId = ref<string | null>(null)
const messageScrollbar = ref<ScrollbarInstance>()

onMounted(() => {
  void loadSessions()
})

// 打开页面时加载历史会话，并默认展示最近一条会话的消息。
async function loadSessions() {
  sessionLoading.value = true
  try {
    sessions.value = await fetchAgentChatSessions()
    if (!activeSessionId.value && sessions.value.length > 0) {
      await selectSession(sessions.value[0].id)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '获取会话失败')
  } finally {
    sessionLoading.value = false
  }
}

// 切换左侧会话时重新读取右侧消息历史。
async function selectSession(sessionId: string) {
  activeSessionId.value = sessionId
  messageLoading.value = true
  try {
    messages.value = await fetchAgentChatMessages(sessionId)
    await scrollToBottom()
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '获取消息失败')
  } finally {
    messageLoading.value = false
  }
}

// 新会话只清空当前输入区和消息区，真正的会话 ID 由首次发送时后端创建。
function startNewSession() {
  activeSessionId.value = null
  messages.value = []
  draft.value = ''
  streamingMessageId.value = null
}

// 发送用户消息后创建助手占位消息，随后把服务端流式增量追加到该消息。
async function sendMessage() {
  const content = draft.value.trim()
  if (!content || sending.value) {
    return
  }

  sending.value = true
  draft.value = ''
  const now = Date.now()
  const userTempId = `user-${now}`
  const assistantTempId = `assistant-${now}`
  streamingMessageId.value = assistantTempId
  messages.value.push(createMessage(userTempId, activeSessionId.value || userTempId, 'USER', content))
  messages.value.push(createMessage(assistantTempId, activeSessionId.value || assistantTempId, 'ASSISTANT', ''))
  await scrollToBottom()

  try {
    await streamAgentChatMessage(activeSessionId.value, content, handleStreamEvent)
    if (activeSessionId.value) {
      // 回答完成后只更新左侧会话摘要，保留右侧流式消息，避免加载遮罩和列表跳动。
      await loadSessions()
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '发送失败')
  } finally {
    sending.value = false
    streamingMessageId.value = null
    await scrollToBottom()
  }
}

// 根据后端 SSE 事件更新当前会话和助手消息内容。
function handleStreamEvent(event: AgentChatStreamEvent) {
  if (event.type === 'session' && event.sessionId) {
    activeSessionId.value = event.sessionId
    updateStreamingSessionId(event.sessionId)
    return
  }

  const assistant = messages.value.find((message) => message.id === streamingMessageId.value)
  if (!assistant) {
    return
  }

  if (event.type === 'delta' && event.content) {
    assistant.content += event.content
    void scrollToBottom()
  }

  if (event.type === 'done') {
    assistant.usedSkills = event.usedSkills || []
  }
}

// 新会话创建成功后，把临时消息的 sessionId 替换成后端真实会话 ID。
function updateStreamingSessionId(sessionId: string) {
  for (const message of messages.value) {
    if (message.sessionId.startsWith('user-') || message.sessionId.startsWith('assistant-')) {
      message.sessionId = sessionId
    }
  }
}

// 统一创建前端临时消息，当前页面保留流式结果，重新打开会话时再读取后端历史记录。
function createMessage(
  id: string,
  sessionId: string,
  role: AgentChatMessage['messageRole'],
  content: string
): AgentChatMessage {
  return {
    id,
    sessionId,
    messageRole: role,
    content,
    usedSkills: [],
    createdTime: new Date().toISOString()
  }
}

// 消息追加后保持滚动条在底部，避免新回复被隐藏。
async function scrollToBottom() {
  await nextTick()
  messageScrollbar.value?.setScrollTop(Number.MAX_SAFE_INTEGER)
}
</script>

<style scoped>
.agent-chat-page {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
  height: calc(100vh - 64px);
  min-height: 560px;
  padding: 16px;
}

.chat-sessions,
.chat-main {
  min-height: 0;
  border: 1px solid #e5ebf3;
  border-radius: 8px;
  background: #ffffff;
}

.chat-sessions {
  display: flex;
  flex-direction: column;
}

.session-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 14px;
  border-bottom: 1px solid #e5ebf3;
}

.session-toolbar h2 {
  margin: 0;
  color: #1d2939;
  font-size: 18px;
}

.session-actions {
  display: flex;
  gap: 8px;
}

.session-scroll {
  flex: 1;
}

.session-item {
  display: flex;
  width: calc(100% - 16px);
  min-height: 64px;
  margin: 8px;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #1d2939;
  cursor: pointer;
  flex-direction: column;
  gap: 8px;
  text-align: left;
}

.session-item:hover,
.session-item.active {
  border-color: #bcd4ff;
  background: #eef5ff;
}

.session-item span {
  overflow: hidden;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item small {
  color: #667085;
}

.chat-main {
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
}

.message-list,
.message-scroll {
  min-height: 0;
}

.message-scroll {
  height: 100%;
}

.chat-empty {
  display: grid;
  height: 100%;
  min-height: 320px;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #98a2b3;
}

.chat-empty .el-icon {
  font-size: 34px;
}

.chat-message {
  display: flex;
  padding: 12px 18px;
}

.chat-message.from-user {
  justify-content: flex-end;
}

.chat-message.from-agent {
  justify-content: flex-start;
}

.message-bubble {
  max-width: min(720px, 78%);
  border-radius: 8px;
  padding: 12px 14px;
  background: #f3f6fb;
  color: #1d2939;
}

.from-user .message-bubble {
  background: #155bd7;
  color: #ffffff;
}

.plain-message {
  margin: 0;
  font-family: inherit;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.markdown-message {
  line-height: 1.7;
  overflow-wrap: anywhere;
}

.markdown-message :deep(p) {
  margin: 0 0 10px;
}

.markdown-message :deep(p:last-child),
.markdown-message :deep(ul:last-child),
.markdown-message :deep(ol:last-child),
.markdown-message :deep(pre:last-child) {
  margin-bottom: 0;
}

.markdown-message :deep(ul),
.markdown-message :deep(ol) {
  margin: 8px 0 10px;
  padding-left: 24px;
}

.markdown-message :deep(code) {
  border-radius: 4px;
  padding: 2px 5px;
  background: #e7ebf2;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 0.92em;
}

.markdown-message :deep(pre) {
  max-width: 100%;
  margin: 10px 0;
  border: 1px solid #d8dee9;
  border-radius: 6px;
  background: #f8fafc;
  overflow-x: auto;
}

.markdown-message :deep(pre code) {
  display: block;
  min-width: max-content;
  padding: 14px 16px;
  background: transparent;
  line-height: 1.6;
  white-space: pre;
}

.markdown-message :deep(a) {
  color: #155bd7;
  text-decoration: underline;
}

.typing-text {
  color: #667085;
}

.used-skills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.chat-composer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 92px;
  gap: 12px;
  align-items: end;
  padding: 14px;
  border-top: 1px solid #e5ebf3;
}

.chat-composer .el-button {
  height: 54px;
}

@media (max-width: 860px) {
  .agent-chat-page {
    grid-template-columns: 1fr;
    height: auto;
  }

  .chat-sessions {
    min-height: 260px;
  }

  .chat-main {
    min-height: 560px;
  }

  .chat-composer {
    grid-template-columns: 1fr;
  }

  .message-bubble {
    max-width: 92%;
  }
}
</style>
