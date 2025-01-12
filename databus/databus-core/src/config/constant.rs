use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug, Clone, PartialEq)]
pub enum Role {
    Administrator,
    Manager,
    Editor,
    Reader,
    None,
    Member,
    Guest,
    Foreigner,
}