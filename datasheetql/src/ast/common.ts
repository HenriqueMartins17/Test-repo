import { Range } from "../diagnostic"

export type Value<T> = {
  value: T
  range: Range
}