import traceback
from typing import Any, Union

import httpx
from loguru import logger

from ai_shared.tracking import Tracking

__all__ = [
    "options",
    "head",
    "get",
    "post",
    "put",
    "delete",
    "request",
    "make_url",
]


def make_url(base_url, path):
    if base_url.endswith("/"):
        base_url = base_url.rstrip("/")

    if path.startswith("/"):
        path = path.lstrip("/")

    return f"{base_url}/{path}"


# {"code": 200, "data": {}, "message": "ok", "success": true}
# {"code": 404, "data": null, "message": "Not Found", "success": false}
# {"code": 500, "data": null, "message": "Internal Server Error", "success": false}
def request(method: str, url: str, **kwargs) -> Union[Any, None]:
    timeout = kwargs.pop("timeout", 10)
    kwargs["timeout"] = timeout

    try:
        response = httpx.request(method, url, **kwargs)
        if response.status_code != 200:
            response.raise_for_status()

        res = response.json()
    except Exception as e:
        Tracking.capture_exception(e)
        logger.error(f"http request error: {e=}, {method=}, {url=}, {kwargs=}")
        logger.error(traceback.format_exc())
        return {}
    else:
        return res


def options(url: str, **kwargs):
    res = request("OPTIONS", url, **kwargs)
    return res


def head(url: str, **kwargs):
    res = request("HEAD", url, **kwargs)
    return res


def get(url: str, params: dict = None, json: dict = None, **kwargs):
    res = request("GET", url, params=params, json=json, **kwargs)
    return res


def post(url: str, data: dict = None, json: dict = None, **kwargs):
    res = request("POST", url, data=data, json=json, **kwargs)
    return res


def put(url: str, data: dict = None, json: dict = None, **kwargs):
    res = request("PUT", url, data=data, json=json, **kwargs)
    return res


def patch(url: str, data: dict = None, json: dict = None, **kwargs):
    res = request("PATCH", url, data=data, json=json, **kwargs)
    return res


def delete(url: str, params: dict = None, **kwargs):
    res = request("DELETE", url, params=params, **kwargs)
    return res
