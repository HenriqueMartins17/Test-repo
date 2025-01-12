import { IDatasheetFieldCreateRo } from "@vikadata/vika/es/interface/datasheet.field.create.ro"
import { QueryResponseReference } from "."

export type ExecutionPlan = ExecutionQuery[]

export type ExecutionQuery = CreateTableQuery | GetTableIdQuery | GetFieldIdQuery | GetViewIdQuery

export type QueryResponse =
  | CreateTableResponse
  | GetTableIdResponse
  | GetFieldIdResponse
  | GetViewIdResponse

export const enum ExecutionQueryKind {
  CreateTable = "create-table",
  GetTableId = "get-table-id",
  GetFieldId = "get-field-id",
  GetViewId = "get-view-id",
}

export type CreateTableQuery = {
  kind: ExecutionQueryKind.CreateTable
  name: string
  description?: string
  fields: NonEmptyArray<IDatasheetFieldCreateRo>
}

export type CreateTableResponse = {
  tableId: string
  fields: { name: string; id: string }[]
}

export type GetTableIdQuery = {
  kind: ExecutionQueryKind.GetTableId
  tableName: string
}

export type GetTableIdResponse = string

export type GetFieldIdQuery = {
  kind: ExecutionQueryKind.GetFieldId
  tableId: QueryResponseReference
  fieldName: string
}

export type GetFieldIdResponse = string

export type GetViewIdQuery = {
  kind: ExecutionQueryKind.GetViewId
  tableId: QueryResponseReference
  tableName: string
  viewName: string
}

export type GetViewIdResponse = string
