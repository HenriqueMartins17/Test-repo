import fs from "fs"
import { compileFile } from "../compiler"
import { DiagnosticCollector, printDiagnostics } from "../diagnostic"
import * as l10n from "../l10n"
import { ISpaceItem, Vika } from "@vikadata/vika"
import {
  ExecutionPlan,
  ExecutionPlanGenerator,
  ExecutionQueryKind,
  QueryResponse,
  resolveQueryResponseRefs,
} from "../execution"
import { IDatasheetCreateVo } from "@vikadata/vika/es/interface/datasheet.create.vo"
import { Terminal, streamToString } from "../util"

export type ExecOptions = {
  filePath: string
  spaceName: string
  apiToken: string
  host: string
}

export async function runCommand(options: ExecOptions) {
  const { filePath, spaceName, apiToken, host } = options
  let input: string
  let source: string
  if (filePath === "-") {
    // TODO windows char encoding
    input = await streamToString(process.stdin)
    source = "<stdin>"
  } else {
    // TODO read error
    input = fs.readFileSync(filePath, "utf-8")
    source = filePath
  }

  const diagCollector = new DiagnosticCollector()
  const result = await compileFile(input, {
    source,
    l10nProvider: l10n.DEFAULT_PROVIDER,
    diagCollector,
  })

  let plan: ExecutionPlan | undefined

  if (!diagCollector.containsErrors()) {
    const planGen = new ExecutionPlanGenerator({
      l10nProdiver: l10n.DEFAULT_PROVIDER,
      diagCollector,
    })
    plan = planGen.generateExecutionPlan(result.queries)
  }

  printDiagnostics(diagCollector.diagnostics, source, input)
  if (diagCollector.containsErrors()) {
    process.exit(1)
  }

  const vika = new Vika({
    token: apiToken,
    host,
    fieldKey: "id",
  })

  const spaceId = await findSpaceId(vika, spaceName)
  const spaceMan = vika.space(spaceId)
  const nodeMan = vika.nodes

  const createdTableCache: Map<string, IDatasheetCreateVo> = new Map()
  /**
   * table name -> table id
   */
  const tableIdCache: Map<string, string> = new Map()
  /**
   * table id -> view name -> view id
   */
  const tableViewCache: Map<string, Map<string, string>> = new Map()
  const queryResponses: QueryResponse[] = []
  let ok = true

  iterPlan: for (const query of plan!) {
    switch (query.kind) {
      case ExecutionQueryKind.CreateTable: {
        Terminal.startSpinner(`Creating table ${query.name}`)
        const resp = await spaceMan.datasheets.create({
          name: query.name,
          description: query.description,
          fields: resolveQueryResponseRefs(query.fields, queryResponses),
        })
        Terminal.stopSpinner()
        // TODO check table name not changed
        if (resp.success) {
          createdTableCache.set(query.name, resp.data)
          queryResponses.push({
            tableId: resp.data.id,
            fields: resp.data.fields!.map((field) => ({ id: field.id, name: field.name })),
          })
          Terminal.info(`Created table ${query.name}`)
        } else {
          Terminal.error(`Creating table ${query.name} failed: ${resp.message}`)
          ok = false
          break iterPlan
        }
        break
      }
      case ExecutionQueryKind.GetTableId: {
        if (tableIdCache.has(query.tableName)) {
          queryResponses.push(tableIdCache.get(query.tableName)!)
        } else {
          const table = createdTableCache.get(query.tableName)
          if (table !== undefined) {
            tableIdCache.set(query.tableName, table.id)
            queryResponses.push(table.id)
          } else {
            Terminal.startSpinner("Fetching table list")
            const resp = await nodeMan.list({ spaceId })
            Terminal.stopSpinner()
            if (!resp.success) {
              Terminal.error(`Fetching table list failed: ${resp.message}`)
              ok = false
              break iterPlan
            }

            for (const node of resp.data.nodes) {
              if (node.type.toLowerCase() === "datasheet") {
                tableIdCache.set(node.name, node.id)
              }
            }

            if (tableIdCache.has(query.tableName)) {
              queryResponses.push(tableIdCache.get(query.tableName)!)
            } else {
              Terminal.error(`Table ${query.tableName} does not exist`)
              ok = false
              break iterPlan
            }
          }
        }
        break
      }
      case ExecutionQueryKind.GetFieldId: {
        throw "TODO"
      }
      case ExecutionQueryKind.GetViewId: {
        const tableId = resolveQueryResponseRefs(query.tableId, queryResponses)
        /**
         * view name -> view id
         */
        let views: Map<string, string>
        if (tableViewCache.has(tableId)) {
          views = tableViewCache.get(tableId)!
        } else {
          Terminal.startSpinner(`Fetching view list of table ${query.tableName}`)
          const resp = await spaceMan.datasheet(tableId).views.list()
          Terminal.stopSpinner()
          if (!resp.success) {
            Terminal.error(`Fetching view list failed: ${resp.message}`)
            ok = false
            break iterPlan
          }

          views = new Map()
          for (const view of resp.data.views) {
            views.set(view.name, view.id)
          }
        }

        if (views.has(query.viewName)) {
          queryResponses.push(views.get(query.viewName)!)
        } else {
          Terminal.error(`Table ${query.tableName} does not have a view named ${query.viewName}`)
          ok = false
          break iterPlan
        }
        break
      }
      default:
        throw new Error("unreachable")
    }
  }

  if (!ok && createdTableCache.size !== 0) {
    Terminal.error(
      `Please delete these table(s) manually: ${[...createdTableCache.keys()].join(", ")}`,
    )
  }
}

async function findSpaceId(vika: Vika, spaceName: string): Promise<string> {
  let spaces: ISpaceItem[]
  const spacesResp = await vika.spaces.list()
  if (spacesResp.success) {
    spaces = spacesResp.data!.spaces
  } else {
    console.log("Fetching space list failed: " + spacesResp.message)
    process.exit(1)
  }

  const space = spaces.find((space) => space.name === spaceName)
  if (!space) {
    console.log(`space ${spaceName} not found`)
    process.exit(1)
  }

  return space.id
}
