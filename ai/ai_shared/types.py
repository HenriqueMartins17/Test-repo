from enum import Enum
from typing import Any, Dict, List, TypedDict


class Field(TypedDict):
    id: str
    name: str
    type: int


FieldMap = Dict[str, Field]


class ViewColumn(TypedDict):
    fieldId: str


class ViewProperty(TypedDict):
    id: str
    name: str
    type: int
    columns: List[ViewColumn]


class DatasheetMeta(TypedDict):
    fieldMap: FieldMap
    views: List[ViewProperty]


class Record(TypedDict):
    id: str
    data: Dict[str, Any]
    createdAt: int
    updatedAt: int


RecordMap = Dict[str, Record]


class DatasheetSnapshot(TypedDict):
    meta: DatasheetMeta
    recordMap: RecordMap
    datasheetId: str


class NodeInfo(TypedDict):
    id: str
    name: str
    description: str
    revision: int


class DatasheetPack(TypedDict):
    snapshot: DatasheetSnapshot
    datasheet: NodeInfo


class FieldType(Enum):
    NotSupport = 0
    Text = 1
    Number = 2
    SingleSelect = 3
    MultiSelect = 4
    DateTime = 5
    Attachment = 6
    Link = 7
    URL = 8
    Email = 9
    Phone = 10
    Checkbox = 11
    Rating = 12
    Member = 13
    LookUp = 14
    # RollUp = 15
    Formula = 16
    Currency = 17
    Percent = 18
    SingleText = 19
    AutoNumber = 20
    CreatedTime = 21
    LastModifiedTime = 22
    CreatedBy = 23
    LastModifiedBy = 24
    Cascader = 25
    OneWayLink = 26
    WorkDoc = 27
    Button = 28
    # no permission column
    DeniedField = 999
