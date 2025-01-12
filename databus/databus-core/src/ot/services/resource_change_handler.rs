use std::{collections::HashMap, sync::Arc};

use serde_json::{Value, json, to_value};

use crate::{ot::{types::{ResourceIdPrefix, ResourceType}, CommonData}, data_source_provider::IDataSourceProvider, so::InternalSpaceStatisticsRo};

use super::ResultSet;


pub struct ResourceChangeHandler {
    pub loader: Arc<dyn IDataSourceProvider>,
    // loader: 
    // logger: Logger,
    // room_resource_rel_service: RoomResourceRelService,
    // node_service: NodeService,
    // datasheet_widget_service: DatasheetWidgetService,
    // datasheet_field_handler: DatasheetFieldHandler,
    // rest_service: RestService,
}

impl ResourceChangeHandler {
    pub fn new(
        loader: Arc<dyn IDataSourceProvider>,
        // logger: Logger,
        // room_resource_rel_service: RoomResourceRelService,
        // node_service: NodeService,
        // datasheet_widget_service: DatasheetWidgetService,
        // datasheet_field_handler: DatasheetFieldHandler,
        // rest_service: RestService,
    ) -> Self {
        Self {
            loader
            // logger,
            // room_resource_rel_service,
            // node_service,
            // datasheet_widget_service,
            // datasheet_field_handler,
            // rest_service,
        }
    }

    pub async fn handle_resource_change(&self, room_id: String, 
        // values: Vec<Value>
        values: Vec<(Value, HashMap<String, Value>, CommonData, ResultSet)>
    ) {
        let _values_str = values.iter().map(|value| {
            json!({
                // "resourceId": value["commonData"]["resourceId"],
                // "dstId": value["commonData"]["dstId"],
                "resourceId": value.2.resource_id,
                "dstId": value.2.dst_id,
            })
        }).collect::<Vec<_>>();

        // self.logger.info(format!(
        //     "HandleResourceChange. roomId: {} values: {}",
        //     room_id,
        //     serde_json::to_string(&values_str).unwrap()
        // ));

        let _is_dsb_room = room_id.starts_with(ResourceIdPrefix::Dashboard.as_str());
        for value in values {
            // let common_data = &value["commonData"];
            // let common_data:CommonData = from_value(common_data.clone()).unwrap();
            let common_data = value.2;
            let dst_id = common_data.dst_id;
            let _resource_id = common_data.resource_id;
            let resource_type = common_data.resource_type;
            let effect_map = value.1;
            let result_set = value.3;
            // let effect_map = &value["effectMap"];
            // let effect_map = from_value(effect_map.clone()).unwrap();
            // let result_set = &value["resultSet"];
            // let result_set:ResultSet = from_value(result_set.clone()).unwrap();

            match resource_type {
                ResourceType::Datasheet => {
                    self.parse_datasheet_result_set(dst_id, effect_map, result_set).await;
                }
                // ResourceType::Dashboard => {
                //     self.parse_dashboard_result_set(resource_id, result_set).await;
                // }
                // ResourceType::Form => {}
                // ResourceType::Widget => {
                //     if is_dsb_room && result_set["updateWidgetDepDatasheetId"].is_some() {
                //         self.room_resource_rel_service.create_or_update_rel(dst_id, vec![result_set["updateWidgetDepDatasheetId"].as_str().unwrap()]).await;
                //     }
                // }
                _ => {}
            }
        }
    }

    async fn parse_datasheet_result_set(&self, dst_id: String, _effect_map: HashMap<String, Value>, result_set: ResultSet) {
        let mut add_resource_ids: Vec<String> = Vec::new();
        let mut del_resource_ids: Vec<String> = Vec::new();
    
        if result_set.add_widget_ids.len() > 0 {
            add_resource_ids.extend(result_set.add_widget_ids.clone());
        }

        if result_set.delete_widget_ids.len() > 0 {
            del_resource_ids.extend(result_set.delete_widget_ids.clone());
        }
    
        if result_set.to_change_formula_expressions.len() > 0 {
            println!("to_change_formula_expressions: {:?}", result_set.to_change_formula_expressions);
            // self.datasheet_field_handler.compute_formula_reference(dst_id.clone(), result_set.to_change_formula_expressions).await;
        }
        // New link field
        if result_set.to_create_foreign_datasheet_id_map.len() > 0 {
            println!("to_create_foreign_datasheet_id_map: {:?}", result_set.to_create_foreign_datasheet_id_map);
            // let meta = effect_map.get("meta").unwrap();
            // let dst_ids = self.datasheet_field_handler.compute_link_field_reference(dst_id.clone(), meta.clone(), result_set.to_create_foreign_datasheet_id_map).await;
            // const meta = effectMap.get(EffectConstantName.Meta);
            // const dstIds = await this.datasheetFieldHandler.computeLinkFieldReference(dstId, meta, resultSet.toCreateForeignDatasheetIdMap);
            // if (dstIds?.length) {
            //   addResourceIds.push(...dstIds);
            // }
        }
        // Delete link field
        if result_set.to_delete_foreign_datasheet_id_map.len() > 0 {
            println!("to_delete_foreign_datasheet_id_map: {:?}", result_set.to_delete_foreign_datasheet_id_map);
            // let meta = effect_map.get("meta").unwrap();
            // let dst_ids = self.datasheet_field_handler.delete_link_field_reference(dst_id.clone(), meta.clone(), result_set.to_delete_foreign_datasheet_id_map).await;
            // const meta = effectMap.get(EffectConstantName.Meta);
            // const dstIds = await this.datasheetFieldHandler.deleteLinkFieldReference(dstId, meta, resultSet.toDeleteForeignDatasheetIdMap);
            // if (dstIds?.length) {
            //   delResourceIds.push(...dstIds);
            // }
        }
        // Original LookUp prop change only influence 1-to-1 reference relation change, compute individually regardless of order is ok.
        // After filter was introduced, field references form 1-to-many relation (like formulas) to cover, to make sure overlapped fields maintain
        // their reference relation, Deleting LookUp analysis should come first
        // Delete LookUp
        if result_set.to_delete_look_up_properties.len() > 0 {
            println!("to_delete_look_up_properties: {:?}", result_set.to_delete_look_up_properties);
            // let meta = effect_map.get("meta").unwrap();
            // let dst_ids = self.datasheet_field_handler.remove_look_up_reference(dst_id.clone(), meta.clone(), result_set.to_delete_look_up_properties).await;
            // const meta = effectMap.get(EffectConstantName.Meta);
            // const dstIds = await this.datasheetFieldHandler.removeLookUpReference(dstId, meta, resultSet.toDeleteLookUpProperties);
            // if (dstIds?.length) {
            //   delResourceIds.push(...dstIds);
            // }
        }
        // New LookUp
        if result_set.to_create_look_up_properties.len() > 0 {
            println!("to_create_look_up_properties: {:?}", result_set.to_create_look_up_properties);
            // let meta = effect_map.get("meta").unwrap();
            // let dst_ids = self.datasheet_field_handler.compute_look_up_reference(dst_id.clone(), meta.clone(), result_set.to_create_look_up_properties).await;
            // const meta = effectMap.get(EffectConstantName.Meta);
            // const dstIds = await this.datasheetFieldHandler.computeLookUpReference(dstId, meta, resultSet.toCreateLookUpProperties);
            // if (dstIds?.length) {
            //   addResourceIds.push(...dstIds);
            // }
        }
      
        // Obtain related node resource (form, mirror, etc) of the datasheet
        let rel_node_ids = self.loader.get_rel_node_id_by_main_node_id(&dst_id).await.unwrap();
        println!("rel_node_ids: {:?}", rel_node_ids);
      
        // Create or update Room - Resource bijection
        println!("add_resource_ids: {:?}", add_resource_ids);
        if add_resource_ids.len() > 0 {
            // self.loader.create_or_update_rel(&dst_id, &add_resource_ids).await.unwrap();
            // Update related node resource asynchronously
            // for node_id in rel_node_ids {
            //     self.loader.create_or_update_rel(&node_id, &add_resource_ids).await.unwrap();
            // }
        }
        // Break Room - Resource bijection
        println!("del_resource_ids: {:?}", del_resource_ids);
        if del_resource_ids.len() > 0 {
            // self.loader.remove_rel(&dst_id, &del_resource_ids).await.unwrap();
            // Update related node resource asynchronously
            // for node_id in rel_node_ids {
            //     self.loader.remove_rel(&node_id, &del_resource_ids).await.unwrap();
            // }
        }
        self.handle_space_statistics(result_set).await;
        //   await this.handleSpaceStatistics(resultSet);
    }

    async fn handle_space_statistics(&self, result_set: ResultSet) {
        let view_count: HashMap<u32, u32> = HashMap::new();
    
        println!("add_views: {:?}", result_set.add_views);
        println!("delete_views: {:?}", result_set.delete_views);
        if result_set.add_views.len() > 0 || result_set.delete_views.len() > 0 {
            // self.calculate_view_count(&mut view_count, result_set.add_views, false).await;
            // self.calculate_view_count(&mut view_count, result_set.delete_views, true).await;
        }
    
        let add_record_count = result_set.to_create_record.len() as i32;
        let record_count = add_record_count - result_set.to_delete_record_ids.len() as i32;
    
        println!("recordCount = {}", record_count);
        println!("Object.keys(viewCount).length = {}", view_count.len());
    
        if view_count.len() > 0 || record_count != 0 {
            let space_id = result_set.space_id;
            let option = InternalSpaceStatisticsRo {
                view_count: Some(view_count),
                record_count: Some(record_count),
            };
            match self.loader.update_space_statistics(&space_id, to_value(option).unwrap()).await {
                Ok(_) => (),
                Err(err) => eprintln!("modifySpaceStatisticsError:{}: {}", space_id, err),
            }
        }
    }
}