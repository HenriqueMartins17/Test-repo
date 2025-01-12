import fs from "fs"
import { compileFile } from "../compiler"
import { DiagnosticCollector, printDiagnostics } from "../diagnostic"
import { ExecutionPlan, ExecutionPlanGenerator } from "../execution"
import * as l10n from "../l10n"
import { streamToString } from "../util"

export type ExecOptions = {
  filePath: string
}

export async function runCommand(options: ExecOptions) {
  const { filePath } = options
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

  console.log(JSON.stringify(plan, null, 2))
}
