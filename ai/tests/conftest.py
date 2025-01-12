# # In pytest, `conftest.py` usually is used to as an injection system to pre-create some objects for testing
# # like database connector, web app instance, etc.

# import pytest
# from fastapi.testclient import TestClient
# from ai_server.app import app


# @pytest.fixture
# def fastapi_client():
#     test_client = TestClient(app)
#     yield test_client
