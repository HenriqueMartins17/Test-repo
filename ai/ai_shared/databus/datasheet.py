from typing import Any, List

from ..types import DatasheetPack, FieldMap, FieldType
from .record import Record


class Datasheet:
    _data_pack: DatasheetPack

    def __init__(self, data_pack: DatasheetPack):
        self._data_pack = data_pack

    def get_fields(self) -> FieldMap:
        return self._data_pack["snapshot"]["meta"]["fieldMap"]

    def get_revision(self) -> int:
        return self._data_pack["datasheet"]["revision"]

    def get_default_view_id(self):
        view = self._data_pack["snapshot"]["meta"]["views"][0]
        return view.get("id")
    
    def get_view_by_id(self, view_id: str):
        views = self._data_pack["snapshot"]["meta"]["views"]
        for view in views:
            if view["id"] == view_id:
                return view
        return None

    def get_records(self) -> List[Record]:
        meta = self._data_pack["snapshot"]["meta"]
        field_map = meta["fieldMap"]
        primary_field_id = meta["views"][0]["columns"][0]["fieldId"]
        record_map = self._data_pack["snapshot"]["recordMap"]
        result: List[Record] = []
        for record in record_map.values():
            result.append(Record(record, field_map, primary_field_id))
        return result

    def get_field_data_list_by_field_type(self, kind: FieldType) -> List[Any]:
        """
        Get FieldData in DataSheetPack by FieldType, URL, Attachments, etc.
        """
        records = self.get_records()
        if not records:
            return []

        datasheet_field_data_list = []
        for record in records:
            field_data_list = record.get_field_data_list_by_field_type(kind=kind)
            if not field_data_list:
                continue

            datasheet_field_data_list.extend(field_data_list)

        return datasheet_field_data_list

    async def to_df(self):
        records = self.get_records()
        import pandas as pd
        data = [record.to_dict() for record in records]
        df = pd.DataFrame(data=data)
        return df
