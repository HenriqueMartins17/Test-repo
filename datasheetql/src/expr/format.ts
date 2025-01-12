import { BinaryOpKind, Expr, ExprKind, UnaryOpKind } from "../ast"

export function formatExpr(expr: Expr): string {
  switch (expr.kind) {
    case ExprKind.Binary:
      return `(${formatExpr(expr.lhs)} ${BINARY_OP_STR[expr.op]} ${formatExpr(expr.rhs)})`
    case ExprKind.Unary:
      switch (expr.op) {
        case UnaryOpKind.Plus:
          return "VALUE(" + formatExpr(expr.arg) + ")"
        case UnaryOpKind.Minus:
          return "(- " + formatExpr(expr.arg) + ")"
        case UnaryOpKind.Not:
          return "(! " + formatExpr(expr.arg) + ")"
      }
    case ExprKind.Var:
      return '{' + expr.name.replace(/[\\}]/g, (_, s) => '\\' + s) + '}'
    case ExprKind.Call:
      return expr.name.value.toUpperCase() + "(" + expr.args.map(formatExpr).join(", ") + ")"
    case ExprKind.Num:
      return expr.text
    case ExprKind.Str:
      return '"' + expr.text.replace(/[\\"\n]/g, (m) => (m === "\n" ? "\\n" : "\\" + m)) + '"'
  }
}

const BINARY_OP_STR: Record<BinaryOpKind, string> = {
  [BinaryOpKind.Plus]: "+",
  [BinaryOpKind.Minus]: "-",
  [BinaryOpKind.Times]: "*",
  [BinaryOpKind.Div]: "/",
  [BinaryOpKind.Mod]: "%",
  [BinaryOpKind.Concat]: "&",
  [BinaryOpKind.Eq]: "=",
  [BinaryOpKind.Ne]: "!=",
  [BinaryOpKind.Gt]: ">",
  [BinaryOpKind.Lt]: "<",
  [BinaryOpKind.Ge]: ">=",
  [BinaryOpKind.Le]: "<=",
  [BinaryOpKind.And]: "&&",
  [BinaryOpKind.Or]: "||",
}
