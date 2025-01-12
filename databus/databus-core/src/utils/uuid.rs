use crate::utils::utils::generate_random_string;
use std::collections::HashMap;
use std::fmt;

const EFFECTIVE_ID_LENGTH: usize = 10;

#[derive(Debug, Clone, Copy)]
pub enum IDPrefix {
  Table,
  View,
  Record,
  Field,
  Option,
  Condition,
  File,
  Comment,
  WidgetPanel,
  Editor,
  SPACE,
  DateTimeAlarm,
  EmbedLink,
  Form,
  Dashboard,
  AutomationAction,
  AutomationTrigger,
  DocumentName,
  AutomationRobot,
}

impl fmt::Display for IDPrefix {
  fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
    match self {
      IDPrefix::Table => write!(f, "dst"),
      IDPrefix::View => write!(f, "viw"),
      IDPrefix::Record => write!(f, "rec"),
      IDPrefix::Field => write!(f, "fld"),
      IDPrefix::Option => write!(f, "opt"),
      IDPrefix::Condition => write!(f, "cdt"),
      // uploaded attachments
      IDPrefix::File => write!(f, "atc"),
      IDPrefix::Comment => write!(f, "cmt"),
      IDPrefix::WidgetPanel => write!(f, "wpl"),
      IDPrefix::Editor => write!(f, "edt"),
      IDPrefix::SPACE => write!(f, "spc"),
      IDPrefix::DateTimeAlarm => write!(f, "dta"),
      IDPrefix::EmbedLink => write!(f, "emb"),
      IDPrefix::Form => write!(f, "fom"),
      IDPrefix::Dashboard => write!(f, "dsb"),
      IDPrefix::AutomationAction => write!(f, "aac"),
      IDPrefix::AutomationTrigger => write!(f, "atr"),
      IDPrefix::DocumentName => write!(f, "doc"),
      IDPrefix::AutomationRobot => write!(f, "aac"),
    }
  }
}

/**
 * Generate unique new id
 * @param prefix new id prefix
 * @param ids Existing ids, will not be repeated with this group
 * @returns string
 */
pub fn get_new_id(prefix: IDPrefix, ids: Vec<String>) -> String {
  get_new_ids(prefix, 1, ids)[0].clone()
}

/**
* Generate a new set of unique ids
* @param prefix new id prefix
* @param num the expected number
* @param ids Existing ids, will not be repeated with this group
* @returns Vec<String>
*/
pub fn get_new_ids(prefix: IDPrefix, num: usize, ids: Vec<String>) -> Vec<String> {
  if num <= 0 {
    return Vec::new();
  }

  let mut new_ids: Vec<String> = Vec::new();
  let mut id_map: HashMap<String, bool> = ids.into_iter().map(|id| (id, true)).collect();

  for _ in 0..num {
    let mut new_id: String;
    loop {
      new_id = format!("{}{}", prefix, generate_random_string(EFFECTIVE_ID_LENGTH));
      if !id_map.contains_key(&new_id) {
        break;
      }
    }
    new_ids.push(new_id.clone());
    id_map.insert(new_id, true);
  }
  new_ids
}

pub struct IdUtil;

impl IdUtil {
  pub fn create_automation_trigger_id() -> String {
    let mut id_prefix = IDPrefix::AutomationTrigger.to_string();
    id_prefix.push_str(generate_random_string(15).as_str());
    id_prefix
  }

  pub fn create_automation_action_id() -> String {
    let mut id_prefix = IDPrefix::AutomationAction.to_string();
    id_prefix.push_str(generate_random_string(15).as_str());
    id_prefix
  }

  pub fn create_document_name() -> String {
    format!("{}{}", IDPrefix::DocumentName.to_string(), generate_random_string(15))
  }

  pub fn create_automation_robot_id() -> String {
    let mut id_prefix = IDPrefix::AutomationRobot.to_string();
    id_prefix.push_str(generate_random_string(15).as_str());
    id_prefix
  }
}

/**
 * Get safe and unique names
 * @param newName the name you want to take
 * @param names An array of existing names, if there are duplicates, suffixes will be added automatically
 */
pub fn get_uniq_name(new_name: &str, names: &[String]) -> String {
  let mut index = 1;
  let name_map: std::collections::HashMap<String, bool> = names.iter().map(|name| (name.clone(), true)).collect();

  let mut uniq_name = new_name.to_string();
  while name_map.contains_key(&uniq_name) {
      uniq_name = format!("{} {}", new_name, index);
      index += 1;
  }
  uniq_name
}

pub enum NamePrefix {
  Field,
  GridView,
  KanbanView,
  GalleryView,
  FormView,
  CalendarView,
  GanttView,
  OrgChartView,
  View,
}

impl fmt::Display for NamePrefix {
  fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
    match self {
      NamePrefix::Field => write!(f, "field"),
      NamePrefix::GridView => write!(f, "grid_view"),
      NamePrefix::KanbanView => write!(f, "kanban_view"),
      NamePrefix::GalleryView => write!(f, "gallery_view"),
      NamePrefix::FormView => write!(f, "form_view"),
      NamePrefix::CalendarView => write!(f, "calendar_view"),
      NamePrefix::GanttView => write!(f, "gantt_view"),
      NamePrefix::OrgChartView => write!(f, "org_chart_view"),
      NamePrefix::View => write!(f, "view"),
    }
  }
}