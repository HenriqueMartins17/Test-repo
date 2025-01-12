import asyncio, os, json
from functools import wraps
from typing import AsyncGenerator, Awaitable
from ai_shared.config import settings, Env
import aiohttp

from loguru import logger
from sse_starlette import ServerSentEvent

from ai_shared.tracking import Tracking


async def wrap_done(fn: Awaitable, event: asyncio.Event):
    """Wrap an awaitable with a event to signal when it's done or an exception is raised."""
    try:
        await fn
    except Exception as e:
        Tracking.capture_exception(e)
        # TODO: handle exception when streaming error.
        error_msg = f"caught exception in streaming request: {e}"
        logger.error(error_msg)
        raise e
    finally:
        # Signal the aiter to stop.
        event.set()


def guard_stream_exception(generator_fn: AsyncGenerator) -> AsyncGenerator:
    @wraps(generator_fn)
    async def wrapper(*args, **kwargs) -> AsyncGenerator:
        try:
            async for item in generator_fn(*args, **kwargs):
                yield item
        except Exception as e:
            Tracking.capture_exception(e)
            logger.error(f"caught exception in streaming: {e}")
            import traceback
            logger.error(traceback.format_exc())
            # ref: https://github.com/sysid/sse-starlette/blob/master/examples/error_handling.py
            message = json.dumps(dict(error=f"caught unexpected error: {str(e)}"))
            yield ServerSentEvent(message)
            return

    return wrapper

async def posthog_capture(distinct_id: str, event_name: str, properties: dict | None = None):
    """
    Posthog event async tracking, used to replace SDK posthog.capture()
    """
    project_id = os.getenv("POSTHOG_PROJECT_API_KEY")
    if project_id is None:
        return

    payload = {
        'api_key': project_id,
        'event': event_name,
        'properties': properties or {},
        'distinct_id': distinct_id
    }

    payload['properties']['env'] = settings.env.value

    url = "https://eu.posthog.com/capture/"
    
    try:
        async with aiohttp.ClientSession() as session:
            if settings.env is Env.DEVELOPMENT:
                print("Sending event to PostHog:", payload)

            await session.post(url, data=json.dumps(payload))
    except Exception as e:
        if settings.env is Env.DEVELOPMENT:
            print(f"Failed to send event to PostHog: {e}")


def is_none(value):
    return value is None
