import os
from pathlib import Path

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "COMI - Comic Reader"
    database_url: str = "sqlite+aiosqlite:///./comi.db"
    library_path: str = os.path.join(str(Path.home()), "comics")
    upload_path: str = os.path.join(str(Path.home()), "comics", "uploads")
    thumbnail_path: str = os.path.join(str(Path.home()), "comics", "thumbnails")
    max_upload_size: int = 500 * 1024 * 1024  # 500MB
    supported_formats: list[str] = [".cbz", ".cbr", ".pdf"]
    cors_origins: list[str] = ["http://localhost:5173", "http://localhost:3000"]

    model_config = {"env_prefix": "COMI_"}


settings = Settings()

for path in [settings.library_path, settings.upload_path, settings.thumbnail_path]:
    os.makedirs(path, exist_ok=True)
