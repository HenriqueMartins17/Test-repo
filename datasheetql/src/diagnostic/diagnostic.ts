import { Range } from "./range"

export const enum DiagnosticSeverity {
  Warning,
  Error,
}

export type Diagnostic = {
  severity: DiagnosticSeverity
  message: string
  range: Range
}

export function containsErrors(diagnostics: Array<Diagnostic>): boolean {
  return diagnostics.some((diag) => diag.severity === DiagnosticSeverity.Error)
}

export class DiagnosticCollector {
  readonly diagnostics: Diagnostic[] = []

  addError(message: string, range: Range) {
    this.diagnostics.push({
      severity: DiagnosticSeverity.Error,
      message,
      range,
    })
  }

  addWarning(message: string, range: Range) {
    this.diagnostics.push({
      severity: DiagnosticSeverity.Warning,
      message,
      range,
    })
  }

  containsErrors(): boolean {
    return containsErrors(this.diagnostics)
  }
}

export function newRange(source: string, line: number, column: number): Range
export function newRange(
  source: string,
  startLine: number,
  startColumn: number,
  endColumn: number,
): Range
export function newRange(
  source: string,
  startLine: number,
  startColumn: number,
  endLine: number,
  endColumn: number,
): Range

export function newRange(
  source: string,
  startLine: number,
  startColumn: number,
  endLineOrEndColumn?: number,
  endColumn?: number,
): Range {
  if (endLineOrEndColumn === undefined) {
    return {
      source,
      startLine,
      startColumn,
      endLine: startLine,
      endColumn: startColumn,
    }
  } else if (endColumn === undefined) {
    const endColumn = endLineOrEndColumn
    return {
      source,
      startLine,
      startColumn,
      endLine: startLine,
      endColumn,
    }
  } else {
    const endLine = endLineOrEndColumn
    return {
      source,
      startLine,
      startColumn,
      endLine,
      endColumn,
    }
  }
}
