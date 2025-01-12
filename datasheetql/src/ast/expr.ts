import { Range } from "../diagnostic"
import { Value } from "./common"

export type ExprValue = number | string | boolean | Date | string[]

export type Expr =
  | BinaryExpr
  | UnaryExpr
  | {
      kind: ExprKind.Var
      name: string
      range: Range
    }
  | {
      kind: ExprKind.Call
      name: Value<string>
      args: Array<Expr>
      range: Range
    }
  | {
      kind: ExprKind.Num
      text: string
      range: Range
    }
  | {
      kind: ExprKind.Str
      text: string
      range: Range
    }

export const enum ExprKind {
  Binary,
  Unary,
  Var,
  Call,
  Num,
  Str,
}

export const enum BinaryOpKind {
  Plus,
  Minus,
  Times,
  Div,
  Mod,
  Concat,
  Eq,
  Ne,
  Gt,
  Lt,
  Ge,
  Le,
  And,
  Or,
}

export type BinaryExpr = {
  kind: ExprKind.Binary
  op: BinaryOpKind
  lhs: Expr
  rhs: Expr
  range: Range
}

export const enum UnaryOpKind {
  Plus,
  Minus,
  Not,
}

export type UnaryExpr = {
  kind: ExprKind.Unary
  op: UnaryOpKind
  arg: Expr
  range: Range
}
