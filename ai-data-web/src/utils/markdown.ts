import DOMPurify from 'dompurify'
import hljs from 'highlight.js'
import MarkdownIt from 'markdown-it'

// AI 回复使用统一的 Markdown 实例，关闭原始 HTML，避免模型内容直接注入页面。
const markdownRenderer = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true
})

// 对声明了合法语言的代码块进行语法高亮，未知语言按纯文本安全转义。
markdownRenderer.options.highlight = (code: string, language: string): string => {
  if (language && hljs.getLanguage(language)) {
    return hljs.highlight(code, { language, ignoreIllegals: true }).value
  }
  return markdownRenderer.utils.escapeHtml(code)
}

// 将 AI 返回的 Markdown 转换为经过安全过滤的 HTML，供消息组件展示。
export function renderMarkdown(content: string): string {
  const renderedHtml = markdownRenderer.render(content || '')
  return DOMPurify.sanitize(renderedHtml, {
    USE_PROFILES: { html: true }
  })
}
