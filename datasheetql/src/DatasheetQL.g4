parser grammar DatasheetQL;

options { tokenVocab = DatasheetQLLexer; }

import Expr;

program : stmt (SEMI stmt)* SEMI? ;

stmt : createTable ;

createTable :
  CREATE TABLE createTableBody (AND TABLE? createTableBody)* ;

createTableBody :
  id LPAR fieldSpec (COMMA fieldSpec)* (COMMA primaryKeyModifier)? COMMA? RPAR tableModifier? ;

tableModifier :
  COMMENT expr ;

primaryKeyModifier :
  primaryKey id ;

fieldSpec :
  id dataType fieldModifier* ;

fieldModifier :
    primaryKey
  | COMMENT expr
  | DEFAULT AUTO
  | DEFAULT expr
  ;

dataType :
    smallText
  | bigText
  | choice
  | number
  | currency
  | percentage
  | datetime
  | file
  | member
  | checkbox
  | rating
  | url
  | phone
  | email
  | link
  | lookup
  | formula
  | autonum
  | createdDatetime
  | modifiedDatetime
  | creator
  | modifier
  ;

smallText :
  TEXT ;

bigText :
    LONG TEXT
  | MULTILINE TEXT
  | MULTI LINE TEXT
  ;

choice :
    CHOICE choiceModifier*
  | MULTI CHOICE choiceModifier*
  ;

choiceModifier :
    MULTI
  | LPAR choiceList RPAR
  ;

choiceList :
    expr (COMMA expr)* COMMA?
  |
  ;

primaryKey :
  PRIMARY KEY? ;

number :
    NUMBER numberModifier*
  | NUMBER expr numberModifier*
  ;

numberModifier :
    UNIT expr
  | PRECISION expr
  | SEPARATOR
  ;

currency :
    CURRENCY currencyModifier*
  | CURRENCY expr currencyModifier*
  ;

currencyModifier :
    LEFT
  | RIGHT
  | UNIT expr
  | PRECISION expr
  ;

percentage :
    PERCENTAGE expr percentageModifier*
  | PERCENTAGE percentageModifier*
  ;

percentageModifier :
  PRECISION expr
  ;

datetime :
    DATETIME
  | DATETIME expr
  ;

file :
  FILE
  ;

member :
    MEMBER memberModifier*
  | MULTI MEMBER memberModifier*
  ;

memberModifier :
    MULTI
  | NOTIFY
  ;

checkbox :
    CHECKBOX
  | CHECKBOX expr
  ;

rating :
    RATING ratingModifier*
  | RATING expr ratingModifier*
  ;

ratingModifier :
  MAX expr
  ;

url :
  URL urlModifier*
  ;

urlModifier :
  TITLE
  ;

phone :
  PHONE
  ;

email :
  EMAIL
  ;

link :
    LINK linkModifier*
  | MULTI LINK linkModifier*
  ;

linkModifier :
    VIA VIEW id
  | TO id
  | id
  | MULTI
  ;

lookup :
  LOOKUP id lookupModifier*
  ;

lookupModifier :
    WHEN expr
  | statFunc OF id
  ;

statFunc :
    MAX
  | id
  ;

formula :
  FORMULA expr formulaModifier*
  ;

formulaModifier :
    PRECISION expr
  | SEPARATOR
  | NUMBER
  | UNIT expr
  | CURRENCY
  | PERCENTAGE
  | DATETIME
  | DATETIME expr
  ;

autonum :
  AUTO NUMBER
  ;

createdDatetime :
    CREATED DATETIME
  | CREATED DATETIME expr
  | DATETIME CREATED
  | DATETIME CREATED expr
  ;

modifiedDatetime :
    MODIFIED DATETIME modifiedFieldList?
  | MODIFIED DATETIME expr modifiedFieldList?
  | DATETIME MODIFIED modifiedFieldList?
  | DATETIME MODIFIED expr modifiedFieldList?
  ;

modifiedFieldList :
  OF LPAR id (COMMA id)* COMMA? RPAR
  ;

creator :
  CREATOR
  ;

modifier :
  MODIFIER modifiedFieldList?
  ;

id :
    ID
  | QUOTED_ID
  ;