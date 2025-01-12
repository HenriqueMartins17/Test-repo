import { APIMetaFieldPropertyFormatEnums, TSymbolAlign } from "@vikadata/vika"
import { IDatasheetFieldCreateRo } from "@vikadata/vika/es/interface/datasheet.field.create.ro"
import {
  CollectType,
  IAddOpenMagicLinkFieldProperty,
  IOpenComputedFormat,
} from "@vikadata/vika/es/interface/field.create.property"
import { ExecutionPlan, ExecutionQueryKind, QueryResponseReference, newQueryResponseRef } from "."
import { LocalizationProvider } from "../l10n"
import {
  DateFormat,
  FieldKind,
  FormulaNumberKind,
  Query,
  QueryKind,
  TimeFormat,
  UnitPosition,
  Value,
} from "../ast"
import { DiagnosticCollector, Range } from "../diagnostic"
import { ExprValueType, formatExpr } from "../expr"
import { EMOJI_NAMES } from "../emoji"

export type GenerationOptions = {
  diagCollector: DiagnosticCollector
  l10nProdiver: LocalizationProvider
}

export class ExecutionPlanGenerator {
  private readonly diagCollector: DiagnosticCollector
  private readonly l10nProvider: LocalizationProvider

  constructor(options: GenerationOptions) {
    const { diagCollector, l10nProdiver } = options
    this.diagCollector = diagCollector
    this.l10nProvider = l10nProdiver
  }

  private addError(range: Range, key: string, args: Record<string, any>) {
    this.diagCollector.addError(this.l10nProvider.format(key, args), range)
  }

  generateExecutionPlan(queries: Query[]): ExecutionPlan {
    const plan: ExecutionPlan = []

    for (const query of queries) {
      switch (query.kind) {
        case QueryKind.CreateTable: {
          const creatingTables: Set<string> = new Set(query.tables.map((t) => t.name.value))
          for (const tableSpec of query.tables) {
            const fields: IDatasheetFieldCreateRo[] = []
            for (const fieldSpec of tableSpec.fields) {
              let field: IDatasheetFieldCreateRo
              switch (fieldSpec.type.kind) {
                case FieldKind.Text:
                  field = {
                    name: fieldSpec.name.value,
                    type: "SingleText",
                    property: {
                      defaultValue: fieldSpec.type.default?.value,
                    },
                  }
                  break
                case FieldKind.MultilineText:
                  field = {
                    name: fieldSpec.name.value,
                    type: "Text",
                  }
                  break
                case FieldKind.Choice:
                  field = {
                    name: fieldSpec.name.value,
                    type: fieldSpec.type.multi ? "MultiSelect" : "SingleSelect",
                    property: {
                      defaultValue: fieldSpec.type.multi
                        ? fieldSpec.type.default
                          ? [fieldSpec.type.default.value]
                          : undefined
                        : fieldSpec.type.default?.value,
                      options: fieldSpec.type.items.value.map((item) => ({ name: item.value })),
                    },
                  }
                  break
                case FieldKind.Number: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "Number",
                    property: {
                      precision: getPrecision(type.precision),
                      defaultValue: type.default ? String(type.default.value) : undefined,
                      symbol: type.unit?.value,
                    },
                  }
                  break
                }
                case FieldKind.Currency: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "Currency",
                    property: {
                      precision: getPrecision(type.precision),
                      defaultValue: type.default ? String(type.default.value) : undefined,
                      symbol: type.unit?.value,
                      symbolAlign: type.unitPos
                        ? type.unitPos.value === UnitPosition.Left
                          ? TSymbolAlign.Left
                          : TSymbolAlign.Right
                        : undefined,
                    },
                  }
                  break
                }
                case FieldKind.Percentage: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "Percent",
                    property: {
                      precision: getPrecision(type.precision),
                      defaultValue: type.default ? String(type.default.value) : undefined,
                    },
                  }
                  break
                }
                case FieldKind.DateTime: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "DateTime",
                    property: {
                      dateFormat: getDateFormat(type.dateFormat),
                      timeFormat: getTimeFormat(type.timeFormat),
                      autoFill: !!type.autoFill,
                      includeTime: !!type.timeFormat,
                    },
                  }
                  break
                }
                case FieldKind.File: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Attachment",
                  }
                  break
                }
                case FieldKind.Member: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Member",
                    property: {
                      isMulti: !!fieldSpec.type.multi,
                      shouldSendMsg: !!fieldSpec.type.notify,
                    },
                  }
                  break
                }
                case FieldKind.CheckBox: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Checkbox",
                    property: {
                      icon: fieldSpec.type.symbol
                        ? fieldSpec.type.symbol.value
                        : EMOJI_NAMES["✅"]!,
                    },
                  }
                  break
                }
                case FieldKind.Rating: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Rating",
                    property: {
                      icon: fieldSpec.type.symbol
                        ? fieldSpec.type.symbol.value
                        : EMOJI_NAMES["⭐"]!,
                      max: fieldSpec.type.max ? fieldSpec.type.max.value : 5,
                    },
                  }
                  break
                }
                case FieldKind.Url: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "URL",
                  }
                  break
                }
                case FieldKind.Phone: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Phone",
                  }
                  break
                }
                case FieldKind.Email: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "Email",
                  }
                  break
                }
                case FieldKind.Link: {
                  // TODO via view
                  const type = fieldSpec.type
                  const properties: Omit<
                    IAddOpenMagicLinkFieldProperty,
                    "foreignDatasheetId" | "limitToViewId"
                  > & {
                    foreignDatasheetId: QueryResponseReference
                    limitToViewId?: QueryResponseReference
                  } = {
                    foreignDatasheetId: newQueryResponseRef(0),
                    limitSingleRecord: !type.multi,
                  }

                  let linkingTableIdIndex: number | undefined

                  if (creatingTables.has(type.targetTable.value)) {
                    // TODO use l10n later
                    this.diagCollector.addError(
                      "link to sibling table is not supported currently",
                      tableSpec.range,
                    )
                  } else {
                    linkingTableIdIndex = plan.length
                    plan.push({
                      kind: ExecutionQueryKind.GetTableId,
                      tableName: type.targetTable.value,
                    })
                    properties.foreignDatasheetId = newQueryResponseRef(linkingTableIdIndex)
                  }

                  if (type.view) {
                    if (linkingTableIdIndex === undefined) {
                      linkingTableIdIndex = plan.length
                      plan.push({
                        kind: ExecutionQueryKind.GetTableId,
                        tableName: type.targetTable.value,
                      })
                    }
                    const ix = plan.length
                    plan.push({
                      kind: ExecutionQueryKind.GetViewId,
                      tableId: newQueryResponseRef(linkingTableIdIndex),
                      tableName: type.targetTable.value,
                      viewName: type.view.value,
                    })
                    properties.limitToViewId = newQueryResponseRef(ix)
                  }

                  field = {
                    name: fieldSpec.name.value,
                    type: "MagicLink",
                    property: properties as any,
                  }
                  break
                }
                case FieldKind.Lookup: {
                  // TODO lookup field
                  this.diagCollector.addError(
                    "lookup field is not supported currently",
                    tableSpec.range,
                  )
                  continue
                }
                case FieldKind.Formula: {
                  const type = fieldSpec.type
                  const resultFormat = type.resultFormat
                  let format: IOpenComputedFormat | undefined
                  if (type.resultType === ExprValueType.Number) {
                    switch (resultFormat.numberKind?.value ?? FormulaNumberKind.Number) {
                      case FormulaNumberKind.Number:
                        format = {
                          type: APIMetaFieldPropertyFormatEnums.Number,
                          format: {
                            precision: getPrecision(resultFormat.precision),
                          },
                        }
                        break
                      case FormulaNumberKind.Currency:
                        format = {
                          type: APIMetaFieldPropertyFormatEnums.Currency,
                          format: {
                            precision: getPrecision(resultFormat.precision),
                            symbol: resultFormat.unit?.value,
                          },
                        }
                        break
                      case FormulaNumberKind.Percentage:
                        format = {
                          type: APIMetaFieldPropertyFormatEnums.Percent,
                          format: {
                            precision: getPrecision(resultFormat.precision),
                          },
                        }
                        break
                    }
                  } else if (type.resultType === ExprValueType.DateTime) {
                    format = {
                      type: APIMetaFieldPropertyFormatEnums.DateTime,
                      format: {
                        dateFormat: getDateFormat(resultFormat.dateFormat),
                        timeFormat: getTimeFormat(resultFormat.timeFormat),
                        includeTime: !!resultFormat.timeFormat,
                      },
                    }
                  }
                  field = {
                    name: fieldSpec.name.value,
                    type: "Formula",
                    property: {
                      expression: formatExpr(type.expr),
                      format,
                    },
                  }
                  break
                }
                case FieldKind.AutoNum: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "AutoNumber",
                  }
                  break
                }
                case FieldKind.CreatedDateTime: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "CreatedTime",
                    property: {
                      dateFormat: getDateFormat(type.dateFormat),
                      timeFormat: getTimeFormat(type.timeFormat),
                      includeTime: !!type.timeFormat,
                    },
                  }
                  break
                }
                case FieldKind.ModifiedDateTime: {
                  const type = fieldSpec.type
                  field = {
                    name: fieldSpec.name.value,
                    type: "LastModifiedTime",
                    property: {
                      dateFormat: getDateFormat(type.dateFormat),
                      timeFormat: getTimeFormat(type.timeFormat),
                      includeTime: !!type.timeFormat,
                      collectType: CollectType.AllFields,
                    },
                  }
                  break
                }
                case FieldKind.Creator: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "CreatedBy",
                  }
                  break
                }
                case FieldKind.Modifier: {
                  field = {
                    name: fieldSpec.name.value,
                    type: "LastModifiedBy",
                    property: {
                      collectType: CollectType.AllFields,
                    },
                  }
                  break
                }
              }
              if (fieldSpec.primary) {
                fields.unshift(field)
              } else {
                fields.push(field)
              }
            }
            plan.push({
              kind: ExecutionQueryKind.CreateTable,
              name: tableSpec.name.value,
              description: tableSpec.comment?.value,
              fields: fields as NonEmptyArray<IDatasheetFieldCreateRo>,
            })
          }
        }
      }
    }
    return plan
  }
}

function getDateFormat(dateFormat: Value<DateFormat> | undefined): string {
  return (dateFormat !== undefined ? dateFormat.value : DateFormat["YYYY/MM/DD"]) as string
}

function getTimeFormat(timeFormat: Value<TimeFormat> | undefined): string {
  return (timeFormat !== undefined ? timeFormat.value : TimeFormat["hh:mm"]) as string
}

function getPrecision(precision: Value<number> | undefined): number {
  if (!precision) {
    return 0
  }
  return precision.value
}
