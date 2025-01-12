use crate::ai::AiDAO;
use crate::datasheet_changeset::DatasheetChangesetDAO;
use crate::datasheet_meta::DatasheetMetaDAO;
use crate::datasheet_record::DatasheetRecordDAO;
use crate::datasheet_record_comment::DatasheetRecordCommentDAOImpl;
use crate::datasheet_revision::DatasheetRevisionDAO;
use crate::node::node::INodeDAO;
use crate::resource::meta::ResourceMetaDAO;
use crate::rest::RestDAO;
use crate::types::{FetchDataPackOptions, FetchDataPackOrigin};
use crate::unit::UnitDAO;
use crate::user::UserDAO;
use crate::{automation, AutomationDAO, DBManager, DBManagerImpl, RepositoryInitOptions};
use crate::{document, DocumentManagerDAO};
use anyhow::Context;
use async_trait::async_trait;
use databus_core::prelude::DatasheetPackSO;
use databus_core::shared::AuthHeader;
use fred::prelude::*;
use std::fmt::{self, Display, Formatter};
use std::sync::Arc;

use crate::redis::RedisDAO;
use databus_shared::prelude::ResultExt;

#[async_trait]
pub trait IDataPackDAO: Send + Sync {
  async fn fetch_datasheet_pack(
    &self,
    source: &str,
    dst_id: &str,
    auth: AuthHeader,
    origin: FetchDataPackOrigin,
    options: Option<FetchDataPackOptions>,
  ) -> anyhow::Result<DatasheetPackSO>;

  async fn destroy(&self) -> anyhow::Result<()>;
}

/**
 * Dependencies Container for DAOs
 */
pub struct DAOManager {
  pub datasheet_dao: Arc<crate::database::datasheet::DatasheetDAO>,
  pub datasheet_meta_dao: Arc<dyn DatasheetMetaDAO>,
  pub repo: Arc<dyn DBManager>,
  pub record_dao: Arc<dyn DatasheetRecordDAO>,
  pub node_dao: Arc<dyn INodeDAO>,
  pub datasheet_revision_dao: Arc<dyn DatasheetRevisionDAO>,
  pub user_dao: Arc<dyn UserDAO>,
  pub unit_dao: Arc<dyn UnitDAO>,
  pub rest_dao: Arc<dyn RestDAO>,
  pub ai_dao: Arc<AiDAO>,
  pub redis_dao: Arc<dyn RedisDAO>,
  pub automation_dao: Arc<AutomationDAO>,
  pub document_manager_dao: Arc<DocumentManagerDAO>,
  pub datasheet_changeset_dao: Arc<dyn DatasheetChangesetDAO>,
}

#[derive(Debug, Clone)]
pub struct RedisOptions {
  pub username: Option<String>,
  pub password: Option<String>,
  pub host: String,
  pub port: u16,
  pub database: Option<u8>,
}

impl RedisOptions {
  fn print_sensitive_info(&self) {
    let password = match &self.password {
      Some(_) => "***",
      None => "None",
    };
    let database = match &self.database {
      Some(db) => db.to_string(),
      None => "None".to_string(),
    };

    println!(
      "RedisOptions {{ username: {:?}, password: {:?}, host: \"{}\", port: {}, database: {} }}",
      self.username, password, self.host, self.port, database
    );
  }
}

impl From<RedisOptions> for RedisConfig {
  fn from(value: RedisOptions) -> Self {
    Self {
      username: value.username,
      password: value.password,
      server: ServerConfig::Centralized {
        server: (value.host, value.port).into(),
      },
      database: value.database,
      ..Default::default()
    }
  }
}

#[derive(Debug, Clone)]
pub struct MysqlOptions {
  pub username: String,
  pub password: String,
  pub host: String,
  pub port: u16,
  pub database: String,
}

impl MysqlOptions {
  fn print_sensitive_info(&self) {
    println!(
      "MysqlOptions {{ username: \"{}\", password: \"***\", host: \"{}\", port: {}, database: \"{}\" }}",
      self.username, self.host, self.port, self.database
    );
  }
}

impl Display for MysqlOptions {
  fn fmt(&self, f: &mut Formatter) -> fmt::Result {
    write!(
      f,
      "mysql://{user}:{password}@{host}:{port}/{database}",
      user = url_escape::encode_component(&self.username),
      password = url_escape::encode_component(&self.password),
      host = self.host,
      port = self.port,
      database = self.database,
    )
  }
}

impl fmt::Display for RedisOptions {
  fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
    let mut display_str = String::new();

    if let Some(username) = &self.username {
      display_str.push_str(&format!("Username: {}\n", username));
    }

    if let Some(password) = &self.password {
      display_str.push_str(&format!("Password: {}\n", password));
    }

    display_str.push_str(&format!("Host: {}\nPort: {}\n", self.host, self.port));

    if let Some(database) = &self.database {
      display_str.push_str(&format!("Database: {}\n", database));
    }

    write!(f, "{}", display_str)
  }
}

#[derive(Debug, Clone)]
pub struct DataPackDAOOptions {
  pub redis: RedisOptions,
  pub mysql: MysqlOptions,
  pub rest_api_base_url: String,
  pub oss_host: String,
  pub table_prefix: String,
}

impl DataPackDAOOptions {
  pub fn new(rest_base_url: String) -> DataPackDAOOptions {
    let redis_option = RedisOptions {
      username: env_var!(REDIS_USERNAME),
      host: env_var!(REDIS_HOST default "127.0.0.1"),
      password: Some(env_var!(REDIS_PASSWORD default "apitable@com")),
      port: env_var!(REDIS_PORT)
        .map(|port| port.parse().expect_with(|_| format!("invalid REDIS_PORT: \"{port}\"")))
        .unwrap_or(6379),
      database: Some(0),
    };
    let mysql_option = crate::MysqlOptions {
      username: env_var!(MYSQL_USERNAME default "root"),
      password: env_var!(MYSQL_PASSWORD default "apitable@com"),
      host: env_var!(MYSQL_HOST default "localhost"),
      port: env_var!(MYSQL_PORT)
        .map(|port| port.parse().expect_with(|_| format!("invalid MYSQL_PORT: \"{port}\"")))
        .unwrap_or(3306),
      database: env_var!(MYSQL_DATABASE default "apitable"),
    };
    let dao_options = DataPackDAOOptions {
      redis: redis_option,
      mysql: mysql_option,
      rest_api_base_url: rest_base_url,
      oss_host: env_var!(OSS_HOST default ""),
      table_prefix: env_var!(DATABASE_TABLE_PREFIX default "apitable_"),
    };

    dao_options
  }
}

#[async_trait]
impl IDataPackDAO for DAOManager {
  async fn fetch_datasheet_pack(
    &self,
    source: &str,
    dst_id: &str,
    auth: AuthHeader,
    origin: FetchDataPackOrigin,
    options: Option<FetchDataPackOptions>,
  ) -> anyhow::Result<DatasheetPackSO> {
    self
      .datasheet_dao
      .fetch_data_pack(source, dst_id, auth, origin, options)
      .await
  }

  async fn destroy(&self) -> anyhow::Result<()> {
    self.repo.close().await?;
    Ok(())
  }
}
impl DAOManager {
  pub async fn new(options: DataPackDAOOptions) -> anyhow::Result<Arc<DAOManager>> {
    // databus_core::new_repository(
    let repo = DBManagerImpl::new(RepositoryInitOptions {
      conn_url: options.mysql.to_string(),
      table_prefix: options.table_prefix.clone(),
    });
    options.mysql.print_sensitive_info();
    options.redis.print_sensitive_info();

    repo.init().await.context("init repository")?;

    let datasheet_meta_dao = crate::database::datasheet_meta::new_dao(repo.clone());

    let resource_meta_dao = ResourceMetaDAO::new(repo.clone());

    let datasheet_record_comment_dao = DatasheetRecordCommentDAOImpl::new(repo.clone());

    let node_desc_dao = crate::node::description::new_dao(repo.clone());

    let redis_dao = crate::redis::new_dao(options.redis.into())
      .await
      .context("init redis")?;

    let rest_dao = crate::rest::new_dao(options.rest_api_base_url);

    let unit_dao = crate::unit::new_dao(options.oss_host.clone(), repo.clone());

    let user_dao = crate::user::new_dao(rest_dao.clone(), repo.clone(), options.oss_host.clone());

    let node_share_setting_dao = crate::node::share_setting::new_dao(repo.clone(), rest_dao.clone());

    let node_rel_repo_dao = crate::node::rel_repo::new_dao(repo.clone());

    let node_perm_dao = crate::node::permission::new_dao(
      repo.clone(),
      node_share_setting_dao.clone(),
      rest_dao.clone(),
      user_dao.clone(),
    );

    let datasheet_revision_dao = crate::database::datasheet_revision::new_dao(repo.clone());

    let node_dao = crate::node::node::new_dao(
      repo.clone(),
      resource_meta_dao.clone(),
      datasheet_revision_dao.clone(),
      node_desc_dao.clone(),
      node_perm_dao.clone(),
      node_share_setting_dao.clone(),
      node_rel_repo_dao.clone(),
    );

    let record_dao = crate::database::datasheet_record::new_dao(repo.clone(), datasheet_record_comment_dao.clone());

    let datasheet_dao = crate::database::datasheet::new_dao(
      datasheet_meta_dao.clone(),
      record_dao.clone(),
      node_dao.clone(),
      datasheet_revision_dao.clone(),
      user_dao.clone(),
      unit_dao.clone(),
      redis_dao.clone(),
      repo.clone(),
    );

    // automation dao
    let trigger_schedule_dao = automation::trigger_schedule::new_dao(repo.clone());
    let automation_dao = automation::new_dao(
      automation::run_history::new_dao(repo.clone()),
      automation::trigger::new_dao(repo.clone(), trigger_schedule_dao.clone()),
      automation::robot::new_dao(repo.clone()),
      automation::action::new_dao(repo.clone()),
      trigger_schedule_dao,
    );

    let document_manager_dao = document::new_dao(
      document::document::new_dao(repo.clone()),
      document::document_operation::new_dao(repo.clone()),
    );

    let datasheet_changeset_dao = crate::database::datasheet_changeset::new_dao(repo.clone());

    Ok(Arc::new(DAOManager {
      datasheet_meta_dao: datasheet_meta_dao.clone(),
      record_dao: record_dao.clone(),
      node_dao: node_dao.clone(),
      datasheet_revision_dao: datasheet_revision_dao.clone(),
      user_dao: user_dao.clone(),
      unit_dao: unit_dao.clone(),
      datasheet_dao: datasheet_dao.clone(),
      rest_dao: rest_dao.clone(),
      repo: repo.clone(),
      ai_dao: Arc::new(AiDAO::new(repo.clone())),
      redis_dao: redis_dao.clone(),
      automation_dao: automation_dao.clone(),
      document_manager_dao: document_manager_dao.clone(),
      datasheet_changeset_dao: datasheet_changeset_dao.clone(),
    }))
  }
}
