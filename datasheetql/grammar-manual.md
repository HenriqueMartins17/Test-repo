# DatasheetQL Grammar Manual

This document in other languages: [中文](./grammar-manual-zh.md)

A DatasheetQL source file is composed of zero or more DatasheetQL statements, separated by `;`s.  
The last statement can be followed by an optional `;`.

<!-- toc -->

- [Lexical Rules](#lexical-rules)
- [Create a Table](#create-a-table)
  * [Table modifiers](#table-modifiers)
    + [Comment](#comment)
  * [Field Declaration](#field-declaration)
    + [Field Modifiers](#field-modifiers)
      - [Primary Key](#primary-key)
      - [Comment](#comment-1)
      - [Default Value](#default-value)
    + [Field Types](#field-types)
      - [Single Line Text](#single-line-text)
      - [Multiline Text](#multiline-text)
      - [Choice](#choice)
      - [Number](#number)
      - [Currency](#currency)
      - [Percentage](#percentage)
      - [DateTime](#datetime)
      - [Attachment](#attachment)
      - [Member](#member)
      - [Check Box](#check-box)
      - [Rating](#rating)
      - [URL](#url)
      - [Phone Number](#phone-number)
      - [E-mail](#e-mail)
      - [Magic Link](#magic-link)
      - [Magic Lookup](#magic-lookup)
      - [Formula](#formula)
      - [Auto Increment Number](#auto-increment-number)
      - [Created Datetime](#created-datetime)
      - [Last Modification Datetime](#last-modification-datetime)
      - [Creator](#creator)
      - [Modifier](#modifier)
- [Expression](#expression)
  * [Constant Expression](#constant-expression)
  * [Filter Expression](#filter-expression)
  * [Formula Expression](#formula-expression)
  * [Operator](#operator)

<!-- tocstop -->

## Lexical Rules

- All keywords are case insensitive, but table names, field names, and view names are case sensitive.
- The rules of identifiers (including table names, field names, etc.):
  - Starts with a letter (Unicode letter) or an underscore, followed by zero or more letters, digits, or underscores. (also Unicode ones)
  - An identifier can be surrounded by curly brackets, such as `{🥰 ㏴}`.  
    If written in this form, an identifier can contain non-letter and digit characters, such as emojis or spaces.  
    If the identifier contains curly brackets, escape it with a `\`, e.g. `{{A\}}` represents the identifier `{A}`.  
    If the identifier contains backslashes, also escape it with a `\`, e.g. `{1\\2}` represents the identifier `1\2`.
- String literals are surrounded by doute quotes, e.g. `"abc"`.  
  Escape sequences:
  - `\\` represents `\`.
  - `\n` represents newline.
  - `\"` represents double quote.
- A line comment starts with `--` (two dashes) and continues until the end of the same line.

## Create a Table

Create a single table:

```sql
CREATE TABLE <table name> ( <field list> ) <table modifier>
```

Creating multiple tables simultaneously (Tables can link to each other):

```sql
CREATE TABLE <table name> ( <field list> ) <table modifier> AND TABLE <table name> ( <field list> ) ...
```

- `<table modifier>` is optional.
- `<field list>` is a sequence of one or more _field declarations_, separated by commas.
- Field names in a table must be unique.
- If the primary key of a table is not the first field, it will be moved to the first column.
- A table must have **exactly one** primary key.
- The field list can be followed by the modifier `PRIMARY KEY <field name>` to specify the primary key, e.g.
  ```sql
  CREATE TABLE example1 ( field1 TEXT, field2 NUMBER, PRIMARY KEY field1 )
  ```
- When creating multiple tables simultaneously, all `TABLE` keywords before the table names can be omitted, except the first one, e.g.
  ```sql
  CREATE TABLE example1 ( field1 CURRENCY PRIMARY KEY ) AND example2 ( field1 TEXT )
  ```

### Table modifiers

#### Comment

Syntax:

```sql
COMMENT <comment expression>
```

where `<comment expression>` is a [constant expression](#Constant%20Expression), whose result must be a string and will be used as the description of the table.

Example:

```sql
CREATE TABLE table1 (
  field1 NUMBER PRIMARY KEY
) COMMENT "This is table1";
```

### Field Declaration

Syntax:

```sql
<field name> <field type> <field modifier list>
```

where `<field modifier list>` is a sequence of zero or more *field modifier*s, separated by spaces.

Example:

```sql
field1 CURRENCY UNIT "$/kg"
```

#### Field Modifiers

##### Primary Key

Syntax:

```sql
PRIMARY KEY
```

where `KEY` can be omitted.  
A table must have **exactly one** primary key.

Field types that can be used as a primary key: single line text, multiline text, number, currency, percentage, datetime, URL, phone number, E-mail, formula, and auto increment number.

Example:

```sql
CREATE TABLE table1 (
  field1 NUMBER PRIMARY KEY
);
```

##### Comment

Syntax:

```sql
COMMENT <comment expression>
```

where `<comment expression>` is a [constant expression](#Constant%20Expression), whose result must be a string and will be used as the description of the field.

Example:

```sql
CREATE TABLE table1 (
  field1 NUMBER PRIMARY KEY
);
```

##### Default Value

Syntax:

```sql
DEFAULT <default value expression>
```

where `<default value expression>` is a [constant expression](#Constant%20Expression), whose result must matches the type of the field.

Field types that allow default values: single line text, choice, number, currency, percentage.

Multiple default items is not supported for multiple choice field currently.

If the field is of datetime type, a special default value `AUTO` can be specified, meaning the current timestamp will be filled into the field when a record is newly created.

Example:

```sql
CREATE TABLE table1 (
  field1 TEXT DEFAULT ("foo" & "bar"),
  field2 DATETIME DEFAULT AUTO
);
```

#### Field Types

##### Single Line Text

Syntax:

```sql
TEXT
```

##### Multiline Text

Syntax:

```sql
LONG TEXT
```

or

```sql
MULTILINE TEXT
```

or

```sql
MULTI LINE TEXT
```

##### Choice

Single choice:

```sql
CHOICE <choice modifier list>
```

Multiple choices:

```sql
MULTI CHOICE <choice modifier list>
```

where `<choice modifier list>` is a sequence of zero or more *choice field modifier*s, separated by spaces.

**Choice modifier**:

- `( <item list> )`: Item list is a sequence of zero or more choice items, separated by commas. Each item is a [constant expression](#Constant%20Expression) whose result is a string, or an identifier.  
  **Limitation**: Items must be unique, but API Table does not have this limitation.
- `MULTI`: Make this field a multiple choice.  
  This modifier have the same effect as the above `MULTI CHOICE` syntax, so for example `CHOICE (male, female) MULTI` is identical to `MULTI CHOICE (male, female)`.

##### Number

Syntax:

```sql
NUMBER <precision> <number modifier list>
```

where `<precision>` is a constant expression, whose result is a number and one of `1`, `0.1`, `0.01`, `0.001` or `0.0001`.  
`<precision>` is optional, the default precision is `1`.

`<number modifier list>` is a sequence of zero or more *number field modifier*s, separated by spaces.

**Number modifier**:

- `PRECISION <precision>`: Same as the above `<precision>` notation.
- `UNIT <unit>`: Specifies the unit of the number。`<unit>` is a constant expression whose result is a string, and currency mnemonics as in [Currency](#Currency) can be used, such as `CNY` and `EUR`. Example: `field1 NUMBER UNIT RMB`
- `SEPARATOR`: Shows thousands separator.

##### Currency

Syntax:

```sql
CURRENCY <precision> <currency modifier list>
```

where `<precision>` is a constant expression, whose result is a number and one of `1`, `0.1`, `0.01`, `0.001` or `0.0001`.  
`<precision>` is optional, the default precision is `1`.

`<currency modifier list>` is a sequence of zero or more *currency field modifier*s, separated by spaces.

**Currency modifier**:

- `UNIT <unit>`: Specifies the symbol of the currency。`<unit>` is a constant expression whose result is a string, and _currency mnemonics_ as in the following table can be used.
- `LEFT`: Shows currency symbol at the left side. Exclusive with `RIGHT`.
- `RIGHT`: Shows currency symbol at the right side. Exclusive with `LEFT`.

**Currency mnemonics**:

|    Name    | symbol |
| :--------: | :----: |
| CNY or RMB |   ￥   |
|    JPY     |   ￥   |
|    USD     |   ＄   |
|    EUR     |   €    |
|    GBP     |   ￡   |

Currency mnemonics occuring in `<unit>` expressions will be replaced by their corresponding currency symbols, and these mnemonics can be used in calculations, e.g. the result of `CNY & "/kg"` is `￥/kg`.

Currency mnemonics are case insensitive, e.g. `cny` is identical to `CNY`.

##### Percentage

Syntax:

```sql
PERCENTAGE <precision> <percentage modifier list>
```

where `<precision>` is a constant expression, whose result is a number and one of `1`, `0.1`, `0.01`, `0.001` or `0.0001`.  
`<precision>` is optional, the default precision is `1`.

`<percentage modifier list>` is a sequence of zero or more *percentage field modifier*s, separated by spaces.

**Percentage modifier**:

- `PRECISION <precision>`: Same as the above `<precision>` notation.

##### DateTime

Syntax:

```sql
DATETIME <date&time format>
```

where `<date&time format>` is a constant expression whose result is a string.  
`<date&time format>` is optional.

Date&time format: Date format followed by time format, no separator is needed.  
Time format is optional, is omitted, time is not shown.

**Date format**：

- `YYYY/MM/DD`, e.g. `2022/04/28`
- `YYYY-MM-DD`, e.g. `2022-04-28`
- `DD/MM/YYYY`, e.g. `28/04/2022`
- `YYYY-MM`, e.g. `2022-04`
- `MM-DD`, e.g. `04-28`
- `YYYY`, e.g. `2022`
- `MM`, e.g. `04`
- `DD`, e.g. `28`

**Time format**:

- `hh:mm`: Time is shown in 12-hour form (AM/PM).
- `HH:mm`: Time is shown in 24-hour form.

Example: `YYYY/MM/DD HH:mm`, spaces can be added properly to improve readability.

##### Attachment

Syntax:

```sql
FILE
```

##### Member

Only single member can be selected:

```sql
MEMBER <member modifier list>
```

Multiple members can be selected:

```sql
MULTI MEMBER <member modifier list>
```

where `<member modifier list>` is a sequence of zero or more *member field modifier*s separated by spaces.

**Member modifier**:

- `NOTIFY`: Notify the member that is newly selected.
- `MULTI`: Multiple members can be selected.  
  Same as the above `MULTI MEMBER` notation, so a field declaration of the form `MEMBER NOTIFY MULTI` is identical to `MULTI MEMBER NOTIFY`.

##### Check Box

Syntax:

```sql
CHECKBOX <checked symbol>
```

where `<checked symbol>` is a constant expression whose result is a string and must be a emoji character.  
`<checked symbol>` is optional, if omitted, it defaults to `✅`.

##### Rating

Syntax:

```sql
RATING <rating symbol> <rating modifier list>
```

where `<rating symbol>` is a constant expression whose result is a string and must be a emoji character.  
`<rating symbol>` is optional and defaults to `⭐` if omitted.

`<rating modifier list>` is a sequence of zero or more *rating field modifier*s, separated by spaces.

**Rating modifier**:

- `MAX <maximum rating>`: Specifies the maximum rating point. `<maximum rating>` is a constant expression whose result is an integer and between 1 and 10 (both inclusive).

##### URL

Syntax:

```sql
URL <URL modifier list>
```

where `<URL modifier list>` is a sequence of zero or more *URL field modifier*s, separated by spaces.

**URL modifier**:

- `TITLE`: Shows page title instead of URL address.

##### Phone Number

Syntax:

```sql
PHONE
```

##### E-mail

Syntax:

```sql
EMAIL
```

##### Magic Link

Only one record can be linked:

```sql
LINK <link modifier list>
```

Multiple records can be linked:

```sql
MULTI LINK <link modifier list>
```

where `<link modifier list>` is a sequence of zero or more *link field modifier*s, separated by spaces.

**Link modifier**:

- `TO <table name>`: Specifies the table that is linked to. This modifier is required. The `TO` keyword can be omitted.
- `VIA VIEW <view name>`: Specifies the view from which the records will be selected.
- `MULTI`: Multiple records can be selected.  
  Same as the above `MULTI LINK` notation, so a field declaration of the form `LINK TO tableName MULTI` is identical to `MULTI LINK TO tableName`.

##### Magic Lookup

Syntax:

```sql
LOOKUP <link field> <lookup modifier list>
```

where `<link field>` is a field of type _magic link_ is the same table.  
`<lookup modifier list>` is a sequence of zero or more *lookup field modifier*s, separated by spaces.

Limitation: result format of statistic function is not supported currently.

**Lookup modifier**:

- `WHEN <filter condition>`: Specifies the condition of record being taken into acocunt by the statistic function. `<filter condition>` is a [filter expression](#Filter%20Expression) whose result is a boolean.
- `<statistic function name> OF <statistic field name>`: Specifies the statistic function that is applied to the given field of the linked table. This modifier is required.
  Available statistic functions:
  - `VALUES`: The referenced values are shown verbatim.
  - `AVERAGE`: The average of numbers.
  - `COUNT`: Count the number of valid _numbers_.
  - `COUNTA`: Count the number of truthy values.
  - `COUNTALL`: Count the number of records.
  - `SUM`: The sum of numbers.
  - `MAX`: The maximum value of numbers.
  - `MIN`: The maximum value of numbers.
  - `AND`: Logical conjunction of boolean values.
  - `OR`: Logical disjunction of boolean values.
  - `XOR`: Logical exclusive disjunction of boolean values.
  - `CONCATENATE`: Concatenate values into a string.
  - `ARRAYJOIN`: Concatenate values into a string, separated by commas.
  - `ARRAYUNIQUE`: Remove duplicate values.
  - `ARRAYCOMPACT`: Remove null values.

##### Formula

Syntax:

```sql
FORMULA <formula expression> <formula modifier list>
```

where `<formula expression>` is a [formula expression](#Formula%20Expression).  
`<formula modifier list>` is a sequence of zero or more *formula field modifier*s, separated by spaces.

**Formula modifier**:

- `NUMBER`: Specifies the result of the formula is shown in number form. This modifier can only be used when the result of the formula is a number.
- `CURRENCY`: Specifies the result of the formula is shown in currency form. This modifier can only be used when the result of the formula is a number.
- `PERCENTAGE`: Specifies the result of the formula is shown in percentage form. This modifier can only be used when the result of the formula is a number.
- `PRECISION <precision>`: Specifies the precision of number, currency or percentage. This modifier must be used in companion with `NUMBER`, `CURRENCY` or `PERCENTAGE`.
- `UNIT <symbol>`: Specifies the symbol of currency. This modifier must be used in companion with `CURRENCY`.
- `SEPARATOR`: Shows thousands separator of the number. This modifier must be used in companion with `NUMBER`.
- `DATETIME <date&time format>`: Specifies the result of the formula is shown in date & time form. This modifier can only be used when the result of the formula is a datetime.  
  `<date&time format>` is optional and defaults to `"YYYY-MM-DD hh:mm"`.

##### Auto Increment Number

Syntax:

```sql
AUTO NUMBER
```

##### Created Datetime

Syntax:

```sql
CREATED DATETIME <date&time format>
```

or

```sql
DATETIME CREATED <date&time format>
```

where `<date&time format>` is optional.

##### Last Modification Datetime

Syntax:

```sql
MODIFIED DATETIME <date&time format> <modified-datetime modifier list>
```

or

```sql
DATETIME MODIFIED <date&time format> <modified-datetime modifier list>
```

where `<date&time format>` is optional.  
`<modified-datetime modifier list>` is a sequence of zero or more *modified-datetime field modifier*s, separated by spaces.

**Modified-datetime modifier**:

- `OF ( <field name list> )`: Specifies the list of fields whose modification triggers the update of the datetime.  
  `<field name list>` is a sequence of one or more field names, separated by commas.

##### Creator

Syntax:

```sql
CREATOR
```

##### Modifier

Syntax:

```sql
MODIFIER <modifier modifier list>
```

where `<modifier modifier list>` is a sequence of zero or more *modifier field modifier*s, separated by spaces.

**Modifier modifier**:

- `OF ( <field name list> )`: Specifies the list of fields whose modification triggers the update of the datetime.  
  `<field name list>` is a sequence of one or more field names, separated by commas.

## Expression

Expressions in DatasheetQL play different role in different places, and different restrictions are enforced to different roles.

### Constant Expression

Expressions that are computed at compile time, so no fields can be referenced in the expression.

### Filter Expression

Expressions that are used in the filter condition of lookup fields.

Due to the limitation of Fusion API, this type of expression is not supported currently.
goes from higher to lower

### Formula Expression

Expressions that are used in formula fields. All other fields can be referenced, and all APITable formula functions can be used.

### Operator

Operators in the following table are listed in descending precedence, operators in the same line have the same precedence.

| Category             |   Associativity   | Operators                                                                                                                          |
| :------------------- | :---------------: | :--------------------------------------------------------------------------------------------------------------------------------- |
| Unary                | Right-associative | `+`: Coerce the operand into a number<br>`-`: Negate the operand<br>`NOT` or `!`: Logical negation                                 |
| Arithmetic           | Left-associative  | `*`: Multiplication<br>`/`: Division<br>`%`: Remainder                                                                             |
| Arithmetic           | Left-associative  | `+`: Addition or string concatenation<br>`-`: Subtraction                                                                          |
| String operation     | Left-associative  | `&`: String concatenation                                                                                                          |
| Relational operation | Left-associative  | `=`: Equals<br>`!=`: Not equals<br>`>`: Greater than<br>`<`: Less than<br>`>=`: Greater than or equals<br>`<=`:Less than or equals |
| Logical operation    | Left-associative  | `AND` or `&&`: Logical conjunction                                                                                                               |
| Logical operation    | Left-associative  | `OR` or <code>&#124;&#124;</code>: Logical disjunction                                                                                           |
