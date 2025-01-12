import { ExprCheckType, ExprValueType, FuncTypeSpec, NameResolver } from "../expr"

// TODO remove all Error types
const FORMULA_FUNCTIONS: Record<string, NonEmptyArray<FuncTypeSpec>> = {
  SUM: [
    {
      params: {
        positional: [ExprValueType.Number],
        rest: ExprValueType.Number,
      },
      ret: ExprValueType.Number,
    },
  ],
  AVERAGE: [
    {
      params: {
        positional: [ExprValueType.Number],
        rest: ExprValueType.Number,
      },
      ret: ExprValueType.Number,
    },
  ],
  MAX: [
    {
      params: {
        positional: [ExprValueType.Number],
        rest: ExprValueType.Number,
      },
      ret: ExprValueType.Number,
    },
  ],
  MIN: [
    {
      params: {
        positional: [ExprValueType.Number],
        rest: ExprValueType.Number,
      },
      ret: ExprValueType.Number,
    },
  ],
  ROUND: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  ROUNDUP: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  ROUNDDOWN: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  CEILING: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  FLOOR: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  EVEN: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  ODD: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  INT: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  ABS: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  SQRT: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  MOD: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  POWER: [
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  EXP: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  LOG: [
    {
      params: {
        positional: [ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  VALUE: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  CONCATENATE: [
    {
      params: {
        positional: [ExprValueType.String],
        rest: ExprValueType.String,
      },
      ret: ExprValueType.String,
    },
  ],
  FIND: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  SEARCH: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String, ExprValueType.Number],
      },
      ret: ExprValueType.Number,
    },
  ],
  MID: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.Number, ExprValueType.Number],
      },
      ret: ExprValueType.String,
    },
  ],
  REPLACE: [
    {
      params: {
        positional: [
          ExprValueType.String,
          ExprValueType.Number,
          ExprValueType.Number,
          ExprValueType.String,
        ],
      },
      ret: ExprValueType.String,
    },
  ],
  SUBSTITUTE: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String, ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
    {
      params: {
        positional: [
          ExprValueType.String,
          ExprValueType.String,
          ExprValueType.String,
          ExprValueType.Number,
        ],
      },
      ret: ExprValueType.String,
    },
  ],
  LEN: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  LEFT: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.Number],
      },
      ret: ExprValueType.String,
    },
  ],
  RIGHT: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.Number],
      },
      ret: ExprValueType.String,
    },
  ],
  LOWER: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
  ],
  UPPER: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
  ],
  REPT: [
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.Number],
      },
      ret: ExprValueType.String,
    },
  ],
  T: [
    {
      params: {
        positional: [ExprCheckType.Error],
      },
      ret: ExprValueType.String,
    },
  ],
  TRIM: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
  ],
  ENCODE_URL_COMPONENT: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
  ],
  // TODO refine this type
  IF: [
    {
      params: {
        positional: [ExprValueType.Boolean, ExprCheckType.Error, ExprCheckType.Error],
      },
      ret: ExprCheckType.Error,
    },
  ],
  // TODO refine this type
  SWITCH: [
    {
      params: {
        positional: [ExprCheckType.Error],
        rest: ExprCheckType.Error,
      },
      ret: ExprCheckType.Error,
    },
  ],
  TRUE: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  FALSE: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  AND: [
    {
      params: {
        positional: [ExprValueType.Boolean],
        rest: ExprValueType.Boolean,
      },
      ret: ExprValueType.Boolean,
    },
  ],
  OR: [
    {
      params: {
        positional: [ExprValueType.Boolean],
        rest: ExprValueType.Boolean,
      },
      ret: ExprValueType.Boolean,
    },
  ],
  XOR: [
    {
      params: {
        positional: [ExprValueType.Boolean],
        rest: ExprValueType.Boolean,
      },
      ret: ExprValueType.Boolean,
    },
  ],
  BLANK: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.String,
    },
  ],
  ERROR: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprCheckType.Error,
    },
  ],
  ISERROR: [
    {
      params: {
        positional: [ExprCheckType.Error],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  IS_ERROR: [
    {
      params: {
        positional: [ExprCheckType.Error],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  TODAY: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  NOW: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  TONOW: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  FROMNOW: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  DATEADD: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.Number, ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  DATETIME_DIFF: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime],
      },
      ret: ExprValueType.DateTime,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  WORKDAY: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.Number],
      },
      ret: ExprValueType.DateTime,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.Number, ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  WORKDAY_DIFF: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  IS_AFTER: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  IS_BEFORE: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  IS_SAME: [
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime],
      },
      ret: ExprValueType.Boolean,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.Boolean,
    },
  ],
  DATETIME_FORMAT: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.String,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.String,
    },
  ],
  DATETIME_PARSE: [
    {
      params: {
        positional: [ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
    {
      params: {
        positional: [ExprValueType.String, ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  DATESTR: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.String,
    },
  ],
  TIMESTR: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.String,
    },
  ],
  YEAR: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  MONTH: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  WEEKDAY: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  WEEKNUM: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  DAY: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  HOUR: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  MINUTE: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  SECOND: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.Number,
    },
  ],
  SET_LOCALE: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.DateTime,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  SET_TIMEZONE: [
    {
      params: {
        positional: [ExprValueType.DateTime],
      },
      ret: ExprValueType.DateTime,
    },
    {
      params: {
        positional: [ExprValueType.DateTime, ExprValueType.String],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  CREATED_TIME: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  LAST_MODIFIED_TIME: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.DateTime,
    },
  ],
  ARRAYCOMPACT: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Array,
    },
  ],
  ARRAYUNIQUE: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Array,
    },
  ],
  ARRAYJOIN: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.String,
    },
  ],
  ARRAYFLATTEN: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Array,
    },
  ],
  COUNT: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Number,
    },
  ],
  COUNTA: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Number,
    },
  ],
  COUNTIF: [
    {
      params: {
        positional: [ExprValueType.Array, ExprCheckType.Error, ExprValueType.String],
      },
      ret: ExprValueType.Number,
    },
  ],
  COUNTALL: [
    {
      params: {
        positional: [ExprValueType.Array],
      },
      ret: ExprValueType.Number,
    },
  ],
  RECORD_ID: [
    {
      params: {
        positional: [],
      },
      ret: ExprValueType.String,
    },
  ],
}

export class FormulaFunctionResolver implements NameResolver {
  constructor(private readonly vars: Record<string, ExprValueType>) {}

  varType(name: string): ExprValueType | null {
    if (Object.hasOwn(this.vars, name)) {
      return this.vars[name]!
    }
    return null
  }

  varValue() {
    return null
  }

  funcType(name: string): NonEmptyArray<FuncTypeSpec> | null {
    return FORMULA_FUNCTIONS[name] ?? null
  }
}
