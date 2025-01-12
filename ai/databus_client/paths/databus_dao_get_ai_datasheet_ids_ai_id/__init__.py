# do not import all endpoints into this module because that uses a lot of memory and stack frames
# if you need the ability to import all endpoints from this module, import them with
# from databus_client.paths.databus_dao_get_ai_datasheet_ids_ai_id import Api

from databus_client.paths import PathValues

path = PathValues.DATABUS_DAO_GET_AI_DATASHEET_IDS_AI_ID