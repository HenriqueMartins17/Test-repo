from typing import Generator
import pytest
from fastapi.testclient import TestClient
from ai_server.app import app


@pytest.fixture(scope="module")
def test_client() -> Generator:
    """Keep using the same loop. If no, an error will `occur Attached to a different loop`"""
    with TestClient(app) as client:
        yield client
