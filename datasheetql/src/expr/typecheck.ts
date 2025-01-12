import { BinaryOpKind, Expr, ExprKind, ExprValue, UnaryOpKind } from "../ast"
import { Range } from "../diagnostic"
import { ExprCheckType, ExprType, ExprValueType, FuncParamTypeSpec, typeMatches } from "./type"
import { NameResolver } from "."

export type CheckErrorCollector = {
  addTypeMismatch(range: Range, expected: NonEmptyArray<ExprValueType>, actual: ExprType): void
  addVarNotFound(range: Range, name: string): void
  addFuncNotFound(range: Range, name: string): void
  addFuncArgsTypeMismatch(range: Range, expected: FuncParamTypeSpec, actual: Array<ExprType>): void
}

export function getValueType(value: ExprValue): ExprValueType {
  switch (typeof value) {
    case "boolean":
      return ExprValueType.Boolean
    case "number":
      return ExprValueType.Number
    case "string":
      return ExprValueType.String
    case "object":
      if (value) {
        if (Array.isArray(value)) {
          if (value.every((x) => typeof x === "string")) {
            return ExprValueType.String
          }
        } else if (value instanceof Date) {
          return ExprValueType.DateTime
        }
      }
      throw new Error("unreachable " + value)
    default:
      throw new Error("unreachable " + value)
  }
}

export function checkExprType(
  expr: Expr,
  nameResolver: NameResolver,
  errCollector: CheckErrorCollector,
): ExprType {
  function go(expr: Expr): ExprType {
    switch (expr.kind) {
      case ExprKind.Binary:
        switch (expr.op) {
          case BinaryOpKind.Plus: {
            const lhsType = go(expr.lhs)
            const rhsType = go(expr.rhs)
            if (lhsType === ExprValueType.String || rhsType === ExprValueType.String) {
              return ExprValueType.String
            }
            if (!typeMatches(lhsType, ExprValueType.Number)) {
              errCollector.addTypeMismatch(
                expr.lhs.range,
                [ExprValueType.Number, ExprValueType.String],
                lhsType,
              )
              return ExprCheckType.Error
            }
            if (!typeMatches(rhsType, ExprValueType.Number)) {
              errCollector.addTypeMismatch(
                expr.rhs.range,
                [ExprValueType.Number, ExprValueType.String],
                rhsType,
              )
              return ExprCheckType.Error
            }
            return ExprValueType.Number
          }
          case BinaryOpKind.Minus:
          case BinaryOpKind.Times:
          case BinaryOpKind.Div:
          case BinaryOpKind.Mod: {
            const lhsType = go(expr.lhs)
            const rhsType = go(expr.rhs)
            if (!typeMatches(lhsType, ExprValueType.Number)) {
              errCollector.addTypeMismatch(expr.lhs.range, [ExprValueType.Number], lhsType)
            }
            if (!typeMatches(rhsType, ExprValueType.Number)) {
              errCollector.addTypeMismatch(expr.rhs.range, [ExprValueType.Number], rhsType)
            }
            return ExprValueType.Number
          }
          case BinaryOpKind.Concat: {
            const lhsType = go(expr.lhs)
            const rhsType = go(expr.rhs)
            if (!typeMatches(lhsType, ExprValueType.String)) {
              errCollector.addTypeMismatch(expr.lhs.range, [ExprValueType.String], lhsType)
            }
            if (!typeMatches(rhsType, ExprValueType.String)) {
              errCollector.addTypeMismatch(expr.rhs.range, [ExprValueType.String], rhsType)
            }
            return ExprValueType.String
          }
          case BinaryOpKind.Eq:
          case BinaryOpKind.Ne: {
            go(expr.lhs)
            go(expr.rhs)
            return ExprValueType.Boolean
          }
          case BinaryOpKind.Gt:
          case BinaryOpKind.Lt:
          case BinaryOpKind.Ge:
          case BinaryOpKind.Le: {
            const lhsType = go(expr.lhs)
            const rhsType = go(expr.rhs)
            if (lhsType === ExprValueType.Number) {
              if (!typeMatches(rhsType, ExprValueType.Number)) {
                errCollector.addTypeMismatch(expr.rhs.range, [ExprValueType.Number], rhsType)
              }
            } else if (lhsType === ExprValueType.String) {
              if (!typeMatches(rhsType, ExprValueType.String)) {
                errCollector.addTypeMismatch(expr.rhs.range, [ExprValueType.String], rhsType)
              }
            } else if (lhsType === ExprCheckType.Error) {
              if (
                rhsType !== ExprCheckType.Error &&
                rhsType !== ExprValueType.Number &&
                rhsType !== ExprValueType.String
              ) {
                errCollector.addTypeMismatch(
                  expr.rhs.range,
                  [ExprValueType.Number, ExprValueType.String],
                  rhsType,
                )
              }
            } else {
              errCollector.addTypeMismatch(
                expr.lhs.range,
                [ExprValueType.Number, ExprValueType.String],
                lhsType,
              )
            }
            return ExprValueType.Boolean
          }
          case BinaryOpKind.And:
          case BinaryOpKind.Or: {
            go(expr.lhs)
            go(expr.rhs)
            return ExprValueType.Boolean
          }
          default:
            throw new Error("unreachable")
        }
      case ExprKind.Unary:
        switch (expr.op) {
          case UnaryOpKind.Plus:
            go(expr.arg)
            return ExprValueType.Number
          case UnaryOpKind.Minus: {
            const argType = go(expr.arg)
            if (!typeMatches(argType, ExprValueType.Number)) {
              errCollector.addTypeMismatch(expr.arg.range, [ExprValueType.Number], argType)
            }
            return ExprValueType.Number
          }
          case UnaryOpKind.Not: {
            const argType = go(expr.arg)
            if (!typeMatches(argType, ExprValueType.Boolean)) {
              errCollector.addTypeMismatch(expr.arg.range, [ExprValueType.Boolean], argType)
            }
            return ExprValueType.Boolean
          }
          default:
            throw new Error("unreachable")
        }
      case ExprKind.Var: {
        const type = nameResolver.varType(expr.name)
        if (type === null) {
          errCollector.addVarNotFound(expr.range, expr.name)
          return ExprCheckType.Error
        }
        return type
      }
      case ExprKind.Call: {
        const argTypes = expr.args.map(go)
        const fnTypes = nameResolver.funcType(expr.name.value)
        if (fnTypes === null) {
          errCollector.addFuncNotFound(expr.name.range, expr.name.value)
          return ExprCheckType.Error
        }

        for (let i = 0; i < fnTypes.length; i++) {
          const fnType = fnTypes[i]!
          if (
            fnType.params.positional.length === argTypes.length ||
            (fnType.params.rest !== undefined && fnType.params.positional.length <= argTypes.length)
          ) {
            if (
              fnType.params.positional.every((paramType, i) =>
                typeMatches(argTypes[i]!, paramType),
              ) &&
              (fnType.params.rest === undefined ||
                argTypes
                  .slice(fnType.params.positional.length)
                  .every((ty) => typeMatches(ty, fnType.params.rest!)))
            ) {
              return fnType.ret
            }
          }
        }
        for (const fnType of fnTypes) {
          errCollector.addFuncArgsTypeMismatch(expr.range, fnType.params, argTypes)
        }
        return ExprCheckType.Error
      }
      case ExprKind.Num:
        return ExprValueType.Number
      case ExprKind.Str:
        return ExprValueType.String
      default:
        throw new Error("unreachable")
    }
  }
  return go(expr)
}
