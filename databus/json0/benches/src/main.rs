use json0::{, Operation, JSON0};
use serde_json::{json};

fn main() {
  let test_json = json!([
    {
      a: "This ia a stirng",
      b: 3.0,
      c: [ "This is c array", 1.0, 2.0, 999.0 ],
      d: {
        d1: 1.0,
        d2: "d2 string"
      }
    }
  ]);

  let operations_json = json!(
      [{"p": ["c", 0], "li": 888}, 
      {"p": ["c", 0], "ld": 8888}, 
      {"p": ["c", 0], "li": 0}]);

  let operations: Vec<Operation> = serde_json::from_value(operations_json).unwrap();

  let _ = JSON0::apply(test_json, operations);
}
