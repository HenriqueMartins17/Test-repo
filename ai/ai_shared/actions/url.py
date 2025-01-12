from .base import ServerRequestAction


class OpenUrlAction(ServerRequestAction):
    """Open URL, just name=url is ok"""

    name = "url"
    id: str = ""

    def call(self, parameters):
        # ui.showUrl()
        pass
