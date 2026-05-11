from datetime import datetime

from pydantic import BaseModel, Field


class TagSchema(BaseModel):
    id: int
    name: str

    model_config = {"from_attributes": True}


class TagCreate(BaseModel):
    name: str = Field(..., min_length=1, max_length=100)


class ComicBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=500)
    author: str | None = None
    series: str | None = None
    volume: int | None = None
    description: str | None = None
    publisher: str | None = None
    year: int | None = None


class ComicCreate(ComicBase):
    pass


class ComicUpdate(BaseModel):
    title: str | None = Field(None, min_length=1, max_length=500)
    author: str | None = None
    series: str | None = None
    volume: int | None = None
    description: str | None = None
    publisher: str | None = None
    year: int | None = None
    tags: list[str] | None = None


class ProgressSummary(BaseModel):
    current_page: int
    total_pages: int
    percentage: float
    last_read_at: datetime | None = None

    model_config = {"from_attributes": True}


class BookmarkSummary(BaseModel):
    id: int
    page_number: int
    label: str | None = None

    model_config = {"from_attributes": True}


class ComicResponse(ComicBase):
    id: int
    file_format: str
    file_size: int
    page_count: int
    cover_path: str | None = None
    added_at: datetime
    updated_at: datetime
    tags: list[TagSchema] = []
    progress: ProgressSummary | None = None
    bookmarks: list[BookmarkSummary] = []

    model_config = {"from_attributes": True}


class ComicListResponse(BaseModel):
    comics: list[ComicResponse]
    total: int
    page: int
    page_size: int


class ComicPageResponse(BaseModel):
    page_number: int
    total_pages: int
    image_url: str
