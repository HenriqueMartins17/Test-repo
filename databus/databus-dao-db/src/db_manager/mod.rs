use async_trait::async_trait;
use chrono::Local;
use mysql_async::{prelude::Queryable, Pool as DbConnPool};
use std::sync::Arc;
use time::{PrimitiveDateTime, UtcOffset};

pub use client::*;

pub mod client;

#[async_trait]
pub trait DBManager: Send + Sync {
  async fn get_client(&self) -> anyhow::Result<client::DbClient>;

  async fn init(&self) -> anyhow::Result<()>;

  async fn close(&self) -> anyhow::Result<()>;

  fn table_prefix(&self) -> &str;

  fn utc_timestamp(&self, date_time: PrimitiveDateTime) -> i64;

  #[cfg(test)]
  async fn take_logs(&self) -> Vec<mock::MockSqlLog>;
}

pub struct DBManagerImpl {
  pool: DbConnPool,
  table_prefix: String,
  tz_offset: UtcOffset,
}

#[derive(Debug, Clone, Default)]
pub struct RepositoryInitOptions {
  pub conn_url: String,
  pub table_prefix: String,
}

impl DBManagerImpl {
  pub fn new(options: RepositoryInitOptions) -> Arc<dyn DBManager> {
    Arc::new(DBManagerImpl {
      pool: DbConnPool::new(options.conn_url.as_str()),
      table_prefix: options.table_prefix,
      tz_offset: UtcOffset::from_whole_seconds(Local::now().offset().local_minus_utc()).unwrap(),
    })
  }
}

#[async_trait]
impl DBManager for DBManagerImpl {
  async fn get_client(&self) -> anyhow::Result<client::DbClient> {
    let conn = self.pool.get_conn().await?;
    Ok(client::DbClient {
      inner: client::DbClientInner::Mysql(conn),
    })
  }

  async fn init(&self) -> anyhow::Result<()> {
    // make sure the connection options are valid.
    let mut conn = self.pool.get_conn().await?;
    conn.ping().await?;
    Ok(())
  }

  async fn close(&self) -> anyhow::Result<()> {
    self.pool.clone().disconnect().await?;
    Ok(())
  }

  fn table_prefix(&self) -> &str {
    &self.table_prefix
  }

  fn utc_timestamp(&self, date_time: PrimitiveDateTime) -> i64 {
    let date_time = date_time.assume_offset(self.tz_offset);
    date_time.unix_timestamp() * 1000 + date_time.millisecond() as i64
  }

  #[cfg(test)]
  async fn take_logs(&self) -> Vec<mock::MockSqlLog> {
    unreachable!()
  }
}

#[cfg(test)]
pub mod mock {
  use super::*;
  use mysql_async::{consts::ColumnType, Column};
  use mysql_async::{Params, Row};
  use mysql_common::row::new_row;
  use mysql_common::value::Value;
  use std::sync::Arc;
  use tokio::sync::Mutex;

  #[derive(Debug, Clone)]
  pub struct MockDb {
    pub(super) logs: Vec<MockSqlLog>,
    pub(super) results: Vec<Vec<Row>>,
  }

  #[cfg(test)]
  #[derive(Debug, Clone, PartialEq)]
  pub struct MockSqlLog {
    pub sql: String,
    pub params: Params,
  }

  pub struct MockRepositoryImpl {
    mock_db: Arc<Mutex<MockDb>>,
  }

  impl MockRepositoryImpl {
    pub fn new<I>(mock_results: I) -> Arc<dyn DBManager>
    where
      I: IntoIterator<Item = Vec<Row>>,
    {
      Arc::new(Self {
        mock_db: Arc::new(Mutex::new(MockDb {
          logs: vec![],
          results: mock_results.into_iter().collect(),
        })),
      })
    }
  }

  #[async_trait]
  impl DBManager for MockRepositoryImpl {
    async fn get_client(&self) -> anyhow::Result<client::DbClient> {
      Ok(client::DbClient {
        inner: client::DbClientInner::Mock(self.mock_db.clone()),
      })
    }

    async fn init(&self) -> anyhow::Result<()> {
      Ok(())
    }

    async fn close(&self) -> anyhow::Result<()> {
      Ok(())
    }

    fn table_prefix(&self) -> &str {
      "apitable_"
    }

    fn utc_timestamp(&self, date_time: PrimitiveDateTime) -> i64 {
      let date_time = date_time.assume_utc();
      date_time.unix_timestamp() * 1000 + date_time.millisecond() as i64
    }

    async fn take_logs(&self) -> Vec<MockSqlLog> {
      let mut mock_db = self.mock_db.lock().await;
      std::mem::take(&mut mock_db.logs)
    }
  }

  pub fn mock_rows<I, II, J>(columns: J, rows: II) -> Vec<Row>
  where
    II: IntoIterator<Item = I>,
    I: IntoIterator<Item = Value>,
    J: IntoIterator<Item = (&'static str, ColumnType)>,
  {
    let columns: Arc<[_]> = columns
      .into_iter()
      .map(|(name, ty)| Column::new(ty).with_name(name.as_bytes()))
      .collect();
    rows
      .into_iter()
      .map(|i| new_row(i.into_iter().collect(), columns.clone()))
      .collect()
  }
}
