import eastAsianWidth from "eastasianwidth"
import emojiRegex from "emoji-regex"

export interface WidthOptions {
  /**
	Count [ambiguous width characters](https://www.unicode.org/reports/tr11/#Ambiguous) as having narrow width (count of 1) instead of wide width (count of 2).

	@default true
	*/
  readonly ambiguousIsNarrow?: boolean
}

/**
 * Get terminal display width of a string.
 *
 * from https://www.npmjs.com/package/string-width
 */
export function stringWidth(string: string, options: WidthOptions = {}): number {
  if (typeof string !== "string" || string.length === 0) {
    return 0
  }

  options = {
    ambiguousIsNarrow: true,
    ...options,
  }

  string = stripAnsi(string)

  if (string.length === 0) {
    return 0
  }

  string = string.replace(emojiRegex(), "  ")

  const ambiguousCharacterWidth = options.ambiguousIsNarrow ? 1 : 2
  let width = 0

  for (const character of string) {
    const codePoint = character.codePointAt(0)!

    // Ignore control characters
    if (codePoint <= 0x1f || (codePoint >= 0x7f && codePoint <= 0x9f)) {
      continue
    }

    // Ignore combining characters
    if (codePoint >= 0x300 && codePoint <= 0x36f) {
      continue
    }

    const code = eastAsianWidth.eastAsianWidth(character)
    switch (code) {
      case "F":
      case "W":
        width += 2
        break
      case "A":
        width += ambiguousCharacterWidth
        break
      default:
        width += 1
    }
  }

  return width
}

/**
Strip [ANSI escape codes](https://en.wikipedia.org/wiki/ANSI_escape_code) from a string.

@example
```
import stripAnsi from 'strip-ansi';

stripAnsi('\u001B[4mUnicorn\u001B[0m');
//=> 'Unicorn'

stripAnsi('\u001B]8;;https://github.com\u0007Click\u001B]8;;\u0007');
//=> 'Click'
```
*/
export function stripAnsi(string: string): string {
  if (typeof string !== "string") {
    throw new TypeError(`Expected a \`string\`, got \`${typeof string}\``)
  }

  return string.replace(ansiRegex(), "")
}

export interface AnsiOptions {
  /**
	Match only the first ANSI escape.

	@default false
	*/
  readonly onlyFirst: boolean
}

/**
Regular expression for matching ANSI escape codes.

@example
```
import ansiRegex from 'ansi-regex';

ansiRegex().test('\u001B[4mcake\u001B[0m');
//=> true

ansiRegex().test('cake');
//=> false

'\u001B[4mcake\u001B[0m'.match(ansiRegex());
//=> ['\u001B[4m', '\u001B[0m']

'\u001B[4mcake\u001B[0m'.match(ansiRegex({onlyFirst: true}));
//=> ['\u001B[4m']

'\u001B]8;;https://github.com\u0007click\u001B]8;;\u0007'.match(ansiRegex());
//=> ['\u001B]8;;https://github.com\u0007', '\u001B]8;;\u0007']
```
*/
export function ansiRegex(options: AnsiOptions = { onlyFirst: false }): RegExp {
  const { onlyFirst } = options
  const pattern = [
    "[\\u001B\\u009B][[\\]()#;?]*(?:(?:(?:(?:;[-a-zA-Z\\d\\/#&.:=?%@~_]+)*|[a-zA-Z\\d]+(?:;[-a-zA-Z\\d\\/#&.:=?%@~_]*)*)?\\u0007)",
    "(?:(?:\\d{1,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-ntqry=><~]))",
  ].join("|")

  return new RegExp(pattern, onlyFirst ? undefined : "g")
}
