use std::collections::HashMap;

use crate::formula::functions::basic::FormulaFunc;
use crate::formula::functions::date_time::{
  DayFunc, HourFunc, MinuteFunc, MonthFunc, SecondFunc, WeekNumFunc, WeekdayFunc, YearFunc,
};
use crate::formula::functions::logical::{
  AndFunc, BlankFunc, ErrorFunc, FalseFunc, IfFunc, IsErrorFunc, NotFunc, OrFunc, SwitchFunc, TrueFunc, XorFunc,
};
use crate::formula::functions::numeric::{
  AbsFunc, AverageFunc, CeilingFunc, EvenFunc, ExpFunc, FloorFunc, IntFunc, LogFunc, MaxFunc, MinFunc, ModFunc,
  OddFunc, PowerFunc, RoundDownFunc, RoundFunc, RoundUpFunc, SqrtFunc, SumFunc, ValueFunc,
};
use crate::formula::functions::record::RecordIdFunc;
use crate::formula::functions::text::{ConcatenateFunc, FindFunc, LeftFunc, LenFunc, MidFunc, ReplaceFunc, ReptFunc, RightFunc, SearchFunc, SubstituteFunc};
use crate::formula::i18n::Strings;

pub mod array;
pub mod basic;
pub mod date_time;
pub mod logical;
pub mod numeric;
pub mod record;
pub mod text;

pub struct Function {
  pub name: String,
  pub func: Box<dyn FormulaFunc>,
  pub summary: String,
  pub definition: String,
  pub example: String,
  pub link_url: Option<String>,
}

pub struct FunctionProvider {
  functions: HashMap<String, Function>,
}

// TODO: refactor
impl FunctionProvider {
  pub fn new() -> Self {
    let mut functions = HashMap::new();
    functions.insert(
      "SUM".to_string(),
      Function {
        name: "SUM".to_string(),
        func: Box::new(SumFunc::new()),
        definition: "SUM(number1, [number2, ...])".to_string(),
        summary: Strings::FunctionSumSummary.to_string(),
        example: Strings::FunctionSumExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "AVERAGE".to_string(),
      Function {
        name: "AVERAGE".to_string(),
        func: Box::new(AverageFunc::new()),
        definition: "AVERAGE(number1, [number2, ...])".to_string(),
        summary: Strings::FunctionAverageSummary.to_string(),
        example: Strings::FunctionAverageExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MAX".to_string(),
      Function {
        name: "MAX".to_string(),
        func: Box::new(MaxFunc::new()),
        definition: "MAX(number1, [number2, ...])".to_string(),
        summary: Strings::FunctionMaxSummary.to_string(),
        example: Strings::FunctionMaxExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MIN".to_string(),
      Function {
        name: "MIN".to_string(),
        func: Box::new(MinFunc::new()),
        definition: "MIN(number1, [number2, ...])".to_string(),
        summary: Strings::FunctionMinSummary.to_string(),
        example: Strings::FunctionMinExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ROUND".to_string(),
      Function {
        name: "ROUND".to_string(),
        func: Box::new(RoundFunc::new()),
        definition: "ROUND(value, [precision])".to_string(),
        summary: Strings::FunctionRoundSummary.to_string(),
        example: Strings::FunctionRoundExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ROUNDUP".to_string(),
      Function {
        name: "ROUNDUP".to_string(),
        func: Box::new(RoundUpFunc::new()),
        definition: "ROUNDUP(value, [precision])".to_string(),
        summary: Strings::FunctionRoundupSummary.to_string(),
        example: Strings::FunctionRoundupExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ROUNDDOWN".to_string(),
      Function {
        name: "ROUNDDOWN".to_string(),
        func: Box::new(RoundDownFunc::new()),
        definition: "ROUNDDOWN(value, [precision])".to_string(),
        summary: Strings::FunctionRounddownSummary.to_string(),
        example: Strings::FunctionRounddownExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "CEILING".to_string(),
      Function {
        name: "CEILING".to_string(),
        func: Box::new(CeilingFunc::new()),
        definition: "CEILING(value, [significance])".to_string(),
        summary: Strings::FunctionCeilingSummary.to_string(),
        example: Strings::FunctionCeilingExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "FLOOR".to_string(),
      Function {
        name: "FLOOR".to_string(),
        func: Box::new(FloorFunc::new()),
        definition: "FLOOR(value, [significance])".to_string(),
        summary: Strings::FunctionFloorSummary.to_string(),
        example: Strings::FunctionFloorExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "EVEN".to_string(),
      Function {
        name: "EVEN".to_string(),
        func: Box::new(EvenFunc::new()),
        definition: "EVEN(value)".to_string(),
        summary: Strings::FunctionEvenSummary.to_string(),
        example: Strings::FunctionEvenExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ODD".to_string(),
      Function {
        name: "ODD".to_string(),
        func: Box::new(OddFunc::new()),
        definition: "ODD(value)".to_string(),
        summary: Strings::FunctionOddSummary.to_string(),
        example: Strings::FunctionOddExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "INT".to_string(),
      Function {
        name: "INT".to_string(),
        func: Box::new(IntFunc::new()),
        definition: "INT(value)".to_string(),
        summary: Strings::FunctionIntSummary.to_string(),
        example: Strings::FunctionIntExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ABS".to_string(),
      Function {
        name: "ABS".to_string(),
        func: Box::new(AbsFunc::new()),
        definition: "ABS(value)".to_string(),
        summary: Strings::FunctionAbsSummary.to_string(),
        example: Strings::FunctionAbsExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "SQRT".to_string(),
      Function {
        name: "SQRT".to_string(),
        func: Box::new(SqrtFunc::new()),
        definition: "SQRT(value)".to_string(),
        summary: Strings::FunctionSqrtSummary.to_string(),
        example: Strings::FunctionSqrtExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MOD".to_string(),
      Function {
        name: "MOD".to_string(),
        func: Box::new(ModFunc::new()),
        definition: "MOD(value, divisor)".to_string(),
        summary: Strings::FunctionModSummary.to_string(),
        example: Strings::FunctionModExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "POWER".to_string(),
      Function {
        name: "POWER".to_string(),
        func: Box::new(PowerFunc::new()),
        definition: "POWER(base, power)".to_string(),
        summary: Strings::FunctionPowerSummary.to_string(),
        example: Strings::FunctionPowerExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "EXP".to_string(),
      Function {
        name: "EXP".to_string(),
        func: Box::new(ExpFunc::new()),
        definition: "EXP(power)".to_string(),
        summary: Strings::FunctionExpSummary.to_string(),
        example: Strings::FunctionExpExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "LOG".to_string(),
      Function {
        name: "LOG".to_string(),
        func: Box::new(LogFunc::new()),
        definition: "LOG(number, base=10)".to_string(),
        summary: Strings::FunctionLogSummary.to_string(),
        example: Strings::FunctionLogExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "VALUE".to_string(),
      Function {
        name: "VALUE".to_string(),
        func: Box::new(ValueFunc::new()),
        definition: "VALUE(text)".to_string(),
        summary: Strings::FunctionValueSummary.to_string(),
        example: Strings::FunctionValueExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "FIND".to_string(),
      Function {
        name: "FIND".to_string(),
        func: Box::new(FindFunc::new()),
        definition: "FIND(stringToFind, whereToSearch, [startFromPosition])".to_string(),
        summary: Strings::FunctionFindSummary.to_string(),
        example: Strings::FunctionFindExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "SEARCH".to_string(),
      Function {
        name: "SEARCH".to_string(),
        func: Box::new(SearchFunc::new()),
        definition: "SEARCH(stringToFind, whereToSearch, [startFromPosition])".to_string(),
        summary: Strings::FunctionSearchSummary.to_string(),
        example: Strings::FunctionSearchExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MID".to_string(),
      Function {
        name: "MID".to_string(),
        func: Box::new(MidFunc::new()),
        definition: "MID(string, whereToStart, count)".to_string(),
        summary: Strings::FunctionMidSummary.to_string(),
        example: Strings::FunctionMidExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "REPLACE".to_string(),
      Function {
        name: "REPLACE".to_string(),
        func: Box::new(ReplaceFunc::new()),
        definition: "REPLACE(string, whereToStart, count, replacement)".to_string(),
        summary: Strings::FunctionReplaceSummary.to_string(),
        example: Strings::FunctionReplaceExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "SUBSTITUTE".to_string(),
      Function {
        name: "SUBSTITUTE".to_string(),
        func: Box::new(SubstituteFunc::new()),
        definition: "SUBSTITUTE(string, oldText, newText, [index])".to_string(),
        summary: Strings::FunctionSubstituteSummary.to_string(),
        example: Strings::FunctionSubstituteExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "CONCATENATE".to_string(),
      Function {
        name: "CONCATENATE".to_string(),
        func: Box::new(ConcatenateFunc::new()),
        definition: "CONCATENATE(text, [text2, ...])".to_string(),
        summary: Strings::FunctionConcatenateSummary.to_string(),
        example: Strings::FunctionConcatenateExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "LEN".to_string(),
      Function {
        name: "LEN".to_string(),
        func: Box::new(LenFunc::new()),
        definition: "LEN(string)".to_string(),
        summary: Strings::FunctionLenSummary.to_string(),
        example: Strings::FunctionLenExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "LEFT".to_string(),
      Function {
        name: "LEFT".to_string(),
        func: Box::new(LeftFunc::new()),
        definition: "LEFT(string, howMany)".to_string(),
        summary: Strings::FunctionLeftSummary.to_string(),
        example: Strings::FunctionLeftExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "RIGHT".to_string(),
      Function {
        name: "RIGHT".to_string(),
        func: Box::new(RightFunc::new()),
        definition: "RIGHT(string, howMany)".to_string(),
        summary: Strings::FunctionRightSummary.to_string(),
        example: Strings::FunctionRightExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "REPT".to_string(),
      Function {
        name: "REPT".to_string(),
        func: Box::new(ReptFunc::new()),
        definition: "REPT(string, number)".to_string(),
        summary: Strings::FunctionReptSummary.to_string(),
        example: Strings::FunctionReptExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "IF".to_string(),
      Function {
        name: "IF".to_string(),
        func: Box::new(IfFunc::new()),
        definition: "IF(logical, value1, value2)".to_string(),
        summary: Strings::FunctionIfSummary.to_string(),
        example: Strings::FunctionIfExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "SWITCH".to_string(),
      Function {
        name: "SWITCH".to_string(),
        func: Box::new(SwitchFunc::new()),
        definition: "SWITCH(expression, [pattern, result...], [default])".to_string(),
        summary: Strings::FunctionSwitchSummary.to_string(),
        example: Strings::FunctionSwitchExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "TRUE".to_string(),
      Function {
        name: "TRUE".to_string(),
        func: Box::new(TrueFunc::new()),
        definition: "TRUE()".to_string(),
        summary: Strings::FunctionTrueSummary.to_string(),
        example: Strings::FunctionTrueExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "FALSE".to_string(),
      Function {
        name: "FALSE".to_string(),
        func: Box::new(FalseFunc::new()),
        definition: "FALSE()".to_string(),
        summary: Strings::FunctionFalseSummary.to_string(),
        example: Strings::FunctionFalseExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "AND".to_string(),
      Function {
        name: "AND".to_string(),
        func: Box::new(AndFunc::new()),
        definition: "AND(logical1, [logical2, ...])".to_string(),
        summary: Strings::FunctionAndSummary.to_string(),
        example: Strings::FunctionAndExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "OR".to_string(),
      Function {
        name: "OR".to_string(),
        func: Box::new(OrFunc::new()),
        definition: "OR(logical1, [logical2, ...])".to_string(),
        summary: Strings::FunctionOrSummary.to_string(),
        example: Strings::FunctionOrExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "XOR".to_string(),
      Function {
        name: "XOR".to_string(),
        func: Box::new(XorFunc::new()),
        definition: "XOR(logical1, [logical2, ...])".to_string(),
        summary: Strings::FunctionXorSummary.to_string(),
        example: Strings::FunctionXorExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "NOT".to_string(),
      Function {
        name: "NOT".to_string(),
        func: Box::new(NotFunc::new()),
        definition: "NOT(boolean)".to_string(),
        summary: Strings::FunctionNotSummary.to_string(),
        example: Strings::FunctionNotExample.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "BLANK".to_string(),
      Function {
        name: "BLANK".to_string(),
        func: Box::new(BlankFunc::new()),
        definition: "BLANK()".to_string(),
        summary: Strings::FunctionBlankExample.to_string(),
        example: Strings::FunctionBlankSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ERROR".to_string(),
      Function {
        name: "ERROR".to_string(),
        func: Box::new(ErrorFunc::new()),
        definition: "ERROR(message)".to_string(),
        summary: Strings::FunctionErrorExample.to_string(),
        example: Strings::FunctionErrorSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "IS_ERROR".to_string(),
      Function {
        name: "IS_ERROR".to_string(),
        func: Box::new(IsErrorFunc::new()),
        definition: "IS_ERROR(expr)".to_string(),
        summary: Strings::FunctionIsErrorExample.to_string(),
        example: Strings::FunctionIsErrorSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "ISERROR".to_string(),
      Function {
        name: "ISERROR".to_string(),
        func: Box::new(IsErrorFunc::new()),
        definition: "ISERROR(expr)".to_string(),
        summary: Strings::FunctionIsErrorExample.to_string(),
        example: Strings::FunctionIsErrorSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "YEAR".to_string(),
      Function {
        name: "YEAR".to_string(),
        func: Box::new(YearFunc::new()),
        definition: "YEAR(date)".to_string(),
        summary: Strings::FunctionYearExample.to_string(),
        example: Strings::FunctionYearSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MONTH".to_string(),
      Function {
        name: "MONTH".to_string(),
        func: Box::new(MonthFunc::new()),
        definition: "MONTH(date)".to_string(),
        summary: Strings::FunctionMonthExample.to_string(),
        example: Strings::FunctionMonthSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "WEEKDAY".to_string(),
      Function {
        name: "WEEKDAY".to_string(),
        func: Box::new(WeekdayFunc::new()),
        definition: "WEEKDAY(date, [startDayOfWeek])".to_string(),
        summary: Strings::FunctionWeekdayExample.to_string(),
        example: Strings::FunctionWeekdaySummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "WEEKNUM".to_string(),
      Function {
        name: "WEEKNUM".to_string(),
        func: Box::new(WeekNumFunc::new()),
        definition: "WEEKNUM(date, [startDayOfWeek])".to_string(),
        summary: Strings::FunctionWeekNumExample.to_string(),
        example: Strings::FunctionWeekNumSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "DAY".to_string(),
      Function {
        name: "DAY".to_string(),
        func: Box::new(DayFunc::new()),
        definition: "DAY(date)".to_string(),
        summary: Strings::FunctionDayExample.to_string(),
        example: Strings::FunctionDaySummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "HOUR".to_string(),
      Function {
        name: "HOUR".to_string(),
        func: Box::new(HourFunc::new()),
        definition: "HOUR(date)".to_string(),
        summary: Strings::FunctionHourExample.to_string(),
        example: Strings::FunctionHourSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "MINUTE".to_string(),
      Function {
        name: "MINUTE".to_string(),
        func: Box::new(MinuteFunc::new()),
        definition: "MINUTE(date)".to_string(),
        summary: Strings::FunctionMinuteExample.to_string(),
        example: Strings::FunctionMinuteSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "SECOND".to_string(),
      Function {
        name: "SECOND".to_string(),
        func: Box::new(SecondFunc::new()),
        definition: "SECOND(date)".to_string(),
        summary: Strings::FunctionSecondExample.to_string(),
        example: Strings::FunctionSecondSummary.to_string(),
        link_url: None,
      },
    );
    functions.insert(
      "RECORD_ID".to_string(),
      Function {
        name: "RECORD_ID".to_string(),
        func: Box::new(RecordIdFunc::new()),
        definition: "RECORD_ID()".to_string(),
        summary: Strings::FunctionRecordIdSummary.to_string(),
        example: Strings::FunctionRecordIdExample.to_string(),
        link_url: None,
      },
    );
    Self { functions }
  }

  pub fn get_function(&self, name: &str) -> Option<&Function> {
    self.functions.get(name.to_ascii_uppercase().as_str())
  }
}
