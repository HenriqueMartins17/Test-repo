import json
from typing import Any, List

from .. import types
from ..types import FieldType
from ai_shared.config import settings
from urllib.parse import quote
from datetime import datetime, timezone

def cell_value_to_str(value: Any, field: types.Field) -> str:
    typ = FieldType(field["type"])

    if typ in [FieldType.SingleText, FieldType.Text, FieldType.Cascader, FieldType.Email]:
        return "".join([segment["text"] for segment in value])
    elif typ == FieldType.SingleSelect or typ == FieldType.MultiSelect:
        return select_to_str(value, field)
    elif typ == FieldType.Attachment:
        return attachments_to_str(value, field)
    elif typ == FieldType.DateTime:
        return datetime_to_str(value, field)
    elif typ == FieldType.URL:
        return url_to_str(value, field)
    elif typ == FieldType.Rating:
        return rating_to_str(value, field)
    elif typ == FieldType.Currency:
        return currency_to_str(value, field)
    else:
        return str(value)

def select_to_str(value: Any, field: types.Field) -> str:
    """
    SingleSelect field value to string
    """

    if not value:
        return ""

    option_ids = value
    select_options = field.get("property", {}).get("options", {})
    result = []
    
    # Determine whether value is str, if it is str, convert it to list
    if isinstance(value, str):
        option_ids = [value]

    for option in select_options:
        if option["id"] in option_ids:
            result.append(option["name"])
        
    return ",".join(result)

def attachments_to_str(value: list, field: types.Field) -> str:
    """
    Attachments field value to string
    """

    if not value:
        return ""

    result = []
    for attachment in value:
        encoded_name = quote(attachment["name"])
        attachment_url = f"({attachment['name']})[{settings.assets_url}/{attachment['token']}?attname={encoded_name}]"

        if attachment["mimeType"].startswith("image/"):
            attachment_url = f"!({attachment['name']})[{settings.assets_url}/{attachment['token']}]"
        
        result.append(attachment_url)
    return ",".join(result)

def datetime_to_str(value: Any, field: types.Field) -> str:
    """
    DateTime field value to string
    TODO: use local datetime string
    TODO: use datetime format from field
    """
    if value == None or value == "":
        return ""
    
    timestamp_s = value / 1000.0
    include_time = field.get("property", {}).get("includeTime", False)
    str_format = "%Y-%m-%d %H:%M:%S %Z %z" if include_time else "%Y-%m-%d %H:%M %Z %z"

    dt_object = datetime.fromtimestamp(timestamp_s, tz=timezone.utc)
    formatted_date = dt_object.strftime(str_format)

    return formatted_date

def url_to_str(value: Any, field: types.Field) -> str:
    """
    URL field value to string
    """
    if not value:
        return ""
    
    url = value[0]
    url_text = url.get("text", "")
    url_title = url.get("title", url_text)
    return f"({url_title})[{url_text}]"

def rating_to_str(value: Any, field: types.Field) -> str:
    """
    Rating field value to string
    """
    if not value:
        return ""
    
    property = field.get("property", {})
    icon = property.get("icon", "star")
    max_value = property.get("max", 5)

    return f"{value} of {max_value} {icon}s"

def currency_to_str(value: Any, field: types.Field) -> str:
    """
    Currency field value to string
    symbol
    """
    if not value:
        return ""
    
    property = field.get("property", {})
    precision = property.get("precision", 2)
    symbol = property.get("symbol", "$")

    format_string = "{:." + str(precision) + "f}"
    return symbol + format_string.format(value)

class Record:
    id: str
    _record: types.Record
    _field_map: types.FieldMap
    _primary_field_id: str

    def __init__(
        self, record: types.Record, field_map: types.FieldMap, primary_field_id: str
    ) -> None:
        self.id = record["id"]
        self._record = record
        self._field_map = field_map
        self._primary_field_id = primary_field_id

    def __str__(self) -> str:
        values = {}
        if "data" in self._record:
            for id, value in self._record["data"].items():
                if id in self._field_map:
                    field = self._field_map[id]
                    field_name = field["name"]
                    value = cell_value_to_str(value, field)
                    values[field_name.strip()] = value.strip()
        # values.append('recordId:' + self.id)
        # TODO local datetime string, use UTC time later
        # values.append('createdTime:' + self._record['created_at'].isoformat())
        # return "\n".join(values)
        return json.dumps(values, ensure_ascii=False)

    def to_string(self, skip_field_types: list[FieldType] | None = None, sort_field_ids: list[str] | None = []) -> str:
        """
        Convert record to string
        param skip_field_types: skip field types
        param sort_by_field_id: sort field ids
        """

        values = {}

        if "data" in self._record:
            record_data = self._record["data"]

            # Re-sort the fields of the record according to the order of sort_field_ids
            sorted_record_data = {field_id: record_data[field_id] for field_id in sort_field_ids if field_id in record_data}

            for id, value in sorted_record_data.items():
                if id in self._field_map:
                    field = self._field_map[id]

                    if skip_field_types and FieldType(field["type"]) in skip_field_types:
                        continue

                    field_name = field["name"]
                    value = cell_value_to_str(value, field)
                    values[field_name.strip()] = value.strip()
        # values.append('recordId:' + self.id)
        # TODO local datetime string, use UTC time later
        # values.append('createdTime:' + self._record['created_at'].isoformat())
        # return "\n".join(values)
        return "\n".join([":".join([k, v]) for k, v in values.items()])

    def to_dict(self) -> dict:
        values = {}
        if "data" in self._record:
            for id, value in self._record["data"].items():
                if id in self._field_map:
                    field = self._field_map[id]
                    field_name = field["name"]
                    value = cell_value_to_str(value, field)
                    values[field_name.strip()] = value.strip()
        return values

    def primary_key(self) -> str:
        if "data" in self._record:
            fields = self._record["data"]
            if self._primary_field_id in fields:
                return cell_value_to_str(
                    fields[self._primary_field_id],
                    self._field_map[self._primary_field_id],
                )
        return ""

    def get_field_ids_by_field_type(self, kind: FieldType) -> tuple | tuple[str, ...]:
        field_ids = []
        for field_id, field_meta in self._field_map.items():
            field_type = FieldType(field_meta.get("type"))
            if field_type == kind:
                field_ids.append(field_id)
        return tuple(field_ids)

    def get_field_data_list_by_field_ids(self, field_ids: tuple[str]) -> List[Any]:
        if not field_ids:
            return []

        field_data_list = []
        record_data = self._record.get("data", {})
        for field_id, field_data in record_data.items():
            if field_id in field_ids:
                field_data_list.append(field_data)
        return field_data_list

    def get_field_data_list_by_field_type(self, kind: FieldType) -> List[Any]:
        field_ids = self.get_field_ids_by_field_type(kind=kind)
        if not field_ids:
            return []

        field_data_list = self.get_field_data_list_by_field_ids(field_ids=field_ids)
        return field_data_list
