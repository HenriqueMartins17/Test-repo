import datetime
import os

from ai_shared.config import settings


class Persist:
    """
    Helper functions for persisting data to disk
    """

    @staticmethod
    def ensure_directory_exists(file_path: str) -> None:
        """Ensure the directory path exists before use a file_path to read or write"""
        directory_path = os.path.dirname(file_path)
        if not os.path.exists(directory_path):
            os.makedirs(directory_path, exist_ok=True)

    @staticmethod
    def get_root_path() -> str:
        """Get the absolute persistent data root directory path

        Returns:
            str: the persistent data root
        """
        return str(settings.persistent_data_root)
