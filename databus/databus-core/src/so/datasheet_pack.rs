use std::collections::HashMap;
use std::sync::Arc;

use serde::{Deserialize, Serialize};
use serde_json::{from_value, Value};
use utoipa::ToSchema;

use databus_shared::prelude::JsonExt;

use crate::prelude::datasheet_pack_vo::{BaseDatasheetPackVO, DatasheetPackVO, DatasheetSnapshotVO, SnapshotPackVO};
use crate::prelude::{FieldSO, RecordSO};
use crate::shared::IUserInfo;

use super::node::NodeSO;
use super::unit::UnitSO;
use super::view::ViewSO;
use super::widget::WidgetPanelSO;

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetEntitySO {
  pub id: i64,
  pub is_deleted: bool,
  pub creator: Option<i64>,
  pub revision: Option<i64>,
  pub created_by: Option<i64>,
  pub updated_by: Option<i64>,
  pub created_at: i64,
  pub updated_at: i64,
  pub dst_id: Option<String>,
  pub node_id: Option<String>,
  pub dst_name: Option<String>,
  pub space_id: Option<String>,
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetPackSO {
  pub snapshot: DatasheetSnapshotSO,
  pub datasheet: NodeSO,

  #[serde(skip_serializing_if = "JsonExt::is_falsy")]
  pub field_permission_map: Option<Value>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub foreign_datasheet_map: Option<HashMap<String, BaseDatasheetPackSO>>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub units: Option<Vec<UnitSO>>,
}

impl DatasheetPackSO {
  pub fn to_vo(&self) -> DatasheetPackVO {
    if self.foreign_datasheet_map.is_none() {
      return DatasheetPackVO {
        snapshot: self.snapshot.to_vo(),
        datasheet: self.datasheet.clone(),
        field_permission_map: self.field_permission_map.clone(),
        foreign_datasheet_map: None,
        units: self.units.clone(),
      };
    }
    let foreign_datasheet_map = self
      .foreign_datasheet_map
      .as_ref()
      .map(|map| map.iter().map(|(k, v)| (k.clone(), v.to_vo())).collect());

    DatasheetPackVO {
      snapshot: self.snapshot.to_vo(),
      datasheet: self.datasheet.clone(),
      field_permission_map: self.field_permission_map.clone(),
      foreign_datasheet_map,
      units: self.units.clone(),
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct SnapshotPackSO {
  pub snapshot: DatasheetSnapshotSO,
}

impl SnapshotPackSO {
  pub fn to_vo(&self) -> SnapshotPackVO {
    SnapshotPackVO {
      snapshot: self.snapshot.to_vo(),
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetSnapshotSO {
  pub meta: DatasheetMetaSO,
  pub record_map: HashMap<String, RecordSO>,
  pub datasheet_id: String,
  // pub datasheet_id: Option<String>,
}

impl DatasheetSnapshotSO {
  pub fn to_vo(&self) -> DatasheetSnapshotVO {
    let field_map = self.meta.field_map.clone();
    let mut record_map = HashMap::new();
    for (k, v) in self.record_map.iter() {
      record_map.insert(k.clone(), v.to_vo(&field_map));
    }
    DatasheetSnapshotVO {
      meta: self.meta.clone(),
      record_map,
      datasheet_id: self.datasheet_id.clone(),
    }
  }

  pub fn get_view(&self, view_id: &str) -> Option<&ViewSO> {
    self.meta.views.iter().find(|v| v.id.as_ref().unwrap() == view_id)
  }

  pub fn get_view_index(&self, view_id: &str) -> Option<usize> {
    self
      .meta
      .views
      .iter()
      .position(|v: &ViewSO| v.id.as_ref().unwrap() == view_id)
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct BaseDatasheetPackSO {
  pub snapshot: DatasheetSnapshotSO,
  pub datasheet: Value,

  #[serde(skip_serializing_if = "JsonExt::is_falsy")]
  pub field_permission_map: Option<Value>,
}

impl BaseDatasheetPackSO {
  pub fn to_vo(&self) -> BaseDatasheetPackVO {
    BaseDatasheetPackVO {
      snapshot: self.snapshot.to_vo(),
      datasheet: self.datasheet.clone(),
      field_permission_map: self.field_permission_map.clone(),
    }
  }
}

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, ToSchema)]
#[serde(rename_all = "camelCase")]
pub struct DatasheetMetaSO {
  pub field_map: HashMap<String, FieldSO>,
  pub views: Vec<ViewSO>,

  #[serde(skip_serializing_if = "Option::is_none")]
  pub widget_panels: Option<Vec<WidgetPanelSO>>,
}

#[derive(Debug, Clone, PartialEq)]
pub struct DatasheetPackContext {
  pub datasheet_pack: Box<DatasheetPackSO>,
  pub user_info: IUserInfo,

  pub unit_map: HashMap<String, Arc<UnitSO>>,
  pub user_map: HashMap<String, Arc<UnitSO>>,
}

impl DatasheetPackContext {
  pub fn get_snapshot(&self, datasheet_id: &str) -> Option<&DatasheetSnapshotSO> {
    if self.datasheet_pack.snapshot.datasheet_id == datasheet_id {
      return Some(&self.datasheet_pack.snapshot);
    } else {
      let option = self
        .datasheet_pack
        .foreign_datasheet_map
        .as_ref()
        .unwrap()
        .get(datasheet_id);
      if option.is_some() {
        return Some(&option.unwrap().snapshot);
      }
    };
    None
  }

  pub fn get_datasheet(&self, datasheet_id: &str) -> Option<NodeSO> {
    if self.datasheet_pack.snapshot.datasheet_id == datasheet_id {
      return Some(self.datasheet_pack.datasheet.clone());
    } else {
      let option = self
        .datasheet_pack
        .foreign_datasheet_map
        .as_ref()
        .unwrap()
        .get(datasheet_id);
      if option.is_some() {
        let datasheet = option.unwrap().datasheet.clone();
        let datasheet: NodeSO = from_value(datasheet).unwrap();
        return Some(datasheet);
      }
    };
    None
  }

  pub fn get_field_map(&self, datasheet_id: &str) -> Option<&HashMap<String, FieldSO>> {
    if self.datasheet_pack.snapshot.datasheet_id == datasheet_id {
      return Some(&self.datasheet_pack.snapshot.meta.field_map);
    } else {
      let option = self
        .datasheet_pack
        .foreign_datasheet_map
        .as_ref()
        .unwrap()
        .get(datasheet_id);
      if option.is_some() {
        return Some(&option.unwrap().snapshot.meta.field_map);
      }
    }
    None
  }

  pub fn get_first_view(&self, datasheet_id: &str) -> Option<&ViewSO> {
    if self.datasheet_pack.snapshot.datasheet_id == datasheet_id {
      return self.datasheet_pack.snapshot.meta.views.first();
    } else {
      let option = self
        .datasheet_pack
        .foreign_datasheet_map
        .as_ref()
        .unwrap()
        .get(datasheet_id);
      if option.is_some() {
        return option.unwrap().snapshot.meta.views.first();
      }
    }
    None
  }

  pub fn get_view(&self, datasheet_id: &str, view_id: &str) -> Option<&ViewSO> {
    if self.datasheet_pack.snapshot.datasheet_id == datasheet_id {
      return self.datasheet_pack.snapshot.get_view(view_id);
    } else {
      let option = self
        .datasheet_pack
        .foreign_datasheet_map
        .as_ref()
        .unwrap()
        .get(datasheet_id);
      if option.is_some() {
        return option.unwrap().snapshot.get_view(view_id);
      }
    }
    None
  }
}

pub fn prepare_context_data(datasheet_pack: Box<DatasheetPackSO>) -> DatasheetPackContext {
  let mut unit_map = HashMap::new();
  let mut user_map = HashMap::new();

  if let Some(units) = &datasheet_pack.units {
    for unit in units {
      let unit_arc = Arc::new(unit.clone());
      if unit.unit_id.is_some() {
        unit_map.insert(unit.unit_id.clone().unwrap(), unit_arc.clone());
      }
      user_map.insert(unit.uuid.clone().unwrap(), unit_arc);
    }
  }

  DatasheetPackContext {
    datasheet_pack,
    unit_map,
    user_map,
    user_info: IUserInfo::default(),
  }
}

#[cfg(test)]
mod tests {
  use crate::mock::mock_daily_sheet_meta::DAILY_SHEET_META;
  use crate::mock::mock_daily_sheet_meta::NAME_LIST_SHEET_META;
  use crate::mock::mock_data_pack::{
    DATA_PACK_ONE, FULL_FIELD_STR_MOCK, META_JSON_STR, META_JSON_STR2, MOCK_DATA_PACK_STR, MOCK_DATA_SHEET_META,
    MOCK_DATA_SHEET_META_WITH_INVALID_SORT_INFO, MOCK_DATA_SHEET_META_WITH_NULL_FILTER,
  };
  use crate::prelude::view_operation::filter::FOperator;
  use crate::prelude::{CellValueVo, FieldKindSO};
  use crate::so::view_operation::filter::FilterConjunction;

  #[test]
  fn test_data_sheet_pack_deserialize() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(MOCK_DATA_PACK_STR).expect("parse error");
    let data_pack = serde_json::from_value::<super::DatasheetPackSO>(data_pack_json).expect("parse error");
    let vo = data_pack.to_vo();
    let v0 = vo.snapshot.record_map.get("recYaAnzOg32z").unwrap();
    assert_eq!(
      matches!(v0.data.get("fldSa2a8K1Apq").unwrap(), CellValueVo::TextCellValue(_)),
      true
    );
    assert_eq!(
      matches!(v0.data.get("fldzxD63fegu1").unwrap(), CellValueVo::EmailCellValue(_)),
      true
    );
    assert_eq!(
      matches!(v0.data.get("fldS15iyc8WRL").unwrap(), CellValueVo::DateTimeCellValue(_)),
      true
    );
    assert_eq!(
      matches!(
        v0.data.get("fldETkEA1KS4z").unwrap(),
        CellValueVo::SingleSelectCellValue(_)
      ),
      true
    );
    assert_eq!(
      matches!(v0.data.get("fldUubtedqHbw").unwrap(), CellValueVo::MemberCellValue(_)),
      true
    );
    assert_eq!(
      matches!(v0.data.get("fldIKSitFsC5n").unwrap(), CellValueVo::CurrencyCellValue(_)),
      true
    );
  }

  #[test]
  fn test_data_sheet_pack_deserialize_one() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(DATA_PACK_ONE).expect("parse error");
    let data_pack = serde_json::from_value::<super::DatasheetPackSO>(data_pack_json).expect("parse error");
    let vo = data_pack.to_vo();
  }

  #[test]
  fn test_data_sheet_pack_full_fields() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(FULL_FIELD_STR_MOCK).expect("parse error");
    let data_pack = serde_json::from_value::<super::DatasheetPackSO>(data_pack_json).expect("parse error");
    let vo = data_pack.to_vo();
  }

  #[test]
  fn test_data_sheet_pack_meta_deserialize() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(MOCK_DATA_SHEET_META).expect("parse error");
    let data_pack_meta = serde_json::from_value::<super::DatasheetMetaSO>(data_pack_json).expect("parse error");
    println!("{:?}", data_pack_meta);
  }

  #[test]
  fn test_data_sheet_pack_meta_deserialize_with_invalid_sort_info() {
    let data_pack_json =
      serde_json::from_str::<serde_json::Value>(MOCK_DATA_SHEET_META_WITH_INVALID_SORT_INFO).expect("parse error");
    let data_pack_meta = serde_json::from_value::<super::DatasheetMetaSO>(data_pack_json).expect("parse error");
    println!("{:?}", data_pack_meta);
  }

  #[test]
  fn test_daily_sheet_pack_meta_deserialize() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(DAILY_SHEET_META).expect("parse error");
    let data_pack_meta = serde_json::from_value::<super::DatasheetMetaSO>(data_pack_json).expect("parse error");
  }

  #[test]
  fn test_name_list_sheet_pack_meta_deserialize() {
    let data_pack_json = serde_json::from_str::<serde_json::Value>(NAME_LIST_SHEET_META).expect("parse error");
    let data_pack_meta = serde_json::from_value::<super::DatasheetMetaSO>(data_pack_json).expect("parse error");
  }

  #[test]
  fn test_data_sheet_pack_meta_null_filter_deserialize() {
    let meta0 = serde_json::from_str::<serde_json::Value>(MOCK_DATA_SHEET_META_WITH_NULL_FILTER).expect("parse error");
    let _meta = serde_json::from_value::<super::DatasheetMetaSO>(meta0).expect("parse error");
  }

  // test serialize of DatasheetMetaSO from a META_JSON_STR
  #[test]
  fn test_datasheet_meta_so2() {
    let meta0 = serde_json::from_str::<serde_json::Value>(META_JSON_STR2).expect("parse error");
    let _meta = serde_json::from_value::<super::DatasheetMetaSO>(meta0).expect("parse error");
  }
  #[test]
  fn test_datasheet_meta_so() {
    let meta0 = serde_json::from_str::<serde_json::Value>(META_JSON_STR).expect("parse error");
    let meta = serde_json::from_value::<super::DatasheetMetaSO>(meta0).expect("parse error");
    assert_eq!(meta.views.len(), 3);
    assert_eq!(meta.views[0].id.clone().unwrap().as_str(), "viwBe2lWB3v7w");
    assert_eq!(meta.views[0].name.clone().unwrap().as_str(), "表格视图");
    assert_eq!(meta.views[0].columns.len(), 11);
    assert_eq!(meta.views[0].columns[3].field_id, "fldvtrHlLlZbf");
    assert_eq!(meta.views[0].columns[3].hidden, Some(false));
    assert_eq!(meta.views[0].columns[4].width, Some(200f64));
    assert_eq!(meta.views[0].columns[5].hidden, Some(false));
    assert_eq!(meta.views[0].columns[6].hidden, Some(false));
    assert_eq!(meta.views[0].columns[7].hidden, Some(false));
    assert_eq!(meta.views[0].columns[8].hidden, Some(false));
    assert_eq!(meta.views[0].columns[9].hidden, Some(false));
    assert_eq!(meta.views[0].columns[10].hidden, Some(false));
    assert_eq!(meta.views[0].auto_save, Some(false));
    assert_eq!(
      meta.views[0].filter_info.clone().unwrap().conjunction,
      FilterConjunction::And
    );
    assert_eq!(meta.views[0].filter_info.clone().unwrap().conditions.len(), 1);
    assert_eq!(
      meta.views[0].filter_info.clone().unwrap().conditions[0].field_id,
      "fldmHjmSjZxVn"
    );
    assert_eq!(
      meta.views[0].filter_info.clone().unwrap().conditions[0].operator,
      FOperator::Contains
    );
    assert_eq!(
      meta.views[0].filter_info.clone().unwrap().conditions[0].field_type,
      FieldKindSO::SingleText
    );
    assert_eq!(
      meta.views[0].filter_info.clone().unwrap().conditions[0].condition_id,
      "cdtYvVSlw23A6"
    );
    assert_eq!(meta.views[0].frozen_column_count, Some(1));
    assert_eq!(meta.views[0].display_hidden_column_within_mirror, Some(false));
    assert_eq!(meta.views[1].id.clone().unwrap(), "viw27xSrQmNCA");
    assert_eq!(meta.views[1].name.clone().unwrap(), " 架构视图");
  }
}
