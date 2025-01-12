use std::{collections::HashMap, sync::Arc};
use serde_json::{Value, to_value, from_value};

use time::Instant;

use crate::{ot::{RoomChannelMessage, changeset::{RemoteChangeset, LocalChangeset, Changeset}, OtEventContext, ChangesetParseResult, CommonData, types::{ResourceType, ActionOTO}, MAX_REVISION_DIFF, EffectConstantName}, so::FieldSO, shared::AuthHeader, data_source_provider::IDataSourceProvider};

use super::{DatasheetOtService, ResourceChangeHandler, RoomResourceRelService};

 pub struct OtService {
    pub loader: Arc<dyn IDataSourceProvider>,
    pub datasheet_ot_service: DatasheetOtService,
    pub resource_change_handler: ResourceChangeHandler,
    pub rel_service: RoomResourceRelService,
 }

 impl OtService {
    pub fn new(
        loader: Arc<dyn IDataSourceProvider>,
        datasheet_ot_service: DatasheetOtService,
        resource_change_handler: ResourceChangeHandler,
        rel_service: RoomResourceRelService,
    ) -> Self {
        Self {loader, datasheet_ot_service, resource_change_handler, rel_service}
    }

    /// /**
    /// * @param message client ROOM message
    /// */
    pub async fn apply_room_changeset(
        &self,
        message: RoomChannelMessage, 
        mut auth: AuthHeader
    ) -> anyhow::Result<Vec<RemoteChangeset>> {
        // Validate that sharing enables editing
        if let Some(_share_id) = &message.share_id { //None
            // self.node_share_setting_service
            //     .check_node_share_can_be_edited(share_id, message.room_id)
            //     .await?;
        }
        //数据库查询
        let space_id = self.loader.get_space_id_by_node_id(&message.room_id).await?;
        if space_id.is_none() {
            return Err(anyhow::anyhow!("OtException::SPACE_NOT_FOUND"));
        }
        auth.space_id = space_id.clone();
        let space_id = space_id.unwrap();

        let _msg_ids: Vec<_> = message
            .changesets
            .iter()
            .map(|cs| cs.message_id.to_string())
            .collect();

        // let client = self.redis_service.get_client(); //redis
        // let lock = RedisLock::new(client);

        // Lock resource, messages of the same resource are consumed sequentially. Timeout is 120s
        // let unlock = lock
        //     .lock(message.changesets.iter().map(|cs| cs.resource_id), 120 * 1000)
        //     .await?;
        let mut attach_cites = Vec::new();
        let mut results = Vec::new();
        let mut context = OtEventContext {
            auth_header: auth.clone(),
            space_id: space_id.clone(),
            from_editable_shared_node: !message.share_id.is_none(),
            operator_user_id: None,
        };

        // //!!! WARN All services and network related operations must be put inside try blocks, or dead lock may happen.
        let begin_time = Instant::now();
        println!("begin_time {:?}", begin_time);
        // self.logger.info(format!("room:[{}] ====> parseChanges Start......", message.room_id));
        let mut transactions = Vec::new();
        for cs in &message.changesets {
            let ChangesetParseResult {transaction, effect_map, common_data, result_set} =
                // self.parse_changes(space_id, message, cs, auth).await?;
                self.parse_changes(space_id.clone(), message.clone(), cs, auth.clone()).await?;
            if context.operator_user_id.is_none() && common_data.user_id.is_some() {
                context.operator_user_id = common_data.user_id.clone();
            }
            transactions.push((transaction, effect_map.clone(), common_data, result_set));
            attach_cites.push(effect_map.get(EffectConstantName::AttachCite.to_string().as_str()).unwrap().clone());
        }
        let parse_end_time = Instant::now();
        println!("parse_end_time {:?}", parse_end_time);
        // self.logger.info(format!(
        //     "room:[{}] ====> parseChanges Finished, duration: {}ms. General transaction start......",
        //     message.room_id,
        //     parse_end_time - begin_time
        // ));
        // ======== multiple-resource operation transaction BEGIN ========
        // self.get_manager()
        //     .transaction(|manager| async move {
                for (_transaction, effect_map, common_data, result_set) in &mut transactions {
                    // transaction(manager, effect_map, common_data, result_set).await?;
                    self.datasheet_ot_service.transaction( effect_map, common_data, result_set).await?;
                    let mut remote_changeset: RemoteChangeset = from_value(effect_map.get(EffectConstantName::RemoteChangeset.to_string().as_str()).unwrap().clone()).unwrap();
                    if remote_changeset.user_id.is_none() && common_data.user_id.is_some() {
                        remote_changeset.user_id = common_data.user_id.clone();
                    }
                    results.push(remote_changeset);
                    // member field auto subscription，async method
                    // self.record_subscription_service
                    //     .handle_record_auto_subscriptions(common_data, result_set)
                    //     .await;
                }
                // Ok(())
            // })
            // .await?;
        // let end_time = Instant::now();
        // println!("end_time {:?}", end_time);
        // self.logger.info(format!(
        //     "room:[{}] ====> General transaction finished, duration: {}ms",
        //     message.room_id,
        //     end_time - parse_end_time
        // ));
        // // Process resource change event
        // let transactions = to_valu
        self.resource_change_handler
            .handle_resource_change(message.room_id, transactions)
            .await;
        // self.resource_change_handler
        //     .handle_resource_change(message.room_id, transactions)
        //     .await?;

        // // ======== multiple-resource operation transaction END ========

        // // Release lock of each resource
        // unlock().await?;

        // println!("attach_cites {:?}", attach_cites);
        // await Promise.all(attachCites.map(item => this.restService.calDstAttachCite(auth, item)));
        
        let this_batch_resource_ids = results.iter().filter(|result| 
            result.resource_type == ResourceType::Datasheet)
            .map(|result| result.resource_id.clone()).collect::<Vec<String>>();
        // println!("this_batch_resource_ids {:?}", this_batch_resource_ids);
        let all_effect_dst_ids = self.rel_service.get_effect_datasheet_ids(this_batch_resource_ids).await;
        // println!("all_effect_dst_ids {:?}", all_effect_dst_ids);
        let has_active_robot = self.loader.get_has_robot_by_resource_ids(all_effect_dst_ids.clone()).await?;
        // println!("has_active_robot {:?}", has_active_robot);
        if has_active_robot {
            // Handle event here
            // this.logger.info('applyRoomChangeset-robot-event-start', { roomId: message.roomId, msgIds, allEffectDstIds, thisBatchResourceIds });
            // Clear cache
            all_effect_dst_ids.iter().for_each(|_resource_id| {
                // clearComputeCache(resourceId);
            });
            // automation async function
            // void this.eventService.handleChangesets(results);
            // self.loader.handle_changesets(results, context).await?;
            // this.logger.info('applyRoomChangeset-robot-event-end', { roomId: message.roomId, msgIds });
        }
        // println!("context {:?}", context);
        //   void this.otEventService.handleChangesets(results, context);
      
        //   // clear cached selectors, will remove after release/1.0.0
        //   clearCachedSelectors();

        Ok(results)
    }

    async fn parse_changes(
        &self,
        space_id: String,
        message: RoomChannelMessage,
        changeset: &LocalChangeset,
        auth: AuthHeader,
    ) -> anyhow::Result<ChangesetParseResult> {
        let RoomChannelMessage {
            source_datasheet_id,
            source_type,
            share_id,
            room_id,
            internal_auth:_,
            allow_all_entrance,
            changesets: _,
        } = message;
        let LocalChangeset { resource_id, resource_type, .. } = changeset;
    
        // Fill in resource_type if it is null
        // if changeset.resource_type.is_none() {
        //     match &resource_id[0..3] {
        //         ConfigConstant::NodeTypeReg::DATASHEET => {
        //             changeset.resource_type = Some(ResourceType::Datasheet);
        //         }
        //         ConfigConstant::NodeTypeReg::DASHBOARD => {
        //             changeset.resource_type = Some(ResourceType::Dashboard);
        //         }
        //         ConfigConstant::NodeTypeReg::WIDGET => {
        //             changeset.resource_type = Some(ResourceType::Widget);
        //         }
        //         _ => {}
        //     }
        // }
        // If no revision exists, default to latest revision of the datasheet
        // if changeset.base_revision.is_none() {
            // changeset.base_revision = Some(
            //     this.changeset_service
            //         .get_max_revision(&resource_id, resource_type)
            //         .await
            //         .unwrap(),
            // );
        // }
    
        // Check if resource message is duplicate
        // if this.logger.is_debug_enabled() {
        //     this.logger.debug(format!("Check if resource [{}] message is duplicate", resource_id));
        // }
        //数据库查询
        // let msg_exist = this
        //     .changeset_service
        //     .count_by_resource_id_and_message_id(&resource_id, resource_type, changeset.message_id)
        //     .await;
        // if msg_exist {
        //     // Duplicate message, throw an exception
        //     return Err(ServerException::new(OtException::MSG_ID_DUPLICATE));
        // }
    
        // Query resource revision
        // if this.logger.is_debug_enabled() {
        //     this.logger.debug(format!("[{}] Obtain revision from database", resource_id));
        // }
        //数据库查询
        // let ResourceMetaInfo {
        //     resource_revision,
        //     node_id,
        // } = this.resource_meta_service.get_resource_info(&resource_id, resource_type).await;
        // if resource_revision.is_none() || node_id.is_none() {
        //     this.logger.info(format!(
        //         REVISION_ERROR : {} 
        //         resource_revision.unwrap_or_default(),
        //         node_id.unwrap_or_default(),
        //         resource_id
        //     ));
        //     return Err(ServerException::new(OtException::REVISION_ERROR));
        // }
        let resource_revision = self.loader.get_datasheet_revision(resource_id).await?;
        let node_id = resource_id.clone();
        // Effect variable collector
        let mut effect_map = HashMap::new();

        // Transform operations submitted by client into correct changeset
        let remote_changeset = self.transform(changeset, resource_revision.clone(), &mut effect_map).unwrap();
        effect_map.insert(EffectConstantName::RemoteChangeset.to_string(), to_value(remote_changeset).unwrap());
        // // Map that needs notification
        effect_map.insert(EffectConstantName::MentionedMessages.to_string(), Value::Null);

        // let resource_id = changeset.resource_id;
        // let resource_type = changeset.resource_type;

        // // Obtain max revision of changesets
        // let changeset_revision = self.changeset_service.get_max_revision(resource_id, resource_type).await;
        let changeset_revision = resource_revision.clone();
        // if self.logger.is_debug_enabled() {
        //     self.logger.debug(format!("[{}] original max revision of changesets: {}", resource_id, changeset_revision));
        // }

        // // If no max revision exists, use the revision from client
        // let right_revision = if let Some(changeset_revision) = changeset_revision {
        //     changeset_revision + 1
        // } else {
        //     changeset.revision
        // };
        let right_revision = changeset_revision + 1;
        // if self.logger.is_debug_enabled() {
        //     self.logger.debug(format!("[{}] Theoretical value of original revision of changesets: {}", resource_id, right_revision));
        //     self.logger.debug(format!("[{}] Theoretical value of client revision: {}", resource_id, changeset.revision));
        // }

        // let is_equal = changeset.revision == right_revision;
        // // Reject the revision if the revisions are not equal
        // if !is_equal {
        //     return Err(OtException::MatchVersionError);
        // }

        // Obtain permission, if editable is enabled, don't query node/permission, set permission to editor directly,
        // and obtain userId
        // let permission = if let Some(internal_auth) = internal_auth {
        //     // Permission {
        //     //     ...self.permission_services.get_default_manager_permission(),
        //     //     user_id: internal_auth.user_id,
        //     //     uuid: internal_auth.uuid,
        //     // }
        //     // 111111
        // } else {
            let permission = self.loader.get_node_role(
                node_id.clone(),
                auth.clone(),
                share_id,
                Some(room_id.clone()),
                source_datasheet_id.clone(),
                source_type.clone(),
                allow_all_entrance,
            ).await?;
        // };
        // this.getNodeRole(nodeId, auth, shareId, roomId, sourceDatasheetId, sourceType, allowAllEntrance);

        // Traverse operations from client, there may be multiple operations, but applied on the same resource.
        let _begin_time = Instant::now();
        // self.logger.info(format!("[{}] ====> Start Meta Operations traversal......", resource_id));
        // let transaction = Value::Null;
        let mut transaction = Value::Null;
        // let mut result_set= ResultSet;
        let mut result_set = self.datasheet_ot_service.create_result_set();
        match resource_type {
            ResourceType::Datasheet => {
                transaction = self.datasheet_ot_service.analyse_operates(
                    space_id.clone(),
                    source_datasheet_id.unwrap_or(room_id),
                    &changeset.operations,
                    &resource_id,
                    &permission,
                    &mut effect_map,
                    &mut result_set,
                    &auth,
                    &source_type,
                ).await?;
            },
            // ResourceType::Widget => {
            //     result_set = self.widget_ot_service.create_result_set();
            //     transaction = self.widget_ot_service.analyse_operates(
            //         &changeset.operations,
            //         &permission,
            //         &result_set,
            //     ).await;
            // },
            // ResourceType::Dashboard => {
            //     result_set = self.dashboard_ot_service.create_result_set();
            //     transaction = self.dashboard_ot_service.analyse_operates(
            //         &changeset.operations,
            //         &permission,
            //         &result_set,
            //     ).await;
            // },
            // ResourceType::Mirror => {
            //     result_set = self.mirror_ot_service.create_result_set();
            //     transaction = self.mirror_ot_service.analyse_operates(
            //         &changeset.operations,
            //         &permission,
            //         &result_set,
            //     ).await;
            // },
            // ResourceType::Form => {
            //     result_set = self.form_ot_service.create_result_set();
            //     transaction = self.form_ot_service.analyse_operates(
            //         &changeset.operations,
            //         &permission,
            //         &result_set,
            //     ).await;
            // },
            _ => {}
        }
        // self.logger.info(format!("[{}] ====> Finished Meta Operations traversal......duration: {}ms", resource_id, begin_time.elapsed().unwrap().as_millis()));

        // let common_data = CommonData {
        //     user_id: permission.user_id,
        //     uuid: permission.uuid,
        //     space_id,
        //     dst_id: node_id,
        //     revision: right_revision,
        //     resource_id,
        //     resource_type,
        //     permission,
        // };
        let common_data = CommonData {
            user_id: permission.user_id.clone(),
            uuid: permission.uuid.clone(),
            space_id,
            dst_id: node_id,
            revision: right_revision as u32,
            resource_id: resource_id.clone(),
            resource_type: resource_type.clone(),
            permission,
            ..Default::default()
        };

        Ok(ChangesetParseResult {
            transaction,
            result_set,
            effect_map,
            common_data,
        })
    }

    fn transform(
        &self,
        changeset: &LocalChangeset,
        db_revision: i32,
        _effect_map: &mut HashMap<String, Value>,
    ) -> anyhow::Result<RemoteChangeset> {
        let LocalChangeset { base_revision, message_id, resource_type, resource_id, operations} = changeset;
        // let LocalChangeset { base_revision, local_changeset } = changeset;
        // if self.logger.is_debug_enabled() {
        //     self.logger.debug(format!("[{}] revision in database:{}", changeset.resource_id, db_revision));
        //     self.logger.debug(format!("[{}] revision from client:{}", changeset.resource_id, base_revision));
        // }
        let revision_diff = db_revision - base_revision;
        // base_revision is not greater than current_revision theoretically
        if revision_diff < 0 {
            return Err(anyhow::anyhow!("OtException::REVISION_CONFLICT"));
        }
    
        // Difference of base_revision and server revision is too large
        if revision_diff > MAX_REVISION_DIFF {
            return Err(anyhow::anyhow!("OtException::REVISION_OVER_LIMIT"));
        }
    
        // self.logger.info(format!(
        //     "{}[{} / {}] operations length: {}",
        //     changeset.resource_id,
        //     base_revision,
        //     db_revision,
        //     changeset.operations.len()
        // ));
        // base_revision is not equal to current_revision, needs transform
        if revision_diff > 0 {
            // Generate revision diff array, for example, if changesets of revisions 8~10 will be fetched, generate [8,9,10]
            let _revisions: Vec<i32> = (base_revision + 1..=db_revision).collect();
            //数据库查询
            // let changesets = self
            //     .changeset_service
            //     .get_by_revisions(changeset.resource_id, changeset.resource_type, revisions)
            //     .await?;
            // let is_equal = changesets.len() == revisions.len();
            let is_equal = true;
            if !is_equal {
                // self.logger.info(format!(
                //     "REVISION_ERROR :{} --- {} --- {}",
                //     revisions
                //         .iter()
                //         .map(|r| r.to_string())
                //         .collect::<Vec<String>>()
                //         .join(","),
                //     changesets
                //         .iter()
                //         .map(|item| item.revision.to_string())
                //         .collect::<Vec<String>>()
                //         .join(","),
                //     changeset.resource_id
                // ));
                return Err(anyhow::anyhow!("OtException::REVISION_ERROR"));
            }
            // let server_actions: Vec<ActionOTO> = changesets
            //     .iter()
            //     .flat_map(|cs| cs.operations.iter().flat_map(|op| op.actions.iter().cloned()))
            //     .collect();
            let mut _local_action_length = 0;
            for op in changeset.operations.iter() {
                _local_action_length += op.actions.len();
            }
            // Revision in database is too large, do not transform in server, hand to client
            // to compare and then store
            // let server_config = self.env_config_service.get_room_config(EnvConfigKey::CONST).unwrap() as IServerConfig;
            // if server_actions.len() * local_action_length > server_config.transform_limit {
            //     self.logger.error(format!(
            //         "{}[action diff too large] {}/{}",
            //         changeset.resource_id,
            //         local_action_length,
            //         server_actions.len()
            //     ));
            //     return Err(OtException::REVISION_OVER_LIMIT);
            // }
    
            let _field_map: Option<String> = if resource_type == &ResourceType::Datasheet {
                // self.datasheet_ot_service
                //     .get_meta_data_by_cache(resource_id, effect_map)
                //     .await?
                //     .field_map
                None
            } else {
                None
            };
            // let remote_changeset = OtService::transform_local_changeset(
            //     local_changeset,
            //     server_actions,
            //     db_revision,
            //     field_map,
            // );
            // Ok(remote_changeset)
            // Ok(RemoteChangeset {
            //     ..Default::default()
            // })
            println!("2222");
            Ok(RemoteChangeset {
                revision: db_revision + 1,
                message_id: message_id.clone(),
                resource_type: resource_type.clone(),
                resource_id: resource_id.clone(),
                operations: operations.clone(),
                ..Default::default()
            })
        } else {
            println!("3333");
            Ok(RemoteChangeset {
                revision: db_revision + 1,
                message_id: message_id.clone(),
                resource_type: resource_type.clone(),
                resource_id: resource_id.clone(),
                operations: operations.clone(),
                ..Default::default()
            })
        }
    }

    fn _transform_local_changeset(
        _local_changeset: Changeset,
        _server_actions: Vec<ActionOTO>,
        _db_revision: i32,
        _field_map: Option<HashMap<String, FieldSO>>,
    ) -> RemoteChangeset {
        // let mut server_link_cell_actions: Option<CellActionMap> = None;
        // let mut link_field_ids: Option<HashSet<String>> = None;
        // let mut changed_field_ids: Option<HashSet<String>> = None;
    
        // if let Some(field_map) = field_map {
        //     server_link_cell_actions = Some(CellActionMap::new());
        //     link_field_ids = Some(HashSet::new());
        //     changed_field_ids = Some(HashSet::new());
    
        //     for (field_id, field) in field_map.iter() {
        //         if field.field_type == FieldType::Link || field.field_type == FieldType::OneWayLink {
        //             link_field_ids.as_mut().unwrap().insert(field_id.clone());
        //         }
        //     }
    
        //     let mut latest_field_ids = HashSet::new();
        //     if let Some(link_field_ids) = &link_field_ids {
        //         latest_field_ids = link_field_ids.clone();
        //     }
    
        //     let mut server_actions_rev = server_actions.clone();
        //     server_actions_rev.reverse();
        //     for action in server_actions_rev {
        //         if action.p.len() == 4 && action.p[0] == "recordMap" && action.p[2] == "data" && link_field_ids.as_ref().unwrap().contains(&(action.p[3] as String)) {
        //             let record_id = action.p[1] as String;
        //             let field_id = action.p[3] as String;
        //             let next_action = server_link_cell_actions.as_ref().unwrap().get(&record_id, &field_id);
        //             if let Some(next_action) = next_action {
        //                 let new_op = jot::compose(&[action], &[next_action]);
        //                 if !new_op.is_empty() {
        //                     let new_action = new_op[new_op.len() - 1].clone();
        //                     if let (Some(oi), Some(od)) = (new_action.oi.clone(), new_action.od.clone()) {
        //                         if oi != od {
        //                             server_link_cell_actions.as_mut().unwrap().set(&record_id, &field_id, &new_action);
        //                         } else {
        //                             server_link_cell_actions.as_mut().unwrap().delete(&record_id, &field_id);
        //                         }
        //                     } else {
        //                         server_link_cell_actions.as_mut().unwrap().set(&record_id, &field_id, &new_action);
        //                     }
        //                 } else {
        //                     server_link_cell_actions.as_mut().unwrap().delete(&record_id, &field_id);
        //                 }
        //             } else {
        //                 server_link_cell_actions.as_mut().unwrap().set(&record_id, &field_id, &action);
        //             }
        //         } else if action.p.len() == 3 && action.p[1] == "fieldMap" {
        //             let field_id = action.p[2].clone();
        //             if let (Some(od), Some(oi)) = (action.od.clone(), action.oi.clone()) {
        //                 if od.field_type != oi.field_type {
        //                     if oi.field_type == FieldType::Link || oi.field_type == FieldType::OneWayLink {
        //                         link_field_ids.as_mut().unwrap().remove(&field_id);
        //                     } else if od.field_type == FieldType::Link || od.field_type == FieldType::OneWayLink {
        //                         link_field_ids.as_mut().unwrap().insert(field_id.clone());
        //                     }
        //                 }
        //             } else if action.oi.is_some() && (action.oi.unwrap().field_type == FieldType::Link || action.oi.unwrap().field_type == FieldType::OneWayLink) {
        //                 link_field_ids.as_mut().unwrap().remove(&field_id);
        //             } else if action.od.is_some() && (action.od.unwrap().field_type == FieldType::Link || action.od.unwrap().field_type == FieldType::OneWayLink) {
        //                 link_field_ids.as_mut().unwrap().insert(field_id.clone());
        //             }
        //         }
        //     }

        //     for field_id in link_field_ids.as_ref().unwrap().iter() {
        //         if !latest_field_ids.contains(field_id) {
        //             changed_field_ids.as_mut().unwrap().insert(field_id.clone());
        //         }
        //     }
        // }

        // let original_server_actions = server_actions.clone();
        // let new_operations: Vec<Operation> = local_changeset.operations.iter().enumerate().map(|(i, op)| {
        //     let (left_op, right_op) = jot::transform_x(&op.actions, &server_actions);
        //     server_actions = right_op;

        //     for v in left_op.iter() {
        //         if v.p.len() == 4 && v.p[3] == "columns" && v.oi.is_some() && v.od.is_some() && v.oi.unwrap().len() != v.od.unwrap().len() {
        //             panic!("Operation abnormal");
        //         }
        //     }

        //     if let Some(server_link_cell_actions) = &server_link_cell_actions {
        //         if !server_link_cell_actions.is_empty() || changed_field_ids.as_ref().unwrap().len() > 0 {
        //             let mut expected_transformed_actions: HashMap<String, Option<JOTAction>> = HashMap::new();
        //             for action in op.actions.iter() {
        //                 if action.p.len() == 4 && action.p[0] == "recordMap" && action.p[2] == "data" && link_field_ids.as_ref().unwrap().contains(&(action.p[3] as String)) {
        //                     let record_id = action.p[1].clone();
        //                     let field_id = action.p[3].clone();
        //                     let cell_id = format!("{}-{}", record_id, field_id);
        //                     if changed_field_ids.as_ref().unwrap().contains(&field_id) {
        //                         panic!("Operation conflict involving magic-link cell update");
        //                     }
        //                     if let Some(server_action) = server_link_cell_actions.get(&record_id, &field_id) {
        //                         expected_transformed_actions.insert(cell_id, transform_link_cell_action(&action, &server_action));
        //                     }
        //                 } else if action.p.len() == 3 && action.p[1] == "fieldMap" && link_field_ids.as_ref().unwrap().contains(&(action.p[2] as String)) {
        //                     let field_id = action.p[2].clone();
        //                     if server_link_cell_actions.map.get(&field_id).is_some() {
        //                         panic!("Operation conflict involving magic-link cell update");
        //                     }
        //                 }
        //             }

        //             let mut new_left_op: Vec<JOTAction> = Vec::new();
        //             for action in left_op.iter() {
        //                 if action.p.len() == 4 && action.p[0] == "recordMap" && action.p[2] == "data" && link_field_ids.as_ref().unwrap().contains(&(action.p[3] as String)) {
        //                     let cell_id = format!("{}-{}", action.p[1], action.p[3]);
        //                     if let Some(expected) = expected_transformed_actions.get(&cell_id) {
        //                         expected_transformed_actions.remove(&cell_id);
        //                         if let Some(expected) = expected {
        //                             new_left_op.push(expected.clone());
        //                         }
        //                     } else {
        //                         new_left_op.push(action.clone());
        //                     }
        //                 } else {
        //                     new_left_op.push(action.clone());
        //                 }
        //             }
        //             for action in expected_transformed_actions.values() {
        //                 if let Some(action) = action {
        //                     new_left_op.push(action.clone());
        //                 }
        //             }

        //             return Operation {
        //                 cmd: op.cmd
        //                 cmd: op.cmd,
        //                 actions: new_left_op,
        //                 main_link_dst_id: op.main_link_dst_id.clone(),
        //             };
        //         } else {
        //             return Operation {
        //                 cmd: op.cmd,
        //                 actions: left_op,
        //                 main_link_dst_id: op.main_link_dst_id.clone(),
        //             };
        //         }
        //     } else {
        //         return Operation {
        //             cmd: op.cmd,
        //             actions: left_op,
        //             main_link_dst_id: op.main_link_dst_id.clone(),
        //         };
        //     }
        // }).collect();

        RemoteChangeset {
            // local_changeset: local_changeset.clone(),
            // revision: db_revision + 1,
            // operations: new_operations,
            ..Default::default()
        }
    }

    pub async fn nest_room_change(&self, room_id: String, changesets: Vec<RemoteChangeset>,
        user_id: String
    ) {
    // ) -> anyhow::Result<()> {
        // let data = self.rel_service.get_room_change_result(room_id.clone(), changesets).await.unwrap();
        // println!("room_id = {}, data = {:?}", room_id, data);
        let json_value = to_value(changesets).unwrap();
        let str = self.loader.execute_command_with_update_records(&room_id, &user_id, json_value).await.unwrap();
        println!("str = {:?}", str);
        // self.socket_grpc_client.nest_room_change(room_id, data).await
    }
}

 