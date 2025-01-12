use crate::{ot::{commands::{CollaCommandDefExecuteResult, SetOrgChartStyleOptions, ExecuteResult}, types::{ActionOTO, ResourceType}, ViewAction, SetOrgChartStyleOTO}, so::DatasheetSnapshotSO};


pub struct SetOrgChartStyle {

}

impl SetOrgChartStyle {
    pub fn execute (
        snapshot: DatasheetSnapshotSO,
        options: SetOrgChartStyleOptions,
    ) -> anyhow::Result<Option<CollaCommandDefExecuteResult>> {
        let SetOrgChartStyleOptions { cmd:_, view_id, style_key, style_value } = options;
        // let datasheet_id = get_active_datasheet_id(state).unwrap();
        let datasheet_id = snapshot.datasheet_id.clone();
        // let datasheet = get_datasheet(state, datasheet_id);
        // if state.is_none() || datasheet.is_none() {
        //     return None;
        // }

        // Determine whether the currently operating view is the active view
        // if datasheet.active_view != view_id {
        //     panic!(t(Strings.error_modify_column_failed_wrong_target_view));
        // }

        let mut actions: Vec<ActionOTO> = Vec::new();
        let set_org_chart_style_action = ViewAction::set_org_chart_style_to_action(snapshot, SetOrgChartStyleOTO {
            view_id,
            style_key,
            style_value,
        }).unwrap();
        // action && collected.push(action);
        if let Some(action) = set_org_chart_style_action {
            actions.push(action);
        }
        if actions.is_empty() {
            return Ok(None);
        }
        Ok(Some(CollaCommandDefExecuteResult {
            result: ExecuteResult::Success,
            resource_id: datasheet_id,
            resource_type: ResourceType::Datasheet,
            actions,
            ..Default::default()
        }))
    }
}