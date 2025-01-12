parser grammar Expr;

options { tokenVocab = DatasheetQLLexer; }

expr :
    factor
  | expr (TIMES | DIV | MOD) expr
  | expr (PLUS | MINUS) expr
  | expr CONCAT expr
  | expr (EQ | NE | GT | LT | GE | LE) expr
  | expr AND expr
  | expr OR expr
  ;

factor :
    MINUS factor
  | PLUS factor
  | NOT factor
  | LPAR expr RPAR
  | id
  | fnName LPAR expr (COMMA expr)* COMMA? RPAR
  | NUM_LITERAL
  | STR_LITERAL
  | AUTO
  ;

fnName :
    ID
  | MAX
  ;

id :
    ID
  | QUOTED_ID
  ;