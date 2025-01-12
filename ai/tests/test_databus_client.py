# import requests
# from unittest import mock
# from ai_server.shared.types import DatasheetPack, DatasheetSnapshot

# # Mocking the requests.get method
# # Mocking the requests.get method
# def mocked_requests_get(*args, **kwargs):
#     class MockResponse:
#         def __init__(self, json_data, status_code):
#             self.json_data = json_data
#             self.status_code = status_code

#         def json(self):
#             return self.json_data

#         def raise_for_status(self):
#             if self.status_code not in [200, 404]:
#                 raise requests.exceptions.HTTPError("Mocked HTTPError")

#     if "databus/get_datasheet_pack/dst1" in args[0]:
#         datasheet_pack_data = {
#             "snapshot": {
#                 "meta": {
#                     "fieldMap": {
#                         "fld1": {"id": "fld1", "name": "Field 1", "type": 1},
#                         "fld2": {"id": "fld2", "name": "Field 2", "type": 2},
#                     },
#                     "views": [
#                         {
#                             "id": "viw1",
#                             "name": "View 1",
#                             "type": 1,
#                             "columns": [{"fieldId": "fld1"}, {"fieldId": "fld2"}],
#                         }
#                     ],
#                 },
#                 "recordMap": {
#                     "rec1": {"id": "rec1", "data": {"fld1": "value1", "fld2": "value2"}, "createdAt": 1, "updatedAt": 2},
#                     "rec2": {"id": "rec2", "data": {"fld1": "value3", "fld2": "value4"}, "createdAt": 3, "updatedAt": 4},
#                 },
#                 "datasheetId": "dst1",
#             },
#             "datasheet": {"id": "dst1", "name": "My Datasheet", "description": "Description of the datasheet"},
#         }
#         return MockResponse({"code": 200, "data": datasheet_pack_data}, 200)
#     else:
#         return MockResponse({"code": 404, "data": ""}, 200)

# class TestDatabusServerAPI:
#     @mock.patch("requests.get", side_effect=mocked_requests_get)
#     def test_get_datasheet_pack(self, mock_get):
#         base_url = "https://example.com/api"
#         databus_api = DatabusServerAPI(base_url)

#         # Test a successful response
#         datasheet_id = "dst1"
#         result = databus_api.get_datasheet_pack(datasheet_id)
#         expected_result = DatasheetPack(
#             snapshot=DatasheetSnapshot(
#                 meta={
#                     "fieldMap": {
#                         "fld1": {"id": "fld1", "name": "Field 1", "type": 1},
#                         "fld2": {"id": "fld2", "name": "Field 2", "type": 2},
#                     },
#                     "views": [
#                         {
#                             "id": "viw1",
#                             "name": "View 1",
#                             "type": 1,
#                             "columns": [{"fieldId": "fld1"}, {"fieldId": "fld2"}],
#                         }
#                     ],
#                 },
#                 recordMap={
#                     "rec1": {"id": "rec1", "data": {"fld1": "value1", "fld2": "value2"}, "createdAt": 1, "updatedAt": 2},
#                     "rec2": {"id": "rec2", "data": {"fld1": "value3", "fld2": "value4"}, "createdAt": 3, "updatedAt": 4},
#                 },
#                 datasheetId="dst1",
#             ),
#             datasheet={"id": "dst1", "name": "My Datasheet", "description": "Description of the datasheet"},
#         )
#         assert result == expected_result

#         # Test an HTTP error response
#         # dst2 didn't exist.
#         datasheet_id = "dst2"
#         result = databus_api.get_datasheet_pack(datasheet_id)
#         assert result is None
