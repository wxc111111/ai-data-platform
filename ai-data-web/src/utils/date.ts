// 格式化后端 LocalDateTime 字符串，避免页面直接显示 ISO 分隔符 T。
export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '-'
  }

  const normalized = value.replace('T', ' ')
  const withoutMilliseconds = normalized.split('.')[0]
  const withoutTimezone = withoutMilliseconds.replace(/Z$/, '')

  return withoutTimezone || '-'
}
