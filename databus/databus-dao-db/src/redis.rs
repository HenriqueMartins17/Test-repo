use async_trait::async_trait;
use fred::prelude::*;
use fred::types::MultipleValues;
use std::sync::Arc;

pub struct IntoMultipleValues<I>(pub I);

impl<I, T> TryInto<MultipleValues> for IntoMultipleValues<I>
where
  I: IntoIterator<Item = T>,
  T: Into<RedisValue>,
  MultipleValues: FromIterator<T>,
{
  type Error = RedisError;

  fn try_into(self) -> Result<MultipleValues, Self::Error> {
    Ok(FromIterator::from_iter(self.0))
  }
}

#[async_trait]
pub trait RedisDAO: Send + Sync {
  async fn get_connection(&self) -> anyhow::Result<Arc<RedisClient>>;
}

struct RedisDAOImpl {
  client: Arc<RedisClient>,
}

pub async fn new_dao(config: RedisConfig) -> anyhow::Result<Arc<dyn RedisDAO>> {
  let client = RedisClient::new(config, None, None);
  client.connect();
  client.wait_for_connect().await?;
  Ok(Arc::new(RedisDAOImpl {
    client: Arc::new(client),
  }))
}

#[async_trait]
impl RedisDAO for RedisDAOImpl {
  async fn get_connection(&self) -> anyhow::Result<Arc<RedisClient>> {
    Ok(self.client.clone())
  }
}

#[cfg(test)]
pub mod mock {
  use std::collections::HashMap;
  use super::*;
  // use crate::hashmap;
  use databus_shared::prelude::{HashSet};
  use fred::mocks::{MockCommand, Mocks};
  use std::ops::Deref;
  use std::sync::Mutex;

  #[derive(Debug, Clone, PartialEq, Eq)]
  pub enum MockValue {
    #[allow(unused)]
    Str(String),
    Set(HashSet<String>),
  }

  #[derive(Debug)]
  pub struct MockRedis {
    logs: Mutex<Vec<MockCommand>>,
    store: Mutex<HashMap<String, MockValue>>,
  }

  impl MockRedis {
    pub fn new() -> Self {
      Self {
        logs: Mutex::new(vec![]),
        store: Mutex::new(hashmap_standard! {}),
      }
    }

    pub fn with_store(mut self, store: HashMap<String, MockValue>) -> Self {
      self.store = Mutex::new(store);
      self
    }

    pub fn take_logs(&self) -> Vec<MockCommand> {
      std::mem::take(&mut *self.logs.lock().unwrap())
    }

    pub fn take_store(&self) -> HashMap<String, MockValue> {
      std::mem::take(&mut *self.store.lock().unwrap())
    }
  }

  impl Mocks for MockRedis {
    fn process_command(&self, command: MockCommand) -> RedisResult<RedisValue> {
      let mut store = self.store.lock().unwrap();

      /// Ensures that the value corresponding to a key is of a given type, if the value does not exist,
      /// the default value is inserted.
      macro_rules! ensure_arity {
        ($n:literal) => {{
          if command.args.len() != $n {
            return Err(RedisError::new(
              RedisErrorKind::InvalidArgument,
              format!(
                "arity mismatch for {}, expected {}, found {}",
                command.cmd,
                $n,
                command.args.len()
              ),
            ));
          }
        }};
        ($n:literal ..) => {{
          if command.args.len() < $n {
            return Err(RedisError::new(
              RedisErrorKind::InvalidArgument,
              format!(
                "arity mismatch for {}, expected at least {}, found {}",
                command.cmd,
                $n,
                command.args.len()
              ),
            ));
          }
        }};
      }

      /// Ensures that the value corresponding to a key is of a given type, if the value does not exist,
      /// the default value is inserted.
      macro_rules! ensure_arg {
        ($idx:tt is $ty:path) => {{
          match command
            .args
            .get($idx)
            .ok_or_else(|| RedisError::new(RedisErrorKind::InvalidArgument, "missing argument"))?
          {
            $ty(arg) => arg,
            arg => {
              return Err(RedisError::new(
                RedisErrorKind::InvalidArgument,
                format!("type mismatch, found {:?}", arg),
              ))
            }
          }
        }};
      }

      /// Ensures that the value corresponding to a key is of a given type, if the value does not exist,
      /// the default value is inserted.
      macro_rules! ensure_value {
        ($key:tt is $ty:path) => {{
          if let $ty(val) = store
            .entry(String::from_utf8($key.to_vec()).unwrap())
            .or_insert_with(|| $ty(Default::default()))
          {
            val
          } else {
            return Err(RedisError::new(RedisErrorKind::InvalidCommand, "not applicable"));
          }
        }};
      }

      self.logs.lock().unwrap().push(command.clone());
      match command.cmd.deref() {
        "GET" => {
          ensure_arity!(1);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          let str = ensure_value!(key is MockValue::Str);
          Ok(RedisValue::String(str.clone().into()))
        }
        "EXPIRE" => {
          ensure_arity!(2);
          Ok(RedisValue::Integer(1))
        }
        "SCARD" => {
          ensure_arity!(1);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          let set = ensure_value!(key is MockValue::Set);
          Ok(RedisValue::Integer(set.len() as _))
        }
        "SISMEMBER" => {
          ensure_arity!(2);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          let set = ensure_value!(key is MockValue::Set);
          let member = ensure_arg!(1 is RedisValue::String);
          Ok(RedisValue::Boolean(set.contains(member.deref())))
        }
        "SMEMBERS" => {
          ensure_arity!(1);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          let set = ensure_value!(key is MockValue::Set);
          Ok(RedisValue::Array(
            set.iter().map(|member| RedisValue::String(member.into())).collect(),
          ))
        }
        "SADD" => {
          ensure_arity!(1..);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          let set = ensure_value!(key is MockValue::Set);
          let members = (1..command.args.len())
            .map(|idx| Ok(ensure_arg!(idx is RedisValue::String).deref().to_owned()))
            .collect::<RedisResult<Vec<_>>>()?;
          set.extend(members);
          Ok(RedisValue::Integer(set.len() as _))
        }
        "DEL" => {
          ensure_arity!(1);
          let key = ensure_arg!(0 is RedisValue::Bytes);
          store.remove(std::str::from_utf8(key.as_ref()).unwrap());
          Ok(RedisValue::Integer(1))
        }
        _ => {
          panic!("unsupported command: {}, args: {:?}", command.cmd, command.args)
        }
      }
    }
  }
}
