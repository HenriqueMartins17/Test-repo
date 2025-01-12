use chrono::{DateTime, Datelike, Local, Months, NaiveDateTime, Timelike};
use chrono_tz::Tz;
use databus_shared::env_var;

pub fn system_tz() -> Tz {
  return env_var!(TZ default "UTC").parse().unwrap();
}

pub fn get_prev_monthly_start_timestamp(interval: u32) -> i64 {
  let date_time = Local::now().checked_sub_months(Months::new(interval));
  if date_time.is_some() {
    // each month starts with 1
    return get_day_start_timestamp(date_time.unwrap());
  }
  return Local::now().with_timezone(&system_tz()).timestamp();
}

pub fn get_next_monthly_start_timestamp(interval: u32) -> i64 {
  let date_time = Local::now().checked_add_months(Months::new(interval));
  if date_time.is_some() {
    // each month starts with 1
    return get_day_start_timestamp(date_time.unwrap());
  }
  return Local::now().with_timezone(&system_tz()).timestamp();
}

pub fn get_monthly_start_format(interval: u32) -> String {
  let timestamp = get_prev_monthly_start_timestamp(interval);
  NaiveDateTime::from_timestamp_opt(timestamp, 0)
    .unwrap()
    .and_local_timezone(system_tz().clone())
    .unwrap()
    .format("%Y-%m-%d %H:%M:%S")
    .to_string()
}

pub fn get_monthly_end_format(interval: u32) -> String {
  let timestamp = get_next_monthly_start_timestamp(interval);
  NaiveDateTime::from_timestamp_opt(timestamp, 0)
    .unwrap()
    .and_local_timezone(system_tz().clone())
    .unwrap()
    .format("%Y-%m-%d %H:%M:%S")
    .to_string()
}

fn get_day_start_timestamp(date_time: DateTime<Local>) -> i64 {
  return date_time
    .with_timezone(&system_tz())
    .with_day(1)
    .unwrap()
    .with_hour(0)
    .unwrap()
    .with_minute(0)
    .unwrap()
    .with_second(0)
    .unwrap()
    .timestamp();
}

#[cfg(test)]
mod test {
  use crate::util::{
    get_monthly_end_format, get_monthly_start_format, get_next_monthly_start_timestamp,
    get_prev_monthly_start_timestamp,
  };

  #[test]
  fn test_get() {
    println!("{}", get_prev_monthly_start_timestamp(1));
    println!("{}", get_next_monthly_start_timestamp(1));
    println!("{}", get_monthly_start_format(1));
    println!("{}", get_monthly_end_format(1));
  }

  #[test]
  fn test_get_with_12() {
    println!("{}", get_prev_monthly_start_timestamp(12));
    println!("{}", get_next_monthly_start_timestamp(12));
    println!("{}", get_monthly_start_format(12));
    println!("{}", get_monthly_end_format(12));
  }

  #[test]
  fn test_get_with_15() {
    println!("{}", get_prev_monthly_start_timestamp(15));
    println!("{}", get_next_monthly_start_timestamp(15));
    println!("{}", get_monthly_start_format(15));
    println!("{}", get_monthly_end_format(15));
  }

  #[test]
  fn test_get_with_0() {
    println!("{}", get_prev_monthly_start_timestamp(0));
    println!("{}", get_next_monthly_start_timestamp(0));
    println!("{}", get_monthly_start_format(0));
    println!("{}", get_monthly_end_format(0));
  }
}
