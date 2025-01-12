import typing_extensions

from databus_client.paths import PathValues
from databus_client.apis.paths.databus import Databus
from databus_client.apis.paths.databus_dao_get_ai_ai_id import DatabusDaoGetAiAiId
from databus_client.apis.paths.databus_dao_get_ai_datasheet_ids_ai_id import DatabusDaoGetAiDatasheetIdsAiId
from databus_client.apis.paths.databus_dao_get_revision_datasheet_id import DatabusDaoGetRevisionDatasheetId
from databus_client.apis.paths.databus_get_datasheet_pack_id import DatabusGetDatasheetPackId

PathToApi = typing_extensions.TypedDict(
    'PathToApi',
    {
        PathValues.DATABUS: Databus,
        PathValues.DATABUS_DAO_GET_AI_AI_ID: DatabusDaoGetAiAiId,
        PathValues.DATABUS_DAO_GET_AI_DATASHEET_IDS_AI_ID: DatabusDaoGetAiDatasheetIdsAiId,
        PathValues.DATABUS_DAO_GET_REVISION_DATASHEET_ID: DatabusDaoGetRevisionDatasheetId,
        PathValues.DATABUS_GET_DATASHEET_PACK_ID: DatabusGetDatasheetPackId,
    }
)

path_to_api = PathToApi(
    {
        PathValues.DATABUS: Databus,
        PathValues.DATABUS_DAO_GET_AI_AI_ID: DatabusDaoGetAiAiId,
        PathValues.DATABUS_DAO_GET_AI_DATASHEET_IDS_AI_ID: DatabusDaoGetAiDatasheetIdsAiId,
        PathValues.DATABUS_DAO_GET_REVISION_DATASHEET_ID: DatabusDaoGetRevisionDatasheetId,
        PathValues.DATABUS_GET_DATASHEET_PACK_ID: DatabusGetDatasheetPackId,
    }
)
