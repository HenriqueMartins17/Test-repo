import copy

from ai_shared.config import DEV_MODE, settings

if __name__ == "__main__":
    import uvicorn.config

    print(f"ai server config: {settings.dict()}")
    print(f"API Docs: http://{settings.aiserver_host}:{settings.aiserver_port}/ai/docs")
    print(f"API Docs: http://{settings.aiserver_host}:{settings.aiserver_port}/ai/redoc")

    log_config = copy.deepcopy(uvicorn.config.LOGGING_CONFIG)
    log_config["formatters"]["default"]["fmt"] = settings.aiserver_app_log_formatter
    uvicorn.run(
        # `<package>.<module>:<attirbute>` works for prod and dev.
        # ref: https://www.uvicorn.org/settings/#application
        "ai_server.app:app",
        host=settings.aiserver_host,
        port=settings.aiserver_port,
        log_config=log_config,
        # enable reload for dev mode to automatically hot reload.
        reload=True if DEV_MODE else False,
        proxy_headers=True,
    )
