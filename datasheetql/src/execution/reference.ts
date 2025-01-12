import { QueryResponse } from "."

/**
 * reference a query response in successive query body
 */
export type QueryResponseReference = {
  '👨‍👨‍👦referenceIndex$$': number
}

export function newQueryResponseRef(index: number): QueryResponseReference {
  return {
    '👨‍👨‍👦referenceIndex$$': index,
  }
}

export function resolveQueryResponseRefs(data: any, responses: QueryResponse[]): any {
  if (data == null) {
    return data
  }

  if (typeof data !== 'object') {
    return data
  }

  if (Array.isArray(data)) {
    return data.map(elem => resolveQueryResponseRefs(elem, responses)) as any
  }

  if ('👨‍👨‍👦referenceIndex$$' in data) {
    return responses[(data as QueryResponseReference)["👨‍👨‍👦referenceIndex$$"]]
  }

  const result: Record<string, any> = {}
  for (const k in data) {
    result[k] = resolveQueryResponseRefs(data[k], responses)
  }
  return result as any
}