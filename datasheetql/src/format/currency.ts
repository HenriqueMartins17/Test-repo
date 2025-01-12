import { ExprValue } from "../ast"
import { ExprValueType, NameResolver } from "../expr"

const CURRENCY_SYMBOLS: Record<string, string> = {
  RMB: "￥",
  CNY: "￥",
  JPY: "￥",
  USD: "＄",
  EUR: "€",
  GBP: "￡",
}

export const CurrencyResolver: NameResolver = {
  varType(name: string): ExprValueType | null {
    if (Object.hasOwn(CURRENCY_SYMBOLS, name.toUpperCase())) {
      return ExprValueType.String
    } else {
      return null
    }
  },
  varValue(name: string): ExprValue | null {
    const sym = CURRENCY_SYMBOLS[name.toUpperCase()]
    return sym !== undefined ? sym : null
  },
  funcType() {
    return null
  }
}
