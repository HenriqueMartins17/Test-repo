pub fn covert_dayjs_format_to_chrono_format(format: &str) -> String {
  format
    .replace("YYYY", "%Y")
    .replace("MM", "%m")
    .replace("DD", "%d")
    .replace("HH", "%H")
    .replace("mm", "%M")
    .replace("ss", "%S")
}
