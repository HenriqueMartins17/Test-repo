import { DateFormat, TimeFormat } from "../ast"

const enum TokenKind {
  YY, // Two-digit year
  YYYY, // Four-digit year
  M, // Month, beginning at 1
  MM, // Month, 2-digits
  MMM, // The abbreviated month name
  MMMM, // The full month name
  D, // Day of month
  DD, // Day of month, 2-digits
  H, // Hours
  HH, // Hours, 2-digits
  h, // Hours, 12-hour clock
  hh, // Hours, 12-hour clock, 2-digits
  m, // Minutes
  mm, // Minutes, 2-digits
  s, // Seconds
  ss, // Seconds, 2-digits
  S, // Hundreds of milliseconds, 1-digit
  SS, // Tens of milliseconds, 2-digits
  SSS, // Milliseconds, 3-digits
  Z, // Offset from UTC
  ZZ, // Compact offset from UTC, 2-digits
  A, // AM PM Post or ante meridiem, upper-case
  a, // am pm Post or ante meridiem, lower-case
  Do, // Day of Month with ordinal
  X, // Unix timestamp
  x, // Unix ms timestamp
  Char,
  EOF,
}

type Token =
  | {
      start: number
      /**
       * exclusive
       */
      end: number
      kind: TokenKind.Char
      text: string
    }
  | {
      start: number
      /**
       * exclusive
       */
      end: number
      kind: Exclude<TokenKind, TokenKind.Char | TokenKind.EOF>
    }
  | {
      start: number
      kind: TokenKind.EOF
    }

export type DateTimeFormatError = {
  position: number
}

export type DateTimeFormat = {
  date: { format: DateFormat; start: number; end: number }
  time?: { format: TimeFormat; start: number; end: number }
}

const DATE_REGEX = /^\s*(?:(YYYY\s*\/\s*MM\s*\/\s*DD)|(YYYY\s*-\s*MM\s*-\s*DD)|(DD\s*\/\s*MM\s*\/\s*YYYY)|(YYYY\s*-\s*MM)|(MM\s*-\s*DD)|(YYYY)|(MM)|(DD))\s*/
const TIME_REGEX = /^(?:(HH\s*:\s*mm\s*)|(hh\s*:\s*mm\s*)|)$/

// TODO use levenshtein automaton
export function parseDatetimeFormat(format: string): DateTimeFormat | DateTimeFormatError {
  const m1 = DATE_REGEX.exec(format)
  if (!m1) {
    return {
      position: 0
    }
  }
  let date: DateFormat
  if (m1[1]) {
    date = DateFormat["YYYY/MM/DD"]
  } else if (m1[2]) {
    date = DateFormat["YYYY-MM-DD"]
  } else if (m1[3]) {
    date = DateFormat["DD/MM/YYYY"]
  } else if (m1[4]) {
    date = DateFormat["YYYY-MM"]
  } else if (m1[5]) {
    date = DateFormat["MM-DD"]
  } else if (m1[6]) {
    date = DateFormat.YYYY
  } else if (m1[7]) {
    date = DateFormat.MM
  } else if (m1[8]) {
    date = DateFormat.DD
  } else {
    throw new Error('unreachable ' + m1[0])
  }

  let time: TimeFormat | undefined
  const m2 = TIME_REGEX.exec(format.slice(m1[0]!.length))
  if (!m2) {
    return {
      position: 0
    }
  }
  if (m2[1]) {
    time = TimeFormat["HH:mm"]
  } else if (m2[2]) {
    time = TimeFormat["hh:mm"]
  }

  return {
    date: {
      format: date,
      start: 0,
      end: m1[0]!.length,
    },
    time: time === undefined ? undefined : {
      format: time,
      start: m1[0]!.length,
      end: format.length
    }
  }
}

const TOKEN_REGEX =
  /(\s+)|[-_:.,()/]|YYYY|YY|MMMM|MMM|MM|M|DD|D|HH|H|hh|h|mm|m|ss|s|SSS|SS|S|ZZ|Z|A|a|Do|X|x/gy

function tokenizeDatetimeFormat(format: string): Token[] | DateTimeFormatError {
  TOKEN_REGEX.lastIndex = 0
  let match: RegExpExecArray | null
  let lastIndex = 0
  const tokens: Token[] = []
  while (
    (lastIndex = TOKEN_REGEX.lastIndex) < format.length &&
    (match = TOKEN_REGEX.exec(format))
  ) {
    if (match[1]) {
      continue
    }

    const segment = match[0]!
    let kind: TokenKind
    switch (segment) {
      case "YYYY":
        kind = TokenKind.YYYY
        break
      case "YY":
        kind = TokenKind.YY
        break
      case "MMMM":
        kind = TokenKind.MMMM
        break
      case "MMM":
        kind = TokenKind.MMM
        break
      case "MM":
        kind = TokenKind.MM
        break
      case "M":
        kind = TokenKind.M
        break
      case "DD":
        kind = TokenKind.DD
        break
      case "D":
        kind = TokenKind.D
        break
      case "HH":
        kind = TokenKind.HH
        break
      case "H":
        kind = TokenKind.H
        break
      case "hh":
        kind = TokenKind.hh
        break
      case "h":
        kind = TokenKind.h
        break
      case "mm":
        kind = TokenKind.mm
        break
      case "m":
        kind = TokenKind.m
        break
      case "ss":
        kind = TokenKind.ss
        break
      case "s":
        kind = TokenKind.s
        break
      case "SSS":
        kind = TokenKind.SSS
        break
      case "SS":
        kind = TokenKind.SS
        break
      case "S":
        kind = TokenKind.S
        break
      case "ZZ":
        kind = TokenKind.ZZ
        break
      case "Z":
        kind = TokenKind.Z
        break
      case "A":
        kind = TokenKind.A
        break
      case "a":
        kind = TokenKind.a
        break
      case "Do":
        kind = TokenKind.Do
        break
      case "X":
        kind = TokenKind.X
        break
      case "x":
        kind = TokenKind.x
        break
      default:
        tokens.push({
          start: lastIndex,
          end: lastIndex + segment.length,
          kind: TokenKind.Char,
          text: segment,
        })
        continue
    }
    tokens.push({
      start: lastIndex,
      end: lastIndex + segment.length,
      kind,
    })
  }

  if (TOKEN_REGEX.lastIndex < format.length) {
    return {
      position: TOKEN_REGEX.lastIndex,
    }
  }

  tokens.push({
    start: format.length,
    kind: TokenKind.EOF,
  })

  return tokens
}