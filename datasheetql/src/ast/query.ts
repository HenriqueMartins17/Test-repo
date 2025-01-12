import { RollUpFuncType } from "@vikadata/vika"
import { Range } from "../diagnostic"
import { ExprValueType } from "../expr"
import { Value } from "./common"
import { Expr } from "./expr"

export const enum QueryKind {
  CreateTable,
}

export type Query = CreateTableQuery

export type CreateTableQuery = {
  kind: QueryKind.CreateTable
  tables: NonEmptyArray<TableSpec>
  range: Range
}

export type TableSpec = {
  name: Value<string>
  fields: NonEmptyArray<FieldSpec>
  comment?: Value<string>
  range: Range
}

export type FieldSpec = {
  name: Value<string>
  type: FieldType
  primary?: Value<true>
  comment?: Value<string>
  range: Range
}

export const enum FieldKind {
  Text,
  MultilineText,
  Choice,
  Number,
  Currency,
  Percentage,
  DateTime,
  File,
  Member,
  CheckBox,
  Rating,
  Url,
  Phone,
  Email,
  Link,
  Lookup,
  Formula,
  AutoNum,
  CreatedDateTime,
  ModifiedDateTime,
  Creator,
  Modifier,
}

export type FieldType =
  | TextField
  | MultilineTextField
  | ChoiceField
  | NumberField
  | CurrencyField
  | PercentageField
  | DateTimeField
  | FileField
  | MemberField
  | CheckBoxField
  | RatingField
  | UrlField
  | PhoneField
  | EmailField
  | LinkField
  | LookupField
  | FormulaField
  | AutoNumField
  | CreatedDateTimeField
  | ModifiedDateTimeField
  | CreatorField
  | ModifierField

export type TextField = {
  kind: FieldKind.Text
  range: Range
  default?: Value<string>
}

export type MultilineTextField = {
  kind: FieldKind.MultilineText
  range: Range
}

export type ChoiceField = {
  kind: FieldKind.Choice
  range: Range
  multi?: Value<true>
  items: Value<Array<Value<string>>>
  default?: Value<string>
}

export type NumberField = {
  kind: FieldKind.Number
  range: Range
  precision?: Value<number>
  separator?: Value<true>
  unit?: Value<string>
  default?: Value<number>
}

export type CurrencyField = {
  kind: FieldKind.Currency
  range: Range
  precision?: Value<number>
  unit?: Value<string>
  unitPos?: Value<UnitPosition>
  default?: Value<number>
}

export const enum UnitPosition {
  Left,
  Right,
}

export type PercentageField = {
  kind: FieldKind.Percentage
  range: Range
  precision?: Value<number>
  default?: Value<number>
}

export const enum DateFormat {
  "YYYY/MM/DD" = "YYYY/MM/DD",
  "YYYY-MM-DD" = "YYYY-MM-DD",
  "DD/MM/YYYY" = "DD/MM/YYYY",
  "YYYY-MM" = "YYYY-MM",
  "MM-DD" = "MM-DD",
  "YYYY" = "YYYY",
  "MM" = "MM",
  "DD" = "DD",
}

export const enum TimeFormat {
  "hh:mm" = "hh:mm",
  "HH:mm" = "HH:mm",
}

export type DateTimeField = {
  kind: FieldKind.DateTime
  range: Range
  autoFill?: Value<true>
  dateFormat?: Value<DateFormat>
  /**
   * If undefined, time is not shown
   */
  timeFormat?: Value<TimeFormat>
}

export type FileField = {
  kind: FieldKind.File
  range: Range
}

export type MemberField = {
  kind: FieldKind.Member
  range: Range
  multi?: Value<true>
  notify?: Value<true>
}

export type CheckBoxField = {
  kind: FieldKind.CheckBox
  range: Range
  symbol?: Value<string>
}

export type RatingField = {
  kind: FieldKind.Rating
  range: Range
  symbol?: Value<string>
  max?: Value<number>
}

export type UrlField = {
  kind: FieldKind.Url
  range: Range
  showTitle?: Value<true>
}

export type PhoneField = {
  kind: FieldKind.Phone
  range: Range
}

export type EmailField = {
  kind: FieldKind.Email
  range: Range
}

export type LinkField = {
  kind: FieldKind.Link
  range: Range
  multi?: Value<true>
  targetTable: Value<string>
  view?: Value<string>
}

export type LookupField = {
  kind: FieldKind.Lookup
  range: Range
  conditions?: Value<NonEmptyArray<Expr>>
  fieldName: Value<string>
  statFunc: Value<string>
  statFieldName: Value<string>
}

export const STATISTIC_FUNCTIONS: Map<string, RollUpFuncType> = new Map([
  ["VALUES", RollUpFuncType.VALUES],
  ["AVERAGE", RollUpFuncType.AVERAGE],
  ["COUNT", RollUpFuncType.COUNT],
  ["COUNTA", RollUpFuncType.COUNTA],
  ["COUNTALL", RollUpFuncType.COUNTALL],
  ["SUM", RollUpFuncType.SUM],
  ["MAX", RollUpFuncType.MAX],
  ["MIN", RollUpFuncType.MIN],
  ["AND", RollUpFuncType.AND],
  ["OR", RollUpFuncType.OR],
  ["XOR", RollUpFuncType.XOR],
  ["CONCATENATE", RollUpFuncType.CONCATENATE],
  ["ARRAYJOIN", RollUpFuncType.ARRAYJOIN],
  ["ARRAYUNIQUE", RollUpFuncType.ARRAYUNIQUE],
  ["ARRAYCOMPACT", RollUpFuncType.ARRAYCOMPACT],
])

export type FormulaField = {
  kind: FieldKind.Formula
  range: Range
  expr: Expr
  resultType: ExprValueType
  resultFormat: FormulaResultFormat
}

export type FormulaResultFormat = {
  numberKind?: Value<FormulaNumberKind>
  separator?: Value<true>
  precision?: Value<number>
  unit?: Value<string>
  dateFormat?: Value<DateFormat>
  timeFormat?: Value<TimeFormat>
}

export const enum FormulaNumberKind {
  Number,
  Currency,
  Percentage,
}

export type AutoNumField = {
  kind: FieldKind.AutoNum
  range: Range
}

export type CreatedDateTimeField = {
  kind: FieldKind.CreatedDateTime
  range: Range
  dateFormat?: Value<DateFormat>
  /**
   * If undefined, time is not shown
   */
  timeFormat?: Value<TimeFormat>
}

export type ModifiedDateTimeField = {
  kind: FieldKind.ModifiedDateTime
  range: Range
  fields?: NonEmptyArray<Value<string>>
  dateFormat?: Value<DateFormat>
  /**
   * If undefined, time is not shown
   */
  timeFormat?: Value<TimeFormat>
}

export type CreatorField = {
  kind: FieldKind.Creator
  range: Range
}

export type ModifierField = {
  kind: FieldKind.Modifier
  range: Range
  fields?: NonEmptyArray<Value<string>>
}

export function getFieldKindName(kind: FieldKind): string {
  switch (kind) {
    case FieldKind.Text:
      return "TEXT"
    case FieldKind.MultilineText:
      return "MULTILINE TEXT"
    case FieldKind.Choice:
      return "CHOICE"
    case FieldKind.Number:
      return "NUMBER"
    case FieldKind.Currency:
      return "CURRENCY"
    case FieldKind.Percentage:
      return "PERCENTAGE"
    case FieldKind.DateTime:
      return "DATETIME"
    case FieldKind.File:
      return "FILE"
    case FieldKind.Member:
      return "MEMBER"
    case FieldKind.CheckBox:
      return "CHECKBOX"
    case FieldKind.Rating:
      return "RATING"
    case FieldKind.Url:
      return "URL"
    case FieldKind.Phone:
      return "PHONE"
    case FieldKind.Email:
      return "EMAIL"
    case FieldKind.Link:
      return "LINK"
    case FieldKind.Lookup:
      return "LOOKUP"
    case FieldKind.Formula:
      return "FORMULA"
    case FieldKind.AutoNum:
      return "AUTO NUM"
    case FieldKind.CreatedDateTime:
      return "CREATED DATETIME"
    case FieldKind.ModifiedDateTime:
      return "MODIFIED DATETIME"
    case FieldKind.Creator:
      return "CREATOR"
    case FieldKind.Modifier:
      return "MODIFIER"
  }
}
