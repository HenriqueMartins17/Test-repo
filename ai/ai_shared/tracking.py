import sentry_sdk
from sentry_sdk.integrations.fastapi import FastApiIntegration

from ai_shared.config import settings


class Tracking:
    @staticmethod
    def init():
        sentry_sdk.init(
            dsn=settings.sentry_dsn,
            integrations=[FastApiIntegration()],
            environment=settings.env.value,
            send_default_pii=True,
            release=settings.aiserver_app_version,
            # Set traces_sample_rate to 1.0 to capture 100%
            # of transactions for performance monitoring.
            # We recommend adjusting this value in production,
            traces_sample_rate=1.0,
        )

    @staticmethod
    def capture_exception(e):
        sentry_sdk.capture_exception(e)
