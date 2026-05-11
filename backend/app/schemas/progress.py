from datetime import datetime

from pydantic import BaseModel, Field


class ProgressUpdate(BaseModel):
    current_page: int = Field(..., ge=0)


class ProgressResponse(BaseModel):
    id: int
    comic_id: int
    current_page: int
    total_pages: int
    percentage: float
    last_read_at: datetime | None = None
    started_at: datetime | None = None
    finished_at: datetime | None = None

    model_config = {"from_attributes": True}


class BookmarkCreate(BaseModel):
    page_number: int = Field(..., ge=0)
    label: str | None = Field(None, max_length=200)
    note: str | None = None


class BookmarkResponse(BaseModel):
    id: int
    comic_id: int
    page_number: int
    label: str | None = None
    note: str | None = None
    created_at: datetime

    model_config = {"from_attributes": True}


class ReadingHistoryEntry(BaseModel):
    comic_id: int
    comic_title: str
    current_page: int
    total_pages: int
    percentage: float
    last_read_at: datetime | None = None
    cover_path: str | None = None
