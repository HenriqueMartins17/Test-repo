use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetDateTimeCellAlarmOptions, ExecuteResult}, types::ResourceType, RecordAlarmOTO}, so::{DatasheetSnapshotSO, RecordAlarm}, DatasheetActions, utils::uuid::{get_new_id, IDPrefix}};

pub struct SetDateTimeCellAlarm {

}

impl SetDateTimeCellAlarm {
    pub fn execute (
        options: SetDateTimeCellAlarmOptions,
        snapshot: Option<DatasheetSnapshotSO>,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> { 
        // let state = context.state;
        let SetDateTimeCellAlarmOptions { record_id, field_id, alarm, cmd:_, datasheet_id:_ } = options;
        let new_alarm_id = get_new_id(IDPrefix::DateTimeAlarm, Vec::new());
        // let datasheet_id = options.datasheet_id.unwrap_or_else(|| Selectors::get_active_datasheet_id(state).unwrap());
        // let snapshot = Selectors::get_snapshot(state, datasheet_id);
        if snapshot.is_none() {
            return Ok(None);
        }
        let snapshot = snapshot.unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        let actions = DatasheetActions::set_date_time_cell_alarm(snapshot.clone(), RecordAlarmOTO {
            record_id,
            field_id,
            alarm: if let Some(alarm) = alarm {
                Some(RecordAlarm {
                    id: Some(new_alarm_id),
                    ..alarm
                })
            } else {
                None
            }
        }).unwrap();
        if actions.is_none() {
            return Ok(None);
        }
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions: actions.unwrap(),
            ..Default::default()
        }))
    }
}