use std::ops::Add;
use std::rc::Rc;
use std::str::FromStr;

use chrono::{DateTime, Datelike, Duration, LocalResult, TimeZone, Timelike, Utc};
use chrono_tz::Tz;

use databus_shared::env_var;

use crate::config::timezones::covert_dayjs_format_to_chrono_format;
use crate::fields::property::{DateFormat, DateTimeFieldPropertySO, TimeFormat};
use crate::prelude::view_operation::filter::IFilterDateTime;
use crate::prelude::{DatasheetPackContext, FieldSO};
use crate::so::view_operation::filter::{ConditionValue, FOperator, FilterDuration};
use crate::so::CellValue;

const DEFAULT_TIME_ZONE: Tz = Tz::Asia__Shanghai;

const DEFAULT_DATETIME_PROPS: DateTimeFieldPropertySO = DateTimeFieldPropertySO {
  date_format: DateFormat::SYyyyMmDd,
  time_format: TimeFormat::HHmm,
  include_time: false,
  auto_fill: false,
  time_zone: None,
  include_time_zone: None,
};

pub fn get_user_time_zone(state: &DatasheetPackContext) -> &Option<String> {
  &state.user_info.time_zone
}

pub fn datetime_format(
  timestamp: &CellValue,
  props: &Option<DateTimeFieldPropertySO>,
  _user_time_zone: &Option<String>,
) -> Option<String> {
  if timestamp.is_null() {
    return None;
  }

  let props = props.as_ref().unwrap_or(&DEFAULT_DATETIME_PROPS);
  let date_format = props.date_format.get_format();
  let time_format = props.time_format.get_format();
  let mut format = date_format.to_string();
  if props.include_time {
    format = format.add(" ").add(time_format);
  }

  let time_zone = DEFAULT_TIME_ZONE;

  let s = if let LocalResult::Single(datetime) = time_zone.timestamp_millis_opt(timestamp.to_number() as i64) {
    let datetime = datetime.with_timezone(&time_zone);
    let format = covert_dayjs_format_to_chrono_format(&format);
    datetime.format(&format).to_string()
  } else {
    "Invalid Date".to_string()
  };

  Some(s)
}

pub struct DateTimeBaseField {
  field_conf: FieldSO,
  state: Rc<DatasheetPackContext>,
}

impl DateTimeBaseField {
  pub fn new(field_conf: FieldSO, state: Rc<DatasheetPackContext>) -> Self {
    Self { field_conf, state }
  }

  pub fn is_meet_filter(operator: &FOperator, cell_value: &CellValue, condition_value: &ConditionValue) -> bool {
    // timeZone and locale are optional

    let tz: String = env_var!(TZ default "Asia/Shanghai");
    Self::_is_meet_filter(operator, cell_value, condition_value, Some(tz.as_str()))
  }

  pub fn cell_value_to_string(&self, cell_value: CellValue) -> Option<String> {
    let props = self.field_conf.property.clone().unwrap().to_date_time_field_property();
    let user_time_zone = get_user_time_zone(&self.state);
    datetime_format(&cell_value, &Some(props), user_time_zone)
  }

  pub fn _is_meet_filter(
    operator: &FOperator,
    cell_value: &CellValue,
    condition_value: &ConditionValue,
    tz: Option<&str>,
  ) -> bool {
    // The logic to judge in advance that it is empty or not.
    return match operator {
      FOperator::IsEmpty => cell_value.is_null(),
      FOperator::IsNotEmpty => !cell_value.is_null(),
      _ => {
        let date_time = condition_value.as_date_time().unwrap();

        let mut timestamp: (Option<String>, Option<i64>) = (None, None);
        let mut outer_filter_duration: FilterDuration;

        match date_time {
          IFilterDateTime::Single(filter_duration) => {
            outer_filter_duration = filter_duration[0].clone();
          }
          IFilterDateTime::ExactDateOption(filter_duration, exact_date) => {
            if exact_date.is_none() {
              return true;
            }
            timestamp.1 = exact_date;
            outer_filter_duration = filter_duration;
          }
          IFilterDateTime::DateRangeOption(filter_duration, range_date) => {
            if range_date.is_none() {
              return true;
            }
            timestamp.0 = range_date;
            outer_filter_duration = filter_duration;
          }
        }
        if cell_value.is_null() {
          return false;
        }

        let (left, right) = Self::get_time_range(outer_filter_duration, timestamp, tz);
        let cell_value = cell_value.as_i64().unwrap();

        match operator {
          FOperator::Is => (left <= cell_value) && (cell_value < right),
          FOperator::IsGreater => cell_value > right,
          FOperator::IsGreaterEqual => cell_value >= left,
          FOperator::IsLess => cell_value < left,
          FOperator::IsLessEqual => cell_value <= right,
          _ => false,
        }
      }
    };
  }

  pub fn get_time_range(
    filter_duration: FilterDuration,
    time: (Option<String>, Option<i64>),
    time_zone: Option<&str>,
  ) -> (i64, i64) {
    let now: DateTime<Tz> = Self::with_time_zone(Utc::now().timestamp_millis(), time_zone);
    match filter_duration {
      FilterDuration::ExactDate => {
        if let Some(timestamp) = time.1 {
          let time = Self::with_time_zone(timestamp, time_zone);
          (
            Self::start_of_day(time).timestamp_millis(),
            Self::end_of_day(time).timestamp_millis(),
          )
        } else {
          panic!("ExactDate has to calculate with timestamp");
        }
      }
      FilterDuration::DateRange => {
        if let Some(time_str) = time.0 {
          let (start_time, end_time) = time_str.split_once('-').unwrap_or_default();
          (start_time.parse::<i64>().unwrap(), end_time.parse::<i64>().unwrap_or(0))
        } else {
          panic!("DateRange has to calculate with timestamp");
        }
      }
      FilterDuration::Today => {
        let time = now;
        (
          Self::start_of_day(time).timestamp_millis(),
          Self::end_of_day(time).timestamp_millis(),
        )
      }
      FilterDuration::Tomorrow => {
        let time = now;
        let time = time + Duration::days(1);
        (
          Self::start_of_day(time).timestamp_millis(),
          Self::end_of_day(time).timestamp_millis(),
        )
      }
      FilterDuration::Yesterday => {
        let time = now + Duration::days(-1);
        (
          Self::start_of_day(time).timestamp_millis(),
          Self::end_of_day(time).timestamp_millis(),
        )
      }
      FilterDuration::TheNextWeek => {
        let start_of_day = now + Duration::days(1);
        let end_of_day = now + Duration::days(7);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::TheLastWeek => {
        let start_of_day = now + Duration::days(-7);
        let end_of_day = now + Duration::days(-1);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::TheNextMonth => {
        let start_of_day = now + Duration::days(1);
        let end_of_day = now + Duration::days(30);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::TheLastMonth => {
        let start_of_day = now + Duration::days(-30);
        let end_of_day = now + Duration::days(-1);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::ThisWeek => {
        let start_of_day = now - Duration::days(now.weekday().num_days_from_monday() as i64);
        let end_of_day = now + Duration::days((6 - now.weekday().num_days_from_monday()) as i64);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::PreviousWeek => {
        let days_until_start_of_week = now.weekday().num_days_from_monday() as i64;
        // 上周的开始日期
        let start_of_day = now - Duration::days(days_until_start_of_week + 7);
        let end_of_day = start_of_day + Duration::days(6);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::ThisMonth => {
        // 当前月的开始日期
        let start_of_day = now.with_day(1).unwrap();
        // 当前月的结束日期
        let end_of_day = now.with_month(now.month() + 1).unwrap().with_day(1).unwrap();
        -Duration::days(1);

        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::PreviousMonth => {
        let start_of_day = now.with_month(now.month() - 1).unwrap().with_day(1).unwrap();

        let end_of_day = now.with_day(1).unwrap() - Duration::days(1);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::ThisYear => {
        let start_of_day = now.with_month(1).unwrap().with_day(1).unwrap();
        let end_of_day = start_of_day.with_year(start_of_day.year() + 1).unwrap() - Duration::days(1);
        (
          Self::start_of_day(start_of_day).timestamp_millis(),
          Self::end_of_day(end_of_day).timestamp_millis(),
        )
      }
      FilterDuration::SomeDayBefore => {
        if let Some(days_before) = time.1 {
          let time = now - Duration::days(days_before);
          (
            Self::start_of_day(time).timestamp_millis(),
            Self::end_of_day(time).timestamp_millis(),
          )
        } else {
          panic!("SomeDayBefore has to calculate with number");
        }
      }
      FilterDuration::SomeDayAfter => {
        if let Some(days_after) = time.1 {
          let time = now + Duration::days(days_after);
          (
            Self::start_of_day(time).timestamp_millis(),
            Self::end_of_day(time).timestamp_millis(),
          )
        } else {
          panic!("SomeDayAfter has to calculate with number");
        }
      }
    }
  }

  // 获取带有时区和区域设置的日期时间
  fn with_time_zone(timestamp: i64, time_zone: Option<&str>) -> DateTime<Tz> {
    let time_zone = time_zone.unwrap_or("UTC");
    let tz = Tz::from_str(time_zone).unwrap();
    let naive_utc = Utc.timestamp_millis_opt(timestamp).unwrap().naive_utc();
    let zoned_date = tz.from_utc_datetime(&naive_utc);

    return zoned_date;
  }

  fn start_of_day(date_time: DateTime<Tz>) -> DateTime<Tz> {
    date_time
      .with_hour(0)
      .expect("Failed to set hour to 0")
      .with_minute(0)
      .expect("Failed to set minute to 0")
      .with_second(0)
      .expect("Failed to set second to 0")
      .with_nanosecond(0)
      .expect("Failed to set nanosecond to 0")
  }

  fn end_of_day(date_time: DateTime<Tz>) -> DateTime<Tz> {
    date_time
      .with_hour(23)
      .expect("Failed to set hour to 23")
      .with_minute(59)
      .expect("Failed to set minute to 59")
      .with_second(59)
      .expect("Failed to set second to 59")
      .with_nanosecond(999_999_999)
      .expect("Failed to set nanosecond to 999_999_999")
  }
}

#[cfg(test)]
mod test {
  use crate::prelude::view_operation::filter::{ConditionValue, FOperator, FilterDuration, IFilterDateTime};
  use crate::prelude::CellValue;
  use chrono::{DateTime, Local, TimeZone, Utc};

  #[test]
  pub fn test_date() {
    let utc: DateTime<Utc> = Utc::now();
    let local: DateTime<Local> = Local::now();
    println!("{}", utc);
    println!("{}", local);
    println!("{}", Utc.timestamp_millis_opt(-1700409600000_i64).unwrap());
    println!("{}", Local.timestamp_millis_opt(1700409600000_i64).unwrap());
  }

  #[test]
  pub fn test_is_meet_filter() {
    let timestamp_millis = Utc::now().timestamp_millis();
    let cell_value = CellValue::from(timestamp_millis as f64);
    let single_today = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::Today]));
    let single_yesterday = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::Yesterday]));
    let single_tomorrow = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::Tomorrow]));
    let single_this_week = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::ThisWeek]));
    let single_next_week = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::TheNextWeek]));
    let single_last_week = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::TheLastWeek]));
    let single_this_month = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::ThisMonth]));
    let single_last_month = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::TheLastMonth]));
    let single_next_month = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::TheNextMonth]));
    let single_this_year = ConditionValue::DateTime(IFilterDateTime::Single(vec![FilterDuration::ThisYear]));
    let exact_date = ConditionValue::DateTime(IFilterDateTime::ExactDateOption(
      FilterDuration::ExactDate,
      Some(timestamp_millis),
    ));
    let exact_date = ConditionValue::DateTime(IFilterDateTime::DateRangeOption(
      FilterDuration::DateRange,
      Some(format!(
        "{}-{}",
        timestamp_millis.to_string(),
        timestamp_millis.to_string()
      )),
    ));
    let result = super::DateTimeBaseField::is_meet_filter(&FOperator::Is, &cell_value, &single_today);
    assert_eq!(result, true);

    let result = super::DateTimeBaseField::is_meet_filter(&FOperator::IsGreater, &cell_value, &single_today);
    assert_eq!(result, false);

    let result = super::DateTimeBaseField::is_meet_filter(&FOperator::IsGreaterEqual, &cell_value, &single_today);
    assert_eq!(result, true);

    let result = super::DateTimeBaseField::is_meet_filter(&FOperator::IsLess, &cell_value, &single_today);
    assert_eq!(result, false);

    let result = super::DateTimeBaseField::is_meet_filter(&FOperator::IsLessEqual, &cell_value, &single_today);
    assert_eq!(result, true);
  }
}
