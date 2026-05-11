"""Reading progress and bookmark endpoints."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.schemas.progress import (
    BookmarkCreate,
    BookmarkResponse,
    ProgressResponse,
    ProgressUpdate,
    ReadingHistoryEntry,
)
from app.services.progress_service import ProgressService

router = APIRouter(prefix="/api/progress", tags=["progress"])


@router.get("/{comic_id}", response_model=ProgressResponse | None)
async def get_progress(
    comic_id: int,
    db: AsyncSession = Depends(get_db),
) -> ProgressResponse | None:
    """Get reading progress for a comic."""
    service = ProgressService(db)
    progress = await service.get_progress(comic_id)
    if not progress:
        return None
    return ProgressResponse.model_validate(progress)


@router.put("/{comic_id}", response_model=ProgressResponse)
async def update_progress(
    comic_id: int,
    update: ProgressUpdate,
    db: AsyncSession = Depends(get_db),
) -> ProgressResponse:
    """Update reading progress for a comic."""
    service = ProgressService(db)
    progress = await service.update_progress(comic_id, update.current_page)
    if not progress:
        raise HTTPException(status_code=404, detail="Comic not found")
    return ProgressResponse.model_validate(progress)


@router.get("/", response_model=list[ReadingHistoryEntry])
async def get_reading_history(
    limit: int = 20,
    db: AsyncSession = Depends(get_db),
) -> list[ReadingHistoryEntry]:
    """Get recent reading history."""
    service = ProgressService(db)
    return await service.get_reading_history(limit=limit)


@router.post("/{comic_id}/bookmarks", response_model=BookmarkResponse)
async def add_bookmark(
    comic_id: int,
    bookmark: BookmarkCreate,
    db: AsyncSession = Depends(get_db),
) -> BookmarkResponse:
    """Add a bookmark to a comic."""
    service = ProgressService(db)
    result = await service.add_bookmark(
        comic_id=comic_id,
        page_number=bookmark.page_number,
        label=bookmark.label,
        note=bookmark.note,
    )
    if not result:
        raise HTTPException(status_code=404, detail="Comic not found")
    return BookmarkResponse.model_validate(result)


@router.get("/{comic_id}/bookmarks", response_model=list[BookmarkResponse])
async def get_bookmarks(
    comic_id: int,
    db: AsyncSession = Depends(get_db),
) -> list[BookmarkResponse]:
    """Get all bookmarks for a comic."""
    service = ProgressService(db)
    bookmarks = await service.get_bookmarks(comic_id)
    return [BookmarkResponse.model_validate(b) for b in bookmarks]


@router.delete("/bookmarks/{bookmark_id}")
async def delete_bookmark(
    bookmark_id: int,
    db: AsyncSession = Depends(get_db),
) -> dict[str, str]:
    """Delete a bookmark."""
    service = ProgressService(db)
    success = await service.delete_bookmark(bookmark_id)
    if not success:
        raise HTTPException(status_code=404, detail="Bookmark not found")
    return {"status": "deleted"}
