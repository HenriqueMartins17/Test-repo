from typing import Any

from loguru import logger

from ai_shared.config import settings


class SendTask:
    """todo: Fake task now, change to send real task later"""

    @staticmethod
    def send_add():
        from ai_server.tasks import Tasks
        if settings.is_dev_mode():
            return Tasks.add(1, 2)
        # todo: real task
        # return Tasks.add.delay(1, 2)
        return Tasks.add(1, 2)

    @staticmethod
    def send_load_url(url_data: Any):
        logger.debug(f"SendTask.send_load_url: {url_data=}")
        from ai_server.tasks import Tasks
        return Tasks.load_url(url_data)

    @staticmethod
    def send_load_attachment(attach_data: Any):
        logger.debug(f"SendTask.send_load_attachment: {attach_data=}")
        from ai_server.tasks import Tasks
        return Tasks.load_attachment(attach_data)
