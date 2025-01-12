import typing_extensions

from databus_client.apis.tags import TagValues
from databus_client.apis.tags.crate_api import CrateApi
from databus_client.apis.tags.data_dao_api_api import DataDaoApiApi
from databus_client.apis.tags.data_services_api_api import DataServicesApiApi

TagToApi = typing_extensions.TypedDict(
    'TagToApi',
    {
        TagValues.CRATE: CrateApi,
        TagValues.DATA_DAO_API: DataDaoApiApi,
        TagValues.DATA_SERVICES_API: DataServicesApiApi,
    }
)

tag_to_api = TagToApi(
    {
        TagValues.CRATE: CrateApi,
        TagValues.DATA_DAO_API: DataDaoApiApi,
        TagValues.DATA_SERVICES_API: DataServicesApiApi,
    }
)
