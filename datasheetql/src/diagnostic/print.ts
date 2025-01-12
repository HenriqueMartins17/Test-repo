import pathMod from "path"
import { Diagnostic, DiagnosticSeverity, Range } from "."
import chalk, { Chalk } from "chalk"
import { stringWidth } from "../util"

// TODO show source text context
export function printDiagnostics(
  diagnostics: Diagnostic[],
  sourcePath: string,
  sourceText: string,
) {
  const sourceLines = sourceText.split("\n")

  const printErrorContext = (color: Chalk, range: Range) => {
    const { startLine, startColumn, endLine, endColumn } = range
    const lineNoWidth = Math.max(String(endLine + 2).length, 2)
    const indent = " ".repeat(lineNoWidth + 2)

    const contextLines: string[] = []

    for (
      let i = Math.max(0, startLine - 2), j = Math.min(sourceLines.length - 1, startLine + 2);
      i <= j;
      i++
    ) {
      const sourceLine = sourceLines[i]!
      contextLines.push(chalk.blue(String(i + 1).padStart(lineNoWidth)) + ": " + sourceLine)
      if (i === startLine) {
        const before = " ".repeat(stringWidth(sourceLine.slice(0, startColumn)))
        let span: string
        if (startLine === endLine) {
          span = "^".repeat(Math.max(1, stringWidth(sourceLine.slice(startColumn, endColumn))))
        } else {
          span = "^".repeat(Math.max(1, stringWidth(sourceLine.slice(startColumn))))
        }
        contextLines.push(indent + before + color(span))
      } else if (endLine > i && i > startLine) {
        contextLines.push(indent + color("^".repeat(stringWidth(sourceLine))))
      }
    }

    if (endLine - startLine > 5) {
      contextLines.push(indent + "        ...")
    }

    if (endLine > startLine) {
      for (
        let i = Math.max(startLine + 2, endLine - 2),
          j = Math.min(sourceLines.length - 1, endLine + 2);
        i <= j;
        i++
      ) {
        const sourceLine = sourceLines[i]!
        contextLines.push(chalk.blue(String(i + 1).padStart(lineNoWidth)) + ": " + sourceLine)
        if (i < endLine) {
          contextLines.push(indent + color("^".repeat(stringWidth(sourceLine))))
        } else if (i === endLine) {
          contextLines.push(indent + color("^".repeat(stringWidth(sourceLine.slice(0, endColumn)))))
        }
      }
    }

    for (const contextLine of contextLines) {
      console.log(contextLine)
    }
  }

  // TODO <stdin> constant
  const path = sourcePath === "<stdin>" ? sourcePath : pathMod.relative(process.cwd(), sourcePath)
  for (const diag of diagnostics) {
    const { severity, range, message } = diag
    switch (severity) {
      case DiagnosticSeverity.Warning:
        console.log(chalk.yellow("warning:") + " " + message)
        console.log(
          "    at " + chalk.underline(`${sourcePath}:${range.startLine + 1}:${range.startColumn + 1}`),
        )
        console.log()
        printErrorContext(chalk.yellow, range)
        break
      case DiagnosticSeverity.Error:
        console.log(chalk.red("error:") + "   " + message)
        console.log(
          "    at " + chalk.underline(`${sourcePath}:${range.startLine + 1}:${range.startColumn + 1}`),
        )
        console.log()
        printErrorContext(chalk.red, range)
        break
    }
    console.log()
  }
}
