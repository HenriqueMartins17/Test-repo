# do not import all endpoints into this module because that uses a lot of memory and stack frames
# if you need the ability to import all endpoints from this module, import them with
# from databus_client.apis.tag_to_api import tag_to_api

import enum


class TagValues(str, enum.Enum):
    CRATE = "crate"
    DATA_DAO_API = "data_dao_api"
    DATA_SERVICES_API = "data_services_api"
