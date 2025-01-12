# do not import all endpoints into this module because that uses a lot of memory and stack frames
# if you need the ability to import all endpoints from this module, import them with
# from databus_client.paths.databus_get_datasheet_pack_id import Api

from databus_client.paths import PathValues

path = PathValues.DATABUS_GET_DATASHEET_PACK_ID