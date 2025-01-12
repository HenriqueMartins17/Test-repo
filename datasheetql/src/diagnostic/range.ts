export type Range = {
  /**
   * source file name
   */
  source: string
  /**
   * 0-indexed
   */
  startLine: number
  /**
   * 0-indexed
   */
  startColumn: number
  /**
   * 0-indexed
   */
  endLine: number
  /**
   * 0-indexed, exclusive
   */
  endColumn: number
}

export function trimColumns(range: Range, startColumnCount: number, endColumnCount: number): Range {
  return {
    ...range,
    startColumn: range.startColumn + startColumnCount,
    endColumn: range.endColumn + endColumnCount,
  }
}