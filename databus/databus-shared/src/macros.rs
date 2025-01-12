#[macro_export]
macro_rules! replace_expr {
  ($_t:tt $sub:expr) => {
    $sub
  };
}

// https://danielkeep.github.io/tlborm/book/blk-counting.html#slice-length
#[macro_export]
macro_rules! count_tts {
    ($($tts:tt)*) => {<[()]>::len(&[$(replace_expr!($tts ())),*])};
}

/// Gets the `String` value of an environment variable.
///
/// # Usage
///
/// `env_var!(VAR)` returns the string value of the environment variable `VAR` if it exists and is not empty,
/// otherwise `None` is returned.
///
/// `env_var!(VAR default EXPR)` returns the string value of `VAR` if exists and is not empty,
/// otherwise the result of the expression `EXPR` is returned.
/// The result of `EXPR` can be any type that implements `Into<String>`.
#[macro_export]
#[doc(hidden)]
macro_rules! env_var {
  ($name:ident) => {
    std::env::var(stringify!($name))
      .ok()
      .and_then($crate::prelude::ContainerExt::into_none_if_empty)
  };
  ($name:ident default $value:expr) => {
    std::env::var(stringify!($name))
      .ok()
      .and_then($crate::prelude::ContainerExt::into_none_if_empty)
      .unwrap_or_else(|| $value.into())
  };
}

/// Creates a `HashSet` from a list of elements.
#[macro_export]
macro_rules! hashset {
  () => {
    $crate::prelude::HashSet::default()
  };
  ($($elem:expr),+ $(,)?) => {{
    let mut set = $crate::prelude::HashSet::with_capacity_and_hasher(
      count_tts!($(($elem))*),
      $crate::prelude::BuildSeaHasher);
    $(
      set.insert($elem);
    )*
    set
  }};
}

/// Creates a `HashMap` from a list of key-value pairs.
#[macro_export]
#[allow(unused)]
#[doc(hidden)]
macro_rules! hashmap {
  () => {
    $crate::prelude::HashMapExt::default()
  };
  ($($key:expr => $value:expr),+ $(,)?) => {{
    let mut map = $crate::prelude::HashMapExt(
      std::collections::HashMap::with_capacity_and_hasher(
      count_tts!($(($key))*),
      $crate::prelude::BuildSeaHasher));
    $(
      map.insert($key, $value);
    )*
    map
  }};
}


#[macro_export]
#[allow(unused)]
#[doc(hidden)]
macro_rules! hashmap_standard {
    () => {
        std::collections::HashMap::new()
    };
    ($($key:expr => $value:expr),+ $(,)?) => {
        {
            let mut map = std::collections::HashMap::with_capacity(
      count_tts!($(($key))*));
            $(
                map.insert($key, $value);
            )*
            map
        }
    };
}


/// Creates a `BTreeMap` from a list of key-value pairs.
#[allow(unused)]
macro_rules! btreemap {
  () => {
    std::collections::BTreeMap::default()
  };
  ($($key:expr => $value:expr),+ $(,)?) => {{
    let mut map = std::collections::BTreeMap::new();
    $(
      map.insert($key, $value);
    )*
    map
  }};
}
