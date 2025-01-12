use std::fmt::Debug;

/// Creates a `Vec<(String, String)>` using key-value pairs.
///
/// # Examples
///
/// ```
/// use databus_core::params_i18n;
///
/// let d = "4";
/// let vec = params_i18n!(a = true, b = 2, c = "3", d = d);
/// ```
#[macro_export]
macro_rules! params_i18n {
    ($($key:ident = $value:expr),* $(,)?) => {{
        let mut vec = vec![];
        $(
            vec.push( ( stringify!($key).to_string(), $value.to_string() ) );
        )*
        vec
    }};
}

/// i18n strings
#[derive(Debug)]
pub enum Strings {
  UnexpectedError,

  FunctionErrEndOfRightBracket,
  FunctionErrInvalidFieldName,
  FunctionErrNoLeftBracket,
  FunctionErrNoRefSelfColumn,
  FunctionErrNotFoundFunctionNameAs,
  FunctionErrUnableParseChar,
  FunctionErrUnrecognizedChar,
  FunctionErrUnrecognizedOperator,
  FunctionErrUnknownFunction,
  FunctionErrUnknownOperator,
  FunctionErrWrongFunctionSuffix,

  FunctionContentEmpty,

  FunctionSumExample,
  FunctionSumSummary,
  FunctionAverageSummary,
  FunctionAverageExample,
  FunctionMaxExample,
  FunctionMaxSummary,
  FunctionMinExample,
  FunctionMinSummary,
  FunctionRoundExample,
  FunctionRoundSummary,
  FunctionRoundupExample,
  FunctionRoundupSummary,
  FunctionRounddownExample,
  FunctionRounddownSummary,
  FunctionCeilingExample,
  FunctionCeilingSummary,
  FunctionFloorExample,
  FunctionFloorSummary,
  FunctionEvenSummary,
  FunctionEvenExample,
  FunctionOddSummary,
  FunctionOddExample,
  FunctionIntExample,
  FunctionIntSummary,
  FunctionAbsSummary,
  FunctionAbsExample,
  FunctionSqrtSummary,
  FunctionSqrtExample,
  FunctionModSummary,
  FunctionModExample,
  FunctionPowerSummary,
  FunctionPowerExample,
  FunctionExpSummary,
  FunctionExpExample,
  FunctionLogSummary,
  FunctionLogExample,
  FunctionValueExample,
  FunctionValueSummary,
  FunctionFindSummary,
  FunctionFindExample,
  FunctionSearchSummary,
  FunctionSearchExample,
  FunctionMidSummary,
  FunctionMidExample,
  FunctionReplaceSummary,
  FunctionReplaceExample,
  FunctionSubstituteSummary,
  FunctionSubstituteExample,
  FunctionConcatenateSummary,
  FunctionConcatenateExample,
  FunctionLenExample,
  FunctionLenSummary,
  FunctionLeftSummary,
  FunctionLeftExample,
  FunctionRightSummary,
  FunctionRightExample,
  FunctionReptSummary,
  FunctionReptExample,
  FunctionTrueExample,
  FunctionTrueSummary,
  FunctionFalseSummary,
  FunctionFalseExample,
  FunctionAndSummary,
  FunctionAndExample,
  FunctionOrSummary,
  FunctionOrExample,
  FunctionXorSummary,
  FunctionXorExample,
  FunctionNotSummary,
  FunctionNotExample,
  FunctionIfExample,
  FunctionIfSummary,
  FunctionSwitchSummary,
  FunctionSwitchExample,
  FunctionBlankExample,
  FunctionBlankSummary,
  FunctionErrorExample,
  FunctionErrorSummary,
  FunctionIsErrorExample,
  FunctionIsErrorSummary,
  FunctionYearExample,
  FunctionYearSummary,
  FunctionMonthExample,
  FunctionMonthSummary,
  FunctionWeekdayExample,
  FunctionWeekdaySummary,
  FunctionWeekNumExample,
  FunctionWeekNumSummary,
  FunctionDayExample,
  FunctionDaySummary,
  FunctionHourExample,
  FunctionHourSummary,
  FunctionMinuteExample,
  FunctionMinuteSummary,
  FunctionSecondExample,
  FunctionSecondSummary,
  FunctionRecordIdExample,
  FunctionRecordIdSummary,

  ParamsCountError,
  ParamsErrorTypeNotEquals,
  ViewFieldSearchNotFoundTip,
}

impl Strings {
  /// # Examples
  ///
  /// ```
  /// use databus_core::formula::i18n::Strings;
  /// use databus_core::params_i18n;
  ///
  /// let s = Strings::FunctionErrUnrecognizedChar.with_params(params_i18n!(token = "token"));
  /// ```
  pub fn with_params(self, params: Vec<(String, String)>) -> I18nMessage {
    I18nMessage { message: self, params }
  }
}

impl ToString for Strings {
  fn to_string(&self) -> String {
    format!("{:?}", self)
  }
}

pub struct I18nMessage {
  message: Strings,
  params: Vec<(String, String)>,
}

impl ToString for I18nMessage {
  fn to_string(&self) -> String {
    format!("{}: {:?}", self.message.to_string(), self.params)
  }
}

#[cfg(test)]
mod tests {
  use super::*;

  fn t(message: impl ToString) -> String {
    message.to_string()
  }

  #[test]
  fn test_normal() {
    assert_eq!("FunctionErrUnrecognizedChar", t(Strings::FunctionErrUnrecognizedChar));
  }

  #[test]
  fn test_params() {
    let d = "4".to_string();
    let params = params_i18n!(a = true, b = 2, c = "3", d = d);

    assert_eq!(
      vec![
        ("a".to_string(), true.to_string()),
        ("b".to_string(), 2.to_string()),
        ("c".to_string(), "3".to_string()),
        ("d".to_string(), d.to_string()),
      ],
      params
    );
  }

  #[test]
  fn test_with_params() {
    println!("{}", format!("{name}", name = "hello"));
    let d = "4";
    assert_eq!(
      "FunctionErrUnrecognizedChar: [(\"a\", \"true\"), (\"b\", \"2\"), (\"c\", \"3\"), (\"d\", \"4\")]",
      t(Strings::FunctionErrUnrecognizedChar.with_params(params_i18n!(a = true, b = 2, c = "3", d = d,)))
    );
  }
}
