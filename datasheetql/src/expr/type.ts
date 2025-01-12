
export const enum ExprValueType {
  Number,
  String,
  Boolean,
  DateTime,
  /**
   * array of strings
   */
  Array,
}

export const enum ExprCheckType {
  Error = 100,
}

export type ExprType = ExprValueType | ExprCheckType

export type FuncTypeSpec = {
  params: FuncParamTypeSpec
  ret: ExprType
}

export type FuncParamTypeSpec = {
  positional: Array<ExprType>
  rest?: ExprType
}

export function typeMatches(type1: ExprType, type2: ExprType): boolean {
  if (type1 === ExprCheckType.Error || type2 === ExprCheckType.Error) {
    return true
  }
  return type1 === type2
}