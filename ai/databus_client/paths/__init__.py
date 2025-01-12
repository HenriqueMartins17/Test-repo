# do not import all endpoints into this module because that uses a lot of memory and stack frames
# if you need the ability to import all endpoints from this module, import them with
# from databus_client.apis.path_to_api import path_to_api

import enum


class PathValues(str, enum.Enum):
    DATABUS = "/databus"
    DATABUS_DAO_GET_AI_AI_ID = "/databus/dao/get_ai/{ai_id}"
    DATABUS_DAO_GET_AI_DATASHEET_IDS_AI_ID = "/databus/dao/get_ai_datasheet_ids/{ai_id}"
    DATABUS_DAO_GET_REVISION_DATASHEET_ID = "/databus/dao/get_revision/{datasheet_id}"
    DATABUS_GET_DATASHEET_PACK_ID = "/databus/get_datasheet_pack/{id}"
