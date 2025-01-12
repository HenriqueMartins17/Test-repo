from typing import Callable

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from loguru import logger

from ai_server.copilot_api import router as copilot_router
from ai_server.inference_api import router as inference_router
from ai_server.trainer_api import router as trainers_router
from ai_shared.config import settings, Env
from ai_shared.exceptions import BaseError
from ai_shared.tracking import Tracking
from ai_shared.vos import APIResponseVO

logger.info(f"ai server config: {settings.dict()}")
Tracking.init()

app = FastAPI(
    docs_url="/ai/docs",
    openapi_url="/ai/openapi.json",
    redoc_url="/ai/redoc",
    title=settings.aiserver_app_title,
    version=settings.aiserver_app_version,
    description=settings.aiserver_app_description,
)

if settings.env == Env.DEVELOPMENT:
    """
    CORS middleware for development
    """
    from fastapi.middleware.cors import CORSMiddleware
    origins = [
        "*"
    ]

    # set CORS middleware
    app.add_middleware(
        CORSMiddleware,
        allow_origins=origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    logger.info(f"enabled CORS for origins: {origins}")

app.include_router(inference_router, prefix="/ai/inference", tags=["inference"])
app.include_router(trainers_router, prefix="/ai/trainers", tags=["trainers"])
app.include_router(copilot_router, prefix="/ai/copilot", tags=["copilot"])


@app.get("/")
def home():
    return APIResponseVO.success("VIKA AI SERVER works.")


@app.middleware("http")
async def guard_global_exceptions(request: Request, call_next: Callable):
    try:
        response = await call_next(request)
        return response

    except Exception as e:
        Tracking.capture_exception(e)
        err = f"caught exception in guard_global_exceptions: {str(e)}"
        logger.error(err)

        import traceback
        logger.error(traceback.format_exc())

        return JSONResponse(
            status_code=500,
            content=APIResponseVO.error(code=500, msg=err).dict(),
        )


@app.exception_handler(BaseError)
async def base_error_handler(request: Request, exc: BaseError):
    logger.error(f"Error raised: {str(exc)}")
    Tracking.capture_exception(exc)
    return JSONResponse(
        status_code=exc.status_code,
        content=APIResponseVO.error(code=exc.code, msg=str(exc), data=exc.data).dict(),
    )
