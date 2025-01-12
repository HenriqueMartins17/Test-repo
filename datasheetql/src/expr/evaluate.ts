import { BinaryOpKind, Expr, ExprKind, ExprValue, UnaryOpKind } from "../ast"
import { Range } from "../diagnostic"
import { ExprValueType, getValueType, NameResolver } from "."

export type EvalError =
  | {
      kind: EvalErrorKind.VarNotFound
      name: string,
      range: Range
    }
  | {
      kind: EvalErrorKind.FnNotFound
      name: string,
      range: Range
    }
  | {
      kind: EvalErrorKind.TypeMismatch
      range: Range
      expected: ExprValueType[]
      actual: ExprValueType
    }
  | {
      kind: EvalErrorKind.NaN
      range: Range
    }
  | {
      kind: EvalErrorKind.Infinity
      range: Range
    }

export const enum EvalErrorKind {
  VarNotFound,
  FnNotFound,
  TypeMismatch,
  NaN,
  Infinity,
}

function typeMismatch(range: Range, expected: ExprValueType[], actual: ExprValueType): never {
  throw {
    kind: EvalErrorKind.TypeMismatch,
    range,
    expected,
    actual,
  } as EvalError
}

function checkNumber(range: Range, value: number): number {
  if (isNaN(value)) {
    throw {
      kind: EvalErrorKind.NaN,
      range,
    }
  } else if (!isFinite(value)) {
    throw {
      kind: EvalErrorKind.Infinity,
      range,
    }
  }
  return value
}

/**
 *
 * @throws EvalError
 */
export function evaluate(expr: Expr, varResolver: NameResolver): ExprValue {
  switch (expr.kind) {
    case ExprKind.Binary:
      switch (expr.op) {
        case BinaryOpKind.Plus:
        case BinaryOpKind.Minus:
        case BinaryOpKind.Times:
        case BinaryOpKind.Div:
        case BinaryOpKind.Mod: {
          let lhs = evaluate(expr.lhs, varResolver)
          let rhs = evaluate(expr.rhs, varResolver)
          const lhsType = getValueType(lhs)
          const rhsType = getValueType(rhs)
          if (lhsType !== ExprValueType.Number) {
            typeMismatch(expr.lhs.range, [ExprValueType.Number], lhsType)
          }
          if (rhsType !== ExprValueType.Number) {
            typeMismatch(expr.rhs.range, [ExprValueType.Number], rhsType)
          }
          let result: number
          lhs = lhs as number
          rhs = rhs as number
          switch (expr.op) {
            case BinaryOpKind.Plus:
              result = lhs + rhs
              break
            case BinaryOpKind.Minus:
              result = lhs - rhs
              break
            case BinaryOpKind.Times:
              result = lhs * rhs
              break
            case BinaryOpKind.Div:
              result = lhs / rhs
              break
            case BinaryOpKind.Mod:
              result = lhs % rhs
              break
          }
          return checkNumber(expr.range, result)
        }
        case BinaryOpKind.Concat: {
          let lhs = evaluate(expr.lhs, varResolver)
          let rhs = evaluate(expr.rhs, varResolver)
          const lhsType = getValueType(lhs)
          const rhsType = getValueType(rhs)
          if (lhsType !== ExprValueType.String) {
            typeMismatch(expr.lhs.range, [ExprValueType.String], lhsType)
          }
          if (rhsType !== ExprValueType.String) {
            typeMismatch(expr.rhs.range, [ExprValueType.String], rhsType)
          }
          return (lhs as string) + (rhs as string)
        }
        case BinaryOpKind.Eq:
        case BinaryOpKind.Ne:
        case BinaryOpKind.Gt:
        case BinaryOpKind.Lt:
        case BinaryOpKind.Ge:
        case BinaryOpKind.Le: {
          let lhs = evaluate(expr.lhs, varResolver)
          let rhs = evaluate(expr.rhs, varResolver)
          const lhsType = getValueType(lhs)
          const rhsType = getValueType(rhs)
          if (lhsType !== rhsType) {
            typeMismatch(expr.rhs.range, [lhsType], rhsType)
          }
          let result: boolean
          switch (expr.op) {
            case BinaryOpKind.Eq:
              result = lhs === rhs
              break
            case BinaryOpKind.Ne:
              result = lhs !== rhs
              break
            case BinaryOpKind.Gt:
              result = lhs > rhs
              break
            case BinaryOpKind.Lt:
              result = lhs < rhs
              break
            case BinaryOpKind.Ge:
              result = lhs >= rhs
              break
            case BinaryOpKind.Le:
              result = lhs <= rhs
              break
          }
          return result
        }
        case BinaryOpKind.And:
        case BinaryOpKind.Or: {
          let lhs = evaluate(expr.lhs, varResolver)
          let rhs = evaluate(expr.rhs, varResolver)
          const lhsType = getValueType(lhs)
          const rhsType = getValueType(rhs)
          if (lhsType !== ExprValueType.Boolean) {
            typeMismatch(expr.lhs.range, [ExprValueType.Boolean], lhsType)
          }
          if (rhsType !== ExprValueType.Boolean) {
            typeMismatch(expr.rhs.range, [ExprValueType.Boolean], rhsType)
          }
          if (expr.op === BinaryOpKind.And) {
            return lhs && rhs
          } else {
            return lhs || rhs
          }
        }
        default:
          throw new Error("unreachable")
      }
    case ExprKind.Unary:
      switch (expr.op) {
        case UnaryOpKind.Plus: {
          const arg = evaluate(expr.arg, varResolver)
          const argType = getValueType(arg)
          if (
            argType !== ExprValueType.Boolean &&
            argType !== ExprValueType.Number &&
            argType !== ExprValueType.String
          ) {
            typeMismatch(
              expr.arg.range,
              [ExprValueType.Boolean, ExprValueType.Number, ExprValueType.String],
              argType,
            )
          }
          return +arg
        }
        case UnaryOpKind.Minus: {
          const arg = evaluate(expr.arg, varResolver)
          const argType = getValueType(arg)
          if (argType !== ExprValueType.Number) {
            typeMismatch(expr.arg.range, [ExprValueType.Number], argType)
          }
          return -arg
        }
        case UnaryOpKind.Not: {
          const arg = evaluate(expr.arg, varResolver)
          const argType = getValueType(arg)
          if (argType !== ExprValueType.Boolean) {
            typeMismatch(expr.arg.range, [ExprValueType.Boolean], argType)
          }
          return -arg
        }
        default:
          throw new Error("unreachable")
      }
    case ExprKind.Var: {
      const value = varResolver.varValue(expr.name)
      if (value === null) {
        throw {
          kind: EvalErrorKind.VarNotFound,
          name: expr.name,
          range: expr.range,
        } as EvalError
      }
      return value
    }
    case ExprKind.Call:
      // TODO eval func call
      for (const arg of expr.args) {
        evaluate(arg, varResolver)
      }
      throw {
        kind: EvalErrorKind.FnNotFound,
        name: expr.name.value,
        range: expr.name.range,
      } as EvalError
    case ExprKind.Num:
      return +expr.text
    case ExprKind.Str:
      return expr.text
  }
}
