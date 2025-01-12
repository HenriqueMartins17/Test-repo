export function formatMessage(message: string, args: Record<string, any>): string {
  return message
    .replace(/\{(\w*)(?:\[(\d+)\])?\}/g, (_m, key, index) => {
      if (Object.hasOwn(args, key)) {
        let value = args[key]
        if (index !== undefined) {
          if (Array.isArray(value)) {
            value = value[+index]
          } else {
            return `{ERROR--${key}[${+index}]}`
          }
        }
        return formatValue(value)
      } else {
        return index !== null ? `{ERROR--${key}[${+index}]}` : `{ERROR--${key}}`
      }
    })
    .replace(/\\([\\{}])/g, (_m, g1) => g1)
}

function formatValue(value: any): string {
  if (value === null || typeof value !== "object") {
    return String(value)
  }
  if (value instanceof Error) {
    return value.message
  } else if (value.toString !== Object.prototype.toString) {
    return String(value)
  } else {
    return JSON.stringify(value)
  }
}
