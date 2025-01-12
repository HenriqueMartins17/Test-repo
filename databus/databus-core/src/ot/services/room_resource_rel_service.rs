use std::{collections::HashSet, sync::Arc};
use serde::{Serialize, Deserialize};
use time::Instant;
use crate::{data_source_provider::IDataSourceProvider, ot::{types::ResourceIdPrefix, changeset::RemoteChangeset}};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq, Default)]
#[serde(rename_all = "camelCase")]
pub struct IClientRoomChangeResult {
    pub changeset: RemoteChangeset,
    pub room_ids: Vec<String>,
}

pub struct RoomResourceRelService {
    pub loader: Arc<dyn IDataSourceProvider>,
//     logger: Logger,
//     redis_service: RedisService,
//     datasheet_meta_service: DatasheetMetaService,
//     compute_field_reference_manager: ComputeFieldReferenceManager,
//     datasheet_service: Rc<RefCell<DatasheetService>>,
//     resource_meta_repository: ResourceMetaRepository,
//     widget_service: WidgetService,
}

impl RoomResourceRelService {
    pub fn new(
        loader: Arc<dyn IDataSourceProvider>,
        // logger: Logger,
        // redis_service: RedisService,
        // datasheet_meta_service: DatasheetMetaService,
        // compute_field_reference_manager: ComputeFieldReferenceManager,
        // datasheet_service: Rc<RefCell<DatasheetService>>,
        // resource_meta_repository: ResourceMetaRepository,
        // widget_service: WidgetService,
    ) -> Self {
        Self {
            loader
            // logger,
            // redis_service,
            // datasheet_meta_service,
            // compute_field_reference_manager,
            // datasheet_service,
            // resource_meta_repository,
            // widget_service,
        }
    }

    pub async fn get_effect_datasheet_ids(&self, resource_ids: Vec<String>) -> Vec<String> {
        let mut all_effect_resource_ids: HashSet<String> = HashSet::new();
    
        for resource_id in resource_ids {
            let room_ids = self.get_datasheet_room_ids(&resource_id, true).await;
            println!("roomIds = {:?}", room_ids);

            if room_ids.len() == 0 {
                // let dst_ids = self.reverse_compute_datasheet_room(&resource_id).await;
                // for id in dst_ids {
                //     all_effect_resource_ids.insert(id);
                // }
                // continue;
            }
    
            for id in room_ids {
                if id.starts_with(ResourceIdPrefix::Datasheet.as_str()) {
                    all_effect_resource_ids.insert(id);
                }
            }
        }
    
        all_effect_resource_ids.into_iter().collect()
    }

    pub async fn get_datasheet_room_ids(&self, resource_id: &str, without_self: bool) -> Vec<String> {
        let resource_key = format!("apitable:nest:resource:{}", resource_id);
        let mut room_ids = self.loader.get_room_ids_from_cache(&resource_key).await.unwrap_or_default();
        if !without_self && room_ids.is_empty() && resource_id.starts_with(ResourceIdPrefix::Datasheet.as_str()) {
            return vec![resource_id.to_string()];
        }
    
        room_ids.retain(|id| id.starts_with(ResourceIdPrefix::Datasheet.as_str()));
        room_ids
    }

    pub async fn get_room_change_result(&self, room_id: String, changesets: Vec<RemoteChangeset>) -> anyhow::Result<Vec<IClientRoomChangeResult>> {
        let start_time = Instant::now();
        println!("Start loading RoomChangeResult roomId:{}", room_id);
    
        // let client = self.redis_service.get_client().await?;
        let mut results: Vec<IClientRoomChangeResult> = Vec::new();
    
        for cs in changesets {
            let resource_key = format!("apitable:nest:resource:{}", cs.resource_id);
            let mut room_ids = self.loader.get_room_ids_from_cache(&resource_key).await.unwrap_or_default();
            println!("room_ids = {:?}", room_ids);
            if room_ids.is_empty() {
                room_ids.push(room_id.clone());
                self.loader.cache_room_ids(&resource_key, room_ids.clone()).await;
            } else if !room_ids.contains(&room_id) {
                room_ids.push(room_id.clone());
            }
    
            results.push(IClientRoomChangeResult {
                changeset: cs,
                room_ids,
            });
        }
    
        let end_time = Instant::now();
        println!("Finished loading RoomChangeResult roomId:{}, duration: {}ms", room_id, end_time-start_time);
    
        Ok(results)
    }
}
