import { ExprValue } from "../ast"
import { ExprValueType, FuncTypeSpec } from "./type"

export type NameResolver = {
  varType(name: string): ExprValueType | null
  varValue(name: string): ExprValue | null
  funcType(name: string): NonEmptyArray<FuncTypeSpec> | null
}


export const DUMMY_NAME_RESOLVER: NameResolver = {
  varType() {
    return null
  },
  varValue() {
    return null
  },
  funcType() {
    return null
  },
}
