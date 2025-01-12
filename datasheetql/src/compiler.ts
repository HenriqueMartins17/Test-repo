import {
  BailErrorStrategy,
  CharStreams,
  CommonTokenStream,
  InputMismatchException,
  NoViableAltException,
  ParserRuleContext,
  RecognitionException,
  Token,
} from "antlr4ts"
import { ParseCancellationException } from "antlr4ts/misc/ParseCancellationException"
import { AbstractParseTreeVisitor } from "antlr4ts/tree/AbstractParseTreeVisitor"
import { TerminalNode } from "antlr4ts/tree/TerminalNode"
import {
  ChoiceContext,
  CreateTableBodyContext,
  CreateTableContext,
  DataTypeContext,
  ExprContext,
  FactorContext,
  ProgramContext,
  StmtContext,
  DatasheetQL,
  FnNameContext,
  IdContext,
  AutonumContext,
  BigTextContext,
  CheckboxContext,
  CreatedDatetimeContext,
  CreatorContext,
  CurrencyContext,
  DatetimeContext,
  EmailContext,
  FieldSpecContext,
  FileContext,
  FormulaContext,
  LinkContext,
  LookupContext,
  MemberContext,
  ModifiedDatetimeContext,
  ModifierContext,
  NumberContext,
  PercentageContext,
  PhoneContext,
  RatingContext,
  SmallTextContext,
  UrlContext,
} from "./DatasheetQL"
import { DatasheetQLLexer } from "./DatasheetQLLexer"
import { DatasheetQLVisitor } from "./DatasheetQLVisitor"
import {
  CreateTableQuery,
  Expr,
  FieldSpec,
  Query,
  QueryKind,
  TableSpec,
  Value,
  FieldKind,
  MemberField,
  UrlField,
  ChoiceField,
  getFieldKindName,
  ExprKind,
  NumberField,
  CheckBoxField,
  RatingField,
  BinaryOpKind,
  UnaryOpKind,
  LinkField,
  ModifierField,
  ExprValue,
  CurrencyField,
  UnitPosition,
  PercentageField,
  DateFormat,
  DateTimeField,
  ModifiedDateTimeField,
  CreatedDateTimeField,
  FormulaField,
  FormulaNumberKind,
  FormulaResultFormat,
  LookupField,
  STATISTIC_FUNCTIONS,
  TimeFormat,
} from "./ast"
import { Diagnostic, DiagnosticCollector, newRange, Range, trimColumns } from "./diagnostic"
import { LocalizationKey as L10nKey, LocalizationProvider } from "./l10n"
import {
  evaluate,
  ExprValueType,
  getValueType,
  EvalError,
  EvalErrorKind,
  checkExprType,
  NameResolver,
  DUMMY_NAME_RESOLVER,
  ExprCheckType,
  CheckErrorCollector,
  ExprType,
  FuncParamTypeSpec,
} from "./expr"
import { numberEqual } from "./util"
import { CurrencyResolver, FormulaFunctionResolver, parseDatetimeFormat } from "./format"
import { EMOJI_NAMES } from "./emoji"

type TmpFieldSpec = Optional<FieldSpec, "name" | "range">

type Result = Query[] | Query | TableSpec | FieldSpec | TmpFieldSpec | Expr | Value<any> | void

type DateTimeFormat = {
  date: Value<DateFormat>
  time?: Value<TimeFormat>
}

class MyDatasheetQLVisitor
  extends AbstractParseTreeVisitor<Result>
  implements DatasheetQLVisitor<Result>
{
  constructor(
    private readonly source: string,
    private readonly l10nProvider: LocalizationProvider,
    private readonly diagCollector: DiagnosticCollector,
  ) {
    super()
  }

  private tokenRange(token: Token): Range {
    const range: Range = {
      source: this.source,
      startLine: token.line - 1,
      startColumn: token.charPositionInLine,
      endLine: token.line - 1,
      endColumn: 0,
    }
    if (token.text && token.text.includes("\n")) {
      const m = token.text.match(/\n/g)!
      range.endLine += m.length - 1
      range.endColumn = m.at(-1)!.length + 1
    } else {
      range.endColumn = token.charPositionInLine + 1 + token.text!.length
    }
    return range
  }

  private nodeRange(node: ParserRuleContext): Range
  private nodeRange(node: TerminalNode): Range

  private nodeRange(node: ParserRuleContext | TerminalNode): Range {
    if (node instanceof TerminalNode) {
      return this.tokenRange(node.symbol)
    } else {
      const start = node.start
      const end = node.stop ?? node.start
      const range: Range = {
        source: this.source,
        startLine: start.line - 1,
        startColumn: start.charPositionInLine,
        endLine: end.line - 1,
        endColumn: 0,
      }
      if (end.text && end.text.includes("\n")) {
        const m = end.text.match(/\n/g)!
        range.endLine += m.length - 1
        range.endColumn = m.at(-1)!.length + 1
      } else if (end.text) {
        range.endColumn = end.charPositionInLine + 1 + end.text.length
      } else {
        range.endColumn = end.charPositionInLine + 1
      }
      return range
    }
  }

  protected defaultResult(): Result {
    throw new Error("unreachable")
  }

  private reportTypeMismatch(
    range: Range,
    expected: NonEmptyArray<ExprValueType>,
    actual: ExprType,
  ) {
    if (expected.length === 1) {
      this.diagCollector.addError(
        this.l10nProvider.format(L10nKey.TypeMismatch1, {
          expected: this.formatType(expected[0]!),
          actual: this.formatType(actual),
        }),
        range,
      )
    } else if (expected.length <= 3) {
      this.diagCollector.addError(
        this.l10nProvider.format(
          expected.length === 2 ? L10nKey.TypeMismatch2 : L10nKey.TypeMismatch3,
          {
            expected: expected.map((e) => this.formatType(e)),
            actual: this.formatType(actual),
          },
        ),
        range,
      )
    } else {
      throw new Error("unreachable")
    }
  }

  visitProgram(ctx: ProgramContext): Query[] {
    const queries = ctx.stmt().map((stmt) => this.visit(stmt) as Query) as NonEmptyArray<Query>
    this.checkUniqueTableName(queries)
    return queries
  }

  visitStmt(ctx: StmtContext): Query {
    const query = this.visit(ctx.createTable()) as CreateTableQuery
    return query
  }

  visitCreateTable(ctx: CreateTableContext): Query {
    const query: CreateTableQuery = {
      kind: QueryKind.CreateTable,
      tables: ctx
        .createTableBody()
        .map((body) => this.visit(body) as TableSpec) as NonEmptyArray<TableSpec>,
      range: this.nodeRange(ctx),
    }
    return query
  }

  visitCreateTableBody(ctx: CreateTableBodyContext): TableSpec {
    const fields = ctx
      .fieldSpec()
      .map((field) => this.visit(field) as FieldSpec) as NonEmptyArray<FieldSpec>
    const spec: TableSpec = {
      fields,
      name: this.visit(ctx.id()) as Value<string>,
      range: this.nodeRange(ctx),
    }
    if (ctx.primaryKeyModifier()) {
      const key = this.visit(ctx.primaryKeyModifier()!.id()) as Value<string>
      const field = fields.find((field) => field.name.value === key.value)
      if (field) {
        if (field.primary) {
          this.addWarning(
            this.nodeRange(ctx.primaryKeyModifier()!),
            L10nKey.RedundantFieldModifier,
            {
              modifier: "PRIMARY",
            },
          )
        } else {
          field.primary = {
            value: true,
            range: this.nodeRange(ctx.primaryKeyModifier()!),
          }
        }
      } else {
        this.addError(key.range, L10nKey.FieldNotFound, { field: key.value })
      }
    }

    this.checkUniqueFieldName(fields)
    this.checkNumPrimaryKeys(spec.range, fields)
    this.checkFormulaExpression(fields)
    this.checkLinks(spec.name.value, fields)
    this.checkLookups(fields)
    this.checkModifiedFields(fields)

    return spec
  }

  private checkUniqueTableName(queries: NonEmptyArray<Query>) {
    const tableNames = new Set<string>()
    for (const query of queries) {
      if (query.kind === QueryKind.CreateTable) {
        for (const table of query.tables) {
          if (tableNames.has(table.name.value)) {
            this.addError(table.name.range, L10nKey.DuplicateTable, {})
          }
          tableNames.add(table.name.value)
        }
      }
    }
  }

  private checkUniqueFieldName(fields: NonEmptyArray<FieldSpec>) {
    const fieldNames = new Map<string, Range | null>()
    for (const field of fields) {
      const name = field.name.value
      const lastRange = fieldNames.get(field.name.value)
      if (lastRange !== undefined) {
        if (lastRange) {
          this.addError(lastRange, L10nKey.DuplicateField, { field: name })
        }
        this.addError(field.name.range, L10nKey.DuplicateField, { field: name })
        fieldNames.set(name, null)
      } else {
        fieldNames.set(name, field.name.range)
      }
    }
  }

  private checkNumPrimaryKeys(tableRange: Range, fields: NonEmptyArray<FieldSpec>) {
    let numPrimary = 0
    for (const field of fields) {
      if (field.primary) {
        ++numPrimary
        switch (field.type.kind) {
          case FieldKind.Text:
          case FieldKind.MultilineText:
          case FieldKind.Number:
          case FieldKind.Currency:
          case FieldKind.Percentage:
          case FieldKind.DateTime:
          case FieldKind.Url:
          case FieldKind.Phone:
          case FieldKind.Email:
          case FieldKind.Formula:
          case FieldKind.AutoNum:
            break
          default:
            this.addError(field.range, L10nKey.FieldTypeDoesNotAllowPrimaryKey, {
              type: getFieldKindName(field.type.kind),
            })
        }
      }
    }

    if (numPrimary > 1) {
      for (const field of fields) {
        if (field.primary) {
          this.addError(field.range, L10nKey.MultiplePrimaryKey, {})
        }
      }
    } else if (numPrimary === 0) {
      this.addError(tableRange, L10nKey.NoPrimaryKey, {})
    }
  }

  private checkFormulaExpression(fields: NonEmptyArray<FieldSpec>) {
    const fieldTypes: Record<string, ExprValueType> = {}
    for (const field of fields) {
      switch (field.type.kind) {
        case FieldKind.Text:
        case FieldKind.MultilineText:
          fieldTypes[field.name.value] = ExprValueType.String
          break
        case FieldKind.Choice:
          if (field.type.multi) {
            fieldTypes[field.name.value] = ExprValueType.Array
          } else {
            fieldTypes[field.name.value] = ExprValueType.String
          }
          break
        case FieldKind.Number:
        case FieldKind.Currency:
        case FieldKind.Percentage:
          fieldTypes[field.name.value] = ExprValueType.Number
          break
        case FieldKind.DateTime:
          fieldTypes[field.name.value] = ExprValueType.DateTime
          break
        case FieldKind.File:
        case FieldKind.Member:
          fieldTypes[field.name.value] = ExprValueType.String
          break
        case FieldKind.CheckBox:
          fieldTypes[field.name.value] = ExprValueType.Boolean
          break
        case FieldKind.Rating:
          fieldTypes[field.name.value] = ExprValueType.Number
          break
        case FieldKind.Url:
          fieldTypes[field.name.value] = ExprValueType.String
          break
        case FieldKind.Phone:
        case FieldKind.Email:
          fieldTypes[field.name.value] = ExprValueType.String
          break
        case FieldKind.Link:
        case FieldKind.Lookup:
        case FieldKind.Formula:
        case FieldKind.AutoNum:
        case FieldKind.CreatedDateTime:
        case FieldKind.ModifiedDateTime:
        case FieldKind.Creator:
        case FieldKind.Modifier:
      }
    }

    const errCollector: CheckErrorCollector = {
      addTypeMismatch: (range: Range, expected: NonEmptyArray<ExprValueType>, actual: ExprType) => {
        this.reportTypeMismatch(range, expected, actual)
      },
      addVarNotFound: (range: Range, name: string) => {
        this.addError(range, L10nKey.VarNotFound, { name })
      },
      addFuncNotFound: (range: Range, name: string) => {
        this.addError(range, L10nKey.FnNotFound, { name })
      },
      addFuncArgsTypeMismatch: (
        range: Range,
        expected: FuncParamTypeSpec,
        actual: Array<ExprType>,
      ) => {
        let expectedParams = expected.positional.map((type) => this.formatType(type)).join(", ")
        if (expected.rest !== undefined) {
          expectedParams += ", "
          expectedParams += this.formatType(expected.rest)
          expectedParams += "..."
        }
        this.addError(range, L10nKey.FuncParamsTypeMismatch, {
          expected: expectedParams,
          actual: actual.map((type) => this.formatType(type)).join(", "),
        })
      },
    }

    for (const field of fields) {
      if (field.type.kind === FieldKind.Formula) {
        const { resultFormat } = field.type
        const exprType = checkExprType(
          field.type.expr,
          new FormulaFunctionResolver(fieldTypes),
          errCollector,
        )
        switch (exprType) {
          case ExprCheckType.Error:
            // type error, don't check result format
            break
          case ExprValueType.Boolean:
          case ExprValueType.String:
          case ExprValueType.Array:
            field.type.resultType = exprType
            for (const modifier in resultFormat) {
              const range = (resultFormat as Record<string, Value<any>>)[modifier]!.range
              this.addError(range, L10nKey.FormulaResultTypeError, {
                type: this.formatType(exprType),
              })
            }
            break
          case ExprValueType.Number:
            field.type.resultType = exprType
            if (resultFormat.numberKind) {
              switch (resultFormat.numberKind.value) {
                case FormulaNumberKind.Number:
                  // do nothing
                  break
                case FormulaNumberKind.Currency:
                  if (resultFormat.separator) {
                    this.addWarning(
                      resultFormat.separator.range,
                      L10nKey.RedundantFormulaResultModifier,
                      { modifier: "SEPARATOR", type: "CURRENCY" },
                    )
                  }
                  break
                case FormulaNumberKind.Percentage:
                  if (resultFormat.separator) {
                    this.addWarning(
                      resultFormat.separator.range,
                      L10nKey.RedundantFormulaResultModifier,
                      { modifier: "SEPARATOR", type: "PERCENTAGE" },
                    )
                  }
                  if (resultFormat.unit) {
                    this.addWarning(
                      resultFormat.unit.range,
                      L10nKey.RedundantFormulaResultModifier,
                      {
                        modifier: "UNIT",
                        type: "PERCENTAGE",
                      },
                    )
                  }
                  break
              }
            }
            if (resultFormat.dateFormat) {
              this.addError(resultFormat.dateFormat.range, L10nKey.FormulaResultTypeError, {
                type: this.formatType(exprType),
              })
            }
            if (resultFormat.timeFormat) {
              this.addError(resultFormat.timeFormat.range, L10nKey.FormulaResultTypeError, {
                type: this.formatType(exprType),
              })
            }
            break
          case ExprValueType.String:
            field.type.resultType = exprType
            for (const key of ["numberKind", "separator", "precision", "unit"] as const) {
              if (key in resultFormat) {
                this.addError(resultFormat[key]!.range, L10nKey.FormulaResultTypeError, {
                  type: this.formatType(exprType),
                })
              }
            }
            break
          default:
            throw new Error("unreachable")
        }
      }
    }
  }

  private checkLinks(tableName: string, fields: NonEmptyArray<FieldSpec>) {
    for (const field of fields) {
      if (field.type.kind === FieldKind.Link) {
        if (field.type.targetTable.value === tableName) {
          this.addError(field.type.targetTable.range, L10nKey.CannotLinkSelf, {})
        }
      }
    }
  }

  /**
   * Check field references modified time and modifier fields.
   */
  private checkModifiedFields(fields: NonEmptyArray<FieldSpec>) {
    const fieldNames: Set<string> = new Set(fields.map((field) => field.name.value))
    for (const field of fields) {
      const type = field.type
      if (type.kind === FieldKind.ModifiedDateTime || type.kind === FieldKind.Modifier) {
        if (type.fields) {
          for (const refField of type.fields) {
            if (!fieldNames.has(refField.value)) {
              this.addError(refField.range, L10nKey.FieldNotFound, { field: refField.value })
            }
          }
        }
      }
    }
  }

  private checkLookups(fields: NonEmptyArray<FieldSpec>) {
    const fieldMap: Map<string, FieldSpec> = new Map()
    for (const field of fields) {
      fieldMap.set(field.name.value, field)
    }

    for (const field of fields) {
      const type = field.type
      if (type.kind === FieldKind.Lookup) {
        const refField = fieldMap.get(type.fieldName.value)
        if (refField) {
          if (refField.type.kind !== FieldKind.Link) {
            this.addError(type.fieldName.range, L10nKey.LookingUpFieldNotLink, {})
          }
        } else {
          this.addError(type.fieldName.range, L10nKey.FieldNotFound, {
            field: type.fieldName.value,
          })
        }
      }
    }
  }

  private formatType(type: ExprType): string {
    switch (type) {
      case ExprValueType.Boolean:
        return this.l10nProvider.format(L10nKey.TypeBoolean, {})
      case ExprValueType.Number:
        return this.l10nProvider.format(L10nKey.TypeNumber, {})
      case ExprValueType.String:
        return this.l10nProvider.format(L10nKey.TypeString, {})
      case ExprValueType.Array:
        return this.l10nProvider.format(L10nKey.TypeArray, {})
      case ExprValueType.DateTime:
        return this.l10nProvider.format(L10nKey.TypeDateTime, {})
      case ExprCheckType.Error:
        return this.l10nProvider.format(L10nKey.TypeError, {})
      default:
        throw new Error("unreachable")
    }
  }

  private addError(range: Range, key: string, args: Record<string, any>) {
    this.diagCollector.addError(this.l10nProvider.format(key, args), range)
  }

  private addEvalError(e: EvalError) {
    switch (e.kind) {
      case EvalErrorKind.VarNotFound:
        this.diagCollector.addError(
          this.l10nProvider.format(L10nKey.VarNotFound, { name: e.name }),
          e.range,
        )
        break
      case EvalErrorKind.FnNotFound:
        this.diagCollector.addError(
          this.l10nProvider.format(L10nKey.FnNotFound, { name: e.name }),
          e.range,
        )
        break
      case EvalErrorKind.TypeMismatch:
        if (e.expected.length === 1) {
          this.diagCollector.addError(
            this.l10nProvider.format(L10nKey.TypeMismatch1, {
              expected: this.formatType(e.expected[0]!),
              actual: this.formatType(e.actual),
            }),
            e.range,
          )
        } else if (e.expected.length <= 3) {
          this.diagCollector.addError(
            this.l10nProvider.format(
              e.expected.length === 2 ? L10nKey.TypeMismatch2 : L10nKey.TypeMismatch3,
              {
                expected: e.expected.map((e) => this.formatType(e)),
                actual: this.formatType(e.actual),
              },
            ),
            e.range,
          )
        } else {
          throw new Error("unreachable")
        }
        break
      case EvalErrorKind.NaN:
        this.diagCollector.addError(this.l10nProvider.format(L10nKey.ResultNaN, {}), e.range)
        break
      case EvalErrorKind.Infinity:
        this.diagCollector.addError(this.l10nProvider.format(L10nKey.ResultInfinity, {}), e.range)
        break
    }
  }

  private addWarning(range: Range, key: string, args: Record<string, any>) {
    this.diagCollector.addWarning(this.l10nProvider.format(key, args), range)
  }

  private evaluate(
    expr: Expr,
    expectedType: ExprValueType,
    nameResolver: NameResolver = DUMMY_NAME_RESOLVER,
  ): ExprValue | undefined {
    try {
      const value = evaluate(expr, nameResolver)
      const type = getValueType(value)
      if (type == expectedType) {
        return value
      } else {
        this.addError(expr.range, L10nKey.TypeMismatch1, {
          expected: this.formatType(expectedType),
          actual: this.formatType(type),
        })
        return undefined
      }
    } catch (e) {
      if (e && typeof (e as any)["kind"] === "number") {
        this.addEvalError(e as EvalError)
        return undefined
      } else {
        throw e
      }
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.fieldSpec`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitFieldSpec(ctx: FieldSpecContext): FieldSpec {
    const name = this.visit(ctx.id()) as Value<string>
    const field: FieldSpec = {
      name,
      range: this.nodeRange(ctx),
      ...(this.visit(ctx.dataType()) as TmpFieldSpec),
    }
    for (const modifier of ctx.fieldModifier()) {
      if (modifier.COMMENT()) {
        const expr = this.visit(modifier.expr()!) as Expr
        const comment = this.evaluate(expr, ExprValueType.String)
        if (comment !== undefined) {
          this.setModifierValue<"comment", string>(
            "COMMENT",
            field,
            "comment",
            comment as string,
            this.nodeRange(modifier),
          )
        }
      } else if (modifier.DEFAULT()) {
        let expected: ExprValueType
        switch (field.type.kind) {
          case FieldKind.Text:
            expected = ExprValueType.String
            break
          case FieldKind.Choice:
            expected = ExprValueType.String
            break
          case FieldKind.Number:
            expected = ExprValueType.Number
            break
          case FieldKind.Currency:
            expected = ExprValueType.Number
            break
          case FieldKind.Percentage:
            expected = ExprValueType.Number
            break

          case FieldKind.DateTime: {
            if (modifier.AUTO()) {
              this.setModifierValue<"autoFill", true>(
                "DEFAULT",
                field.type as DateTimeField,
                "autoFill",
                true,
                this.nodeRange(modifier),
              )
            } else {
              this.addError(this.nodeRange(modifier), L10nKey.FieldDoesNotAllowDefault, {
                type: getFieldKindName(field.type.kind),
              })
            }
            continue
          }

          case FieldKind.MultilineText:
          case FieldKind.File:
          case FieldKind.Member:
          case FieldKind.Link:
          case FieldKind.Lookup:
          case FieldKind.Formula:
          case FieldKind.AutoNum:
          case FieldKind.CreatedDateTime:
          case FieldKind.ModifiedDateTime:
          case FieldKind.Creator:
          case FieldKind.Modifier:
          case FieldKind.CheckBox:
          case FieldKind.Rating:
          case FieldKind.Url:
          case FieldKind.Phone:
          case FieldKind.Email:
            this.addError(this.nodeRange(modifier), L10nKey.FieldDoesNotAllowDefault, {
              type: getFieldKindName(field.type.kind),
            })
            continue
        }
        const expr = this.visit(modifier.expr()!) as Expr
        let default_: ExprValue | undefined
        if (field.type.kind === FieldKind.Choice && expr.kind === ExprKind.Var) {
          default_ = expr.name
        } else {
          default_ = this.evaluate(expr, expected)
        }
        if (default_ !== undefined) {
          if (field.type.kind === FieldKind.Choice) {
            if (!field.type.items.value.find((item) => item.value === default_)) {
              this.addError(expr.range, L10nKey.ItemNotFound, { item: default_ })
            }
          }
          this.setModifierValue(
            "DEFAULT",
            field.type,
            "default",
            default_,
            this.nodeRange(modifier),
          )
        }
      } else {
        this.setModifierValue<"primary", true>(
          "PRIMARY KEY",
          field,
          "primary",
          true,
          this.nodeRange(modifier),
        )
      }
    }
    return field
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.dataType`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitDataType(ctx: DataTypeContext): TmpFieldSpec {
    const type =
      ctx.smallText() ||
      ctx.bigText() ||
      ctx.choice() ||
      ctx.number() ||
      ctx.currency() ||
      ctx.percentage() ||
      ctx.datetime() ||
      ctx.file() ||
      ctx.member() ||
      ctx.checkbox() ||
      ctx.rating() ||
      ctx.url() ||
      ctx.phone() ||
      ctx.email() ||
      ctx.link() ||
      ctx.lookup() ||
      ctx.formula() ||
      ctx.autonum() ||
      ctx.createdDatetime() ||
      ctx.modifiedDatetime() ||
      ctx.creator() ||
      ctx.modifier()!
    return this.visit(type) as TmpFieldSpec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.text`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitSmallText(ctx: SmallTextContext): TmpFieldSpec {
    return {
      type: {
        kind: FieldKind.Text,
        range: this.nodeRange(ctx),
      },
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.bigText`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitBigText(ctx: BigTextContext): TmpFieldSpec {
    return {
      type: {
        kind: FieldKind.MultilineText,
        range: this.nodeRange(ctx),
      },
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.choice`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitChoice(ctx: ChoiceContext): TmpFieldSpec {
    const type: Optional<ChoiceField, "items"> = {
      kind: FieldKind.Choice,
      range: this.nodeRange(ctx),
    }
    if (ctx.MULTI()) {
      type.multi = {
        value: true,
        range: this.nodeRange(ctx.MULTI()!),
      }
    }
    for (const modifier of ctx.choiceModifier()) {
      if (modifier.MULTI()) {
        this.setModifierValue<"multi", true>(
          "MULTI",
          type,
          "multi",
          true,
          this.nodeRange(modifier.MULTI()!),
        )
      } else if (modifier.LPAR()) {
        const items: Value<string>[] = []
        for (const expr of modifier.choiceList()!.expr()) {
          const itemExpr = this.visit(expr) as Expr
          if (itemExpr.kind === ExprKind.Var) {
            items.push({
              value: itemExpr.name,
              range: itemExpr.range,
            })
          } else {
            const item = this.evaluate(itemExpr, ExprValueType.String)
            if (item !== undefined) {
              items.push({
                value: item as string,
                range: itemExpr.range,
              })
            }
          }
        }
        this.setModifierValue<"items", Value<string>[]>(
          this.l10nProvider.format(L10nKey.ChoiceItemsModifier, {}),
          type,
          "items",
          items,
          this.nodeRange(modifier),
        )
      }
    }
    if (type.items === undefined) {
      this.addError(type.range, L10nKey.MissingChoiceItems, {})
      type.items = {
        value: [],
        range: this.nodeRange(ctx),
      }
    }
    return {
      type: type as ChoiceField,
    }
  }

  private getPrecision(expr: Expr): number | undefined {
    if (expr.kind === ExprKind.Num) {
      const precision = +expr.text
      if (numberEqual(precision, 1)) {
        return 0
      } else if (numberEqual(precision, 0.1)) {
        return 1
      } else if (numberEqual(precision, 0.01)) {
        return 2
      } else if (numberEqual(precision, 0.001)) {
        return 3
      } else if (numberEqual(precision, 0.0001)) {
        return 4
      } else {
        this.addError(expr.range, L10nKey.InvalidPrecision, {})
        return undefined
      }
    } else {
      this.addError(expr.range, L10nKey.InvalidPrecision, {})
      return undefined
    }
  }

  visitNumber(ctx: NumberContext): TmpFieldSpec {
    const type: NumberField = {
      kind: FieldKind.Number,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.expr()) {
      const precExpr = this.visit(ctx.expr()!) as Expr
      const precision = this.getPrecision(precExpr)
      if (precision !== undefined) {
        type.precision = {
          value: precision,
          range: this.nodeRange(ctx.expr()!),
        }
      }
    }
    for (const modifier of ctx.numberModifier()) {
      if (modifier.SEPARATOR()) {
        this.setModifierValue<"separator", true>(
          "SEPARATOR",
          type,
          "separator",
          true,
          this.nodeRange(modifier),
        )
      } else if (modifier.PRECISION()) {
        const precExpr = this.visit(modifier.expr()!) as Expr
        const precision = this.getPrecision(precExpr)
        if (precision !== undefined) {
          this.setModifierValue<"precision", number>(
            "PRECISION",
            type,
            "precision",
            precision,
            this.nodeRange(modifier),
          )
        }
      } else if (modifier.UNIT()) {
        const unitExpr = this.visit(modifier.expr()!) as Expr
        const unit = this.evaluate(unitExpr, ExprValueType.String, CurrencyResolver)
        if (unit !== undefined) {
          this.setModifierValue<"unit", string>(
            "UNIT",
            type,
            "unit",
            unit as string,
            this.nodeRange(modifier),
          )
        }
      }
    }
    return spec
  }

  visitCurrency(ctx: CurrencyContext): TmpFieldSpec {
    const type: CurrencyField = {
      kind: FieldKind.Currency,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.expr()) {
      const precExpr = this.visit(ctx.expr()!) as Expr
      const precision = this.getPrecision(precExpr)
      if (precision !== undefined) {
        type.precision = {
          value: precision,
          range: this.nodeRange(ctx.expr()!),
        }
      }
    }
    for (const modifier of ctx.currencyModifier()) {
      if (modifier.LEFT()) {
        this.setModifierValue<"unitPos", UnitPosition>(
          this.l10nProvider.format(L10nKey.CurrencyUnitPosition, {}),
          type,
          "unitPos",
          UnitPosition.Left,
          this.nodeRange(modifier),
        )
      } else if (modifier.RIGHT()) {
        this.setModifierValue<"unitPos", UnitPosition>(
          this.l10nProvider.format(L10nKey.CurrencyUnitPosition, {}),
          type,
          "unitPos",
          UnitPosition.Right,
          this.nodeRange(modifier),
        )
      } else if (modifier.UNIT()) {
        const unitExpr = this.visit(modifier.expr()!) as Expr
        const unit = this.evaluate(unitExpr, ExprValueType.String, CurrencyResolver)
        if (unit !== undefined) {
          this.setModifierValue<"unit", string>(
            "UNIT",
            type,
            "unit",
            unit as string,
            this.nodeRange(modifier),
          )
        }
      } else if (modifier.PRECISION()) {
        const precExpr = this.visit(modifier.expr()!) as Expr
        const precision = this.getPrecision(precExpr)
        if (precision !== undefined) {
          this.setModifierValue<"precision", number>(
            "PRECISION",
            type,
            "precision",
            precision,
            this.nodeRange(modifier),
          )
        }
      }
    }
    return spec
  }

  visitPercentage(ctx: PercentageContext): TmpFieldSpec {
    const type: PercentageField = {
      kind: FieldKind.Percentage,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.expr()) {
      const precExpr = this.visit(ctx.expr()!) as Expr
      const precision = this.getPrecision(precExpr)
      if (precision !== undefined) {
        type.precision = {
          value: precision,
          range: this.nodeRange(ctx.expr()!),
        }
      }
    }
    for (const modifier of ctx.percentageModifier()) {
      if (modifier.PRECISION()) {
        const precExpr = this.visit(modifier.expr()!) as Expr
        const precision = this.getPrecision(precExpr)
        if (precision !== undefined) {
          this.setModifierValue<"precision", number>(
            "PRECISION",
            type,
            "precision",
            precision,
            this.nodeRange(modifier),
          )
        }
      }
    }
    return spec
  }

  private parseDatetimeFormat(expr: Expr): DateTimeFormat | undefined {
    let srcRange: Range | undefined
    if (expr.kind === ExprKind.Str) {
      // trim double-quotes
      srcRange = trimColumns(expr.range, 1, 1)
    }

    const format = this.evaluate(expr, ExprValueType.String)
    if (format === undefined) {
      return undefined
    }

    let normFormat = (format as string).normalize("NFKC")
    if (normFormat !== format) {
      srcRange = undefined
    }

    const parseResult = parseDatetimeFormat(normFormat)
    if ("position" in parseResult) {
      this.addError(expr.range, L10nKey.InvalidDateTimeFormat, { format })
      return undefined
    }

    return {
      date: {
        value: parseResult.date.format,
        range: srcRange ? srcRange : expr.range,
      },
      time:
        parseResult.time === undefined
          ? undefined
          : {
              value: parseResult.time.format,
              range: srcRange ? srcRange : expr.range,
            },
    }
  }

  visitDatetime(ctx: DatetimeContext): TmpFieldSpec {
    const type: DateTimeField = {
      kind: FieldKind.DateTime,
      range: this.nodeRange(ctx),
    }
    if (ctx.expr()) {
      const fmtExpr = this.visit(ctx.expr()!) as Expr
      const fmt = this.parseDatetimeFormat(fmtExpr)
      if (fmt) {
        type.dateFormat = fmt.date
        type.timeFormat = fmt.time
      }
    }
    const spec: TmpFieldSpec = {
      type,
    }
    return spec
  }

  visitFile(ctx: FileContext): TmpFieldSpec {
    const spec: TmpFieldSpec = {
      type: {
        kind: FieldKind.File,
        range: this.nodeRange(ctx),
      },
    }
    return spec
  }

  setModifierValue<K extends string, T>(
    modifierName: string,
    object: { [key in K]?: Value<T> },
    field: K,
    value: T,
    range: Range,
  ) {
    if (object[field]) {
      this.addWarning(range, L10nKey.RedundantFieldModifier, {
        modifier: modifierName,
      })
    } else {
      object[field] = {
        value,
        range,
      }
    }
  }

  visitMember(ctx: MemberContext): TmpFieldSpec {
    const type: MemberField = {
      kind: FieldKind.Member,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.MULTI()) {
      type.multi = {
        value: true,
        range: this.nodeRange(ctx.MULTI()!),
      }
    }
    for (const modifier of ctx.memberModifier()) {
      if (modifier.MULTI()) {
        this.setModifierValue("MULTI", type, "multi", true, this.nodeRange(modifier.MULTI()!))
      } else if (modifier.NOTIFY()) {
        this.setModifierValue("NOTIFY", type, "notify", true, this.nodeRange(modifier.NOTIFY()!))
      }
    }
    return spec
  }

  private setSymbol<S extends string>(
    expr: ExprContext,
    object: { [key in S]?: Value<string> },
    key: S,
  ) {
    const symbolExpr = this.visit(expr) as Expr
    const symbol = this.evaluate(symbolExpr, ExprValueType.String)
    if (symbol !== undefined) {
      if (symbol === "") {
        this.addError(symbolExpr.range, L10nKey.EmptySymbol, {})
      } else {
        if (Object.hasOwn(EMOJI_NAMES, symbol as string)) {
          object[key] = {
            value: EMOJI_NAMES[symbol as string]!,
            range: symbolExpr.range,
          }
        } else {
          this.addError(symbolExpr.range, L10nKey.UnknownSymbol, { symbol })
        }
      }
    }
  }

  visitCheckbox(ctx: CheckboxContext): TmpFieldSpec {
    const type: CheckBoxField = {
      kind: FieldKind.CheckBox,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.expr()) {
      this.setSymbol(ctx.expr()!, type, "symbol")
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.rating`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitRating(ctx: RatingContext): TmpFieldSpec {
    const type: RatingField = {
      kind: FieldKind.Rating,
      range: this.nodeRange(ctx),
    }
    const spec: TmpFieldSpec = {
      type,
    }
    if (ctx.expr()) {
      this.setSymbol(ctx.expr()!, type, "symbol")
    }
    for (const modifier of ctx.ratingModifier()) {
      const maxExpr = this.visit(modifier.expr()) as Expr
      let max = this.evaluate(maxExpr, ExprValueType.Number)
      if (max !== undefined) {
        if (!Number.isInteger(max)) {
          this.addWarning(maxExpr.range, L10nKey.NotInteger, { result: max })
          max = Math.round(max as number)
        }
        if (max <= 0 || max > 10) {
          this.addError(maxExpr.range, L10nKey.MaxRatingOutOfRange, { result: max })
        } else {
          this.setModifierValue<"max", number>(
            "MAX",
            type,
            "max",
            max as number,
            this.nodeRange(modifier),
          )
        }
      }
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.url`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitUrl(ctx: UrlContext): TmpFieldSpec {
    const type: UrlField = {
      kind: FieldKind.Url,
      range: this.nodeRange(ctx),
    }
    for (const modifier of ctx.urlModifier()) {
      if (modifier.TITLE()) {
        this.setModifierValue("TITLE", type, "showTitle", true, this.nodeRange(modifier.TITLE()!))
      }
    }
    return {
      type,
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.phone`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitPhone(ctx: PhoneContext): TmpFieldSpec {
    const spec: TmpFieldSpec = {
      type: {
        kind: FieldKind.Phone,
        range: this.nodeRange(ctx),
      },
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.email`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitEmail(ctx: EmailContext): TmpFieldSpec {
    const spec: TmpFieldSpec = {
      type: {
        kind: FieldKind.Email,
        range: this.nodeRange(ctx),
      },
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.link`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitLink(ctx: LinkContext): TmpFieldSpec {
    const type: Optional<LinkField, "targetTable"> = {
      kind: FieldKind.Link,
      range: this.nodeRange(ctx),
    }
    if (ctx.MULTI()) {
      type.multi = {
        value: true,
        range: this.nodeRange(ctx.MULTI()!),
      }
    }
    for (const modifier of ctx.linkModifier()) {
      if (modifier.MULTI()) {
        this.setModifierValue<"multi", true>(
          "MULTI",
          type,
          "multi",
          true,
          this.nodeRange(modifier.MULTI()!),
        )
      } else if (modifier.VIA()) {
        this.setModifierValue<"view", string>(
          "VIA VIEW",
          type,
          "view",
          (this.visit(modifier.id()!) as Value<string>).value,
          this.nodeRange(modifier),
        )
      } else if (modifier.TO() || modifier.id()) {
        this.setModifierValue<"targetTable", string>(
          this.l10nProvider.format(L10nKey.LinkTableModifier, {}),
          type,
          "targetTable",
          (this.visit(modifier.id()!) as Value<string>).value,
          this.nodeRange(modifier),
        )
      } else {
        throw new Error("unreachable")
      }
    }
    if (!type.targetTable) {
      this.addError(type.range, L10nKey.MissingLinkTargetTable, {})
    }
    const spec: TmpFieldSpec = {
      type: type as LinkField,
    }
    return spec
  }

  visitLookup(ctx: LookupContext): TmpFieldSpec {
    const type: Optional<LookupField, "statFunc" | "statFieldName"> = {
      kind: FieldKind.Lookup,
      range: this.nodeRange(ctx),
      fieldName: this.visit(ctx.id()) as Value<string>,
    }
    for (const modifier of ctx.lookupModifier()) {
      if (modifier.WHEN()) {
        throw "TODO lookup filter condition"
      } else if (modifier.OF()) {
        const func = modifier.statFunc()!.MAX()
          ? "MAX"
          : (this.visit(modifier.statFunc()!.id()!) as Value<string>).value
        this.setModifierValue<"statFunc", string>(
          this.l10nProvider.format(L10nKey.LookupStatFieldModifier, {}),
          type,
          "statFunc",
          func,
          this.nodeRange(modifier.statFunc()!),
        )
        if (!STATISTIC_FUNCTIONS.has(type.statFunc!.value.toUpperCase())) {
          this.addError(type.statFunc!.range, L10nKey.UnknownLookupStat, {})
        }
        this.setModifierValue<"statFieldName", string>(
          this.l10nProvider.format(L10nKey.LookupStatFieldModifier, {}),
          type,
          "statFieldName",
          (this.visit(modifier.id()!) as Value<string>).value,
          this.nodeRange(modifier.id()!),
        )
      } else {
        throw new Error("unreachable")
      }
    }
    if (!type.statFunc || !type.statFieldName) {
      this.addError(type.range, L10nKey.MissingLookupStat, {})
    }
    const spec: TmpFieldSpec = {
      type: type as LookupField,
    }
    return spec
  }

  visitFormula(ctx: FormulaContext): TmpFieldSpec {
    const resultFormat: FormulaResultFormat = {}
    const type: Omit<FormulaField, "resultType"> = {
      kind: FieldKind.Formula,
      range: this.nodeRange(ctx),
      expr: this.visit(ctx.expr()) as Expr,
      resultFormat,
    }
    for (const modifier of ctx.formulaModifier()) {
      if (modifier.SEPARATOR()) {
        this.setModifierValue<"separator", true>(
          "SEPARATOR",
          resultFormat,
          "separator",
          true,
          this.nodeRange(modifier),
        )
      } else if (modifier.NUMBER()) {
        this.setModifierValue<"numberKind", FormulaNumberKind>(
          "NUMBER",
          resultFormat,
          "numberKind",
          FormulaNumberKind.Number,
          this.nodeRange(modifier),
        )
      } else if (modifier.CURRENCY()) {
        this.setModifierValue<"numberKind", FormulaNumberKind>(
          "NUMBER",
          resultFormat,
          "numberKind",
          FormulaNumberKind.Currency,
          this.nodeRange(modifier),
        )
      } else if (modifier.PERCENTAGE()) {
        this.setModifierValue<"numberKind", FormulaNumberKind>(
          "NUMBER",
          resultFormat,
          "numberKind",
          FormulaNumberKind.Percentage,
          this.nodeRange(modifier),
        )
      } else if (modifier.UNIT()) {
        const unitExpr = this.visit(modifier.expr()!) as Expr
        const unit = this.evaluate(unitExpr, ExprValueType.String, CurrencyResolver)
        if (unit !== undefined) {
          this.setModifierValue<"unit", string>(
            "UNIT",
            resultFormat,
            "unit",
            unit as string,
            this.nodeRange(modifier),
          )
        }
      } else if (modifier.PRECISION()) {
        const precExpr = this.visit(modifier.expr()!) as Expr
        const precision = this.getPrecision(precExpr)
        if (precision !== undefined) {
          this.setModifierValue<"precision", number>(
            "PRECISION",
            resultFormat,
            "precision",
            precision,
            this.nodeRange(modifier),
          )
        }
      } else if (modifier.DATETIME()) {
        if (ctx.expr()) {
          const fmtExpr = this.visit(ctx.expr()!) as Expr
          const fmt = this.parseDatetimeFormat(fmtExpr)
          if (fmt) {
            resultFormat.dateFormat = fmt.date
            resultFormat.timeFormat = fmt.time
          }
        }
      } else {
        throw new Error("unreachable")
      }
    }

    const spec: TmpFieldSpec = {
      type: type as FormulaField,
    }
    return spec
  }

  visitAutonum(ctx: AutonumContext): TmpFieldSpec {
    const spec: TmpFieldSpec = {
      type: {
        kind: FieldKind.AutoNum,
        range: this.nodeRange(ctx),
      },
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.createdDatetime`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitCreatedDatetime(ctx: CreatedDatetimeContext): TmpFieldSpec {
    const type: CreatedDateTimeField = {
      kind: FieldKind.CreatedDateTime,
      range: this.nodeRange(ctx),
    }
    if (ctx.expr()) {
      const fmtExpr = this.visit(ctx.expr()!) as Expr
      const fmt = this.parseDatetimeFormat(fmtExpr)
      if (fmt) {
        type.dateFormat = fmt.date
        type.timeFormat = fmt.time
      }
    }
    const spec: TmpFieldSpec = {
      type,
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.modifiedDatetime`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitModifiedDatetime(ctx: ModifiedDatetimeContext): TmpFieldSpec {
    const type: ModifiedDateTimeField = {
      kind: FieldKind.ModifiedDateTime,
      range: this.nodeRange(ctx),
    }
    if (ctx.expr()) {
      const fmtExpr = this.visit(ctx.expr()!) as Expr
      const fmt = this.parseDatetimeFormat(fmtExpr)
      if (fmt) {
        type.dateFormat = fmt.date
        type.timeFormat = fmt.time
      }
    }
    if (ctx.modifiedFieldList()) {
      type.fields = ctx
        .modifiedFieldList()!
        .id()
        .map((id) => this.visit(id) as Value<string>) as NonEmptyArray<Value<string>>
    }
    const spec: TmpFieldSpec = {
      type,
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.creator`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitCreator(ctx: CreatorContext): TmpFieldSpec {
    const spec: TmpFieldSpec = {
      type: {
        kind: FieldKind.Creator,
        range: this.nodeRange(ctx),
      },
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.modifier`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitModifier(ctx: ModifierContext): TmpFieldSpec {
    const type: ModifierField = {
      kind: FieldKind.Modifier,
      range: this.nodeRange(ctx),
    }
    if (ctx.modifiedFieldList()) {
      type.fields = ctx
        .modifiedFieldList()!
        .id()
        .map((id) => this.visit(id) as Value<string>) as NonEmptyArray<Value<string>>
    }
    const spec: TmpFieldSpec = {
      type,
    }
    return spec
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.id`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitId(ctx: IdContext): Value<string> {
    if (ctx.ID()) {
      return {
        value: ctx.ID()!.text,
        range: this.nodeRange(ctx.ID()!),
      }
    } else {
      const text = ctx.QUOTED_ID()!.text
      return {
        value: text.slice(1, -1).replace(/\\([\\}])/g, (_m, g1) => g1),
        range: this.nodeRange(ctx.QUOTED_ID()!),
      }
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.expr`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitExpr(ctx: ExprContext): Expr {
    let op: BinaryOpKind
    if (ctx.factor()) {
      return this.visit(ctx.factor()!) as Expr
    } else if (ctx.TIMES()) {
      op = BinaryOpKind.Times
    } else if (ctx.DIV()) {
      op = BinaryOpKind.Div
    } else if (ctx.MOD()) {
      op = BinaryOpKind.Mod
    } else if (ctx.PLUS()) {
      op = BinaryOpKind.Plus
    } else if (ctx.MINUS()) {
      op = BinaryOpKind.Minus
    } else if (ctx.CONCAT()) {
      op = BinaryOpKind.Concat
    } else if (ctx.EQ()) {
      op = BinaryOpKind.Eq
    } else if (ctx.NE()) {
      op = BinaryOpKind.Ne
    } else if (ctx.GT()) {
      op = BinaryOpKind.Gt
    } else if (ctx.LT()) {
      op = BinaryOpKind.Lt
    } else if (ctx.GE()) {
      op = BinaryOpKind.Ge
    } else if (ctx.LE()) {
      op = BinaryOpKind.Le
    } else if (ctx.AND()) {
      op = BinaryOpKind.And
    } else if (ctx.OR()) {
      op = BinaryOpKind.Or
    } else {
      throw new Error("unreachable")
    }
    return {
      kind: ExprKind.Binary,
      op,
      lhs: this.visit(ctx.expr(0)!) as Expr,
      rhs: this.visit(ctx.expr(1)!) as Expr,
      range: this.nodeRange(ctx),
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.factor`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitFactor(ctx: FactorContext): Expr {
    if (ctx.PLUS()) {
      return {
        kind: ExprKind.Unary,
        op: UnaryOpKind.Plus,
        arg: this.visit(ctx.factor()!) as Expr,
        range: this.nodeRange(ctx),
      }
    } else if (ctx.MINUS()) {
      return {
        kind: ExprKind.Unary,
        op: UnaryOpKind.Minus,
        arg: this.visit(ctx.factor()!) as Expr,
        range: this.nodeRange(ctx),
      }
    } else if (ctx.NOT()) {
      return {
        kind: ExprKind.Unary,
        op: UnaryOpKind.Not,
        arg: this.visit(ctx.factor()!) as Expr,
        range: this.nodeRange(ctx),
      }
    } else if (ctx.fnName()) {
      const name = this.visit(ctx.fnName()!) as Value<string>
      const args = ctx.expr().map((expr) => this.visit(expr) as Expr)
      return {
        kind: ExprKind.Call,
        name,
        args,
        range: this.nodeRange(ctx),
      }
    } else if (ctx.id()) {
      const { value: name, range } = this.visit(ctx.id()!) as Value<string>
      return {
        kind: ExprKind.Var,
        name,
        range,
      }
    } else if (ctx.NUM_LITERAL()) {
      return {
        kind: ExprKind.Num,
        text: ctx.NUM_LITERAL()!.text,
        range: this.nodeRange(ctx),
      }
    } else if (ctx.STR_LITERAL()) {
      return {
        kind: ExprKind.Str,
        text: ctx
          .STR_LITERAL()!
          .text.slice(1, -1)
          .replace(/\\[n"\\]/g, (_, g1) => (g1 === "n" ? "\n" : g1)),
        range: this.nodeRange(ctx),
      }
    } else if (ctx.LPAR()) {
      return this.visit(ctx.expr(0)) as Expr
    } else {
      throw new Error("unreachable")
    }
  }

  /**
   * Visit a parse tree produced by `DatasheetQL.fnName`.
   * @param ctx the parse tree
   * @return the visitor result
   */
  visitFnName(ctx: FnNameContext): Value<string> {
    let node
    if (ctx.ID()) {
      node = ctx.ID()!
    } else {
      node = ctx.MAX()!
    }
    return {
      value: node.text.toUpperCase(),
      range: this.nodeRange(node),
    }
  }
}

export type CompileOptions = {
  /**
   * Source file path
   */
  source: string
  /**
   * Localization provider
   */
  l10nProvider: LocalizationProvider
}

export type CompileResult = {
  /**
   * AST of queries
   */
  queries: Query[]
  /**
   * Diagnostic messages
   */
  diagnostics: Diagnostic[]
}

export async function compileFile(
  input: string,
  options: CompileOptions & { diagCollector: DiagnosticCollector },
): Promise<Omit<CompileResult, "diagnostics">>
export async function compileFile(input: string, options: CompileOptions): Promise<CompileResult>

/**
 * Compile DatasheetQL source code into AST.
 *
 * @param input source code
 */
export async function compileFile(
  input: string,
  options: CompileOptions & { diagCollector?: DiagnosticCollector },
): Promise<Optional<CompileResult, "diagnostics">> {
  const { source, l10nProvider } = options
  let returnsDiags = false
  let diagCollector = options.diagCollector
  if (diagCollector === undefined) {
    diagCollector = new DiagnosticCollector()
    returnsDiags = true
  }

  const inputStream = CharStreams.fromString(input)
  const lexer = new DatasheetQLLexer(inputStream)
  const tokenStream = new CommonTokenStream(lexer)
  const parser = new DatasheetQL(tokenStream)
  parser.errorHandler = new BailErrorStrategy()
  parser.removeErrorListeners()

  const visitor = new MyDatasheetQLVisitor(source, l10nProvider, diagCollector)

  let tree: ProgramContext

  try {
    tree = parser.program()
  } catch (e) {
    if (e instanceof ParseCancellationException) {
      if (e.cause instanceof InputMismatchException || e.cause instanceof RecognitionException) {
        const token = e.cause.getOffendingToken()
        const message = l10nProvider.format(L10nKey.ParseError, { error: "syntax error" })
        if (token) {
          diagCollector.addError(
            message,
            newRange(source, token.line, token.charPositionInLine + 1),
          )
        } else {
          const lines = input.match(/\n/g)
          if (lines) {
            diagCollector.addError(message, newRange(source, lines.length, lines.at(-1)!.length))
          } else {
            diagCollector.addError(message, newRange(source, 1, input.length))
          }
        }
        return {
          queries: [],
          diagnostics: diagCollector.diagnostics,
        }
      } else {
        throw new Error("unreachable " + e)
      }
    } else if (e instanceof NoViableAltException) {
      const token = e.startToken
      const message = l10nProvider.format(L10nKey.ParseError, { error: "syntax error" })
      diagCollector.addError(message, newRange(source, token.line, token.charPositionInLine + 1))
      return {
        queries: [],
        diagnostics: diagCollector.diagnostics,
      }
    } else {
      throw e
    }
  }

  const queries = visitor.visit(tree)

  if (returnsDiags) {
    return {
      queries: queries as Query[],
      diagnostics: diagCollector.diagnostics,
    }
  } else {
    return {
      queries: queries as Query[],
    }
  }
}
