import enUS from "./langs/en_US.lang.json"
import { formatMessage } from "./format"

export type LocalizationProvider = {
  format(key: string, args: Record<string, any>): string
  get language(): string
}

export const DEFAULT_PROVIDER: LocalizationProvider = {
  language: "en_US",
  format(key: string, args: Record<string, any>): string {
    const message = (enUS as Record<string, string>)[key]
    if (message === undefined) {
      return `unrecognized l10n key ${key}`
    }
    return formatMessage(message, args)
  },
}
