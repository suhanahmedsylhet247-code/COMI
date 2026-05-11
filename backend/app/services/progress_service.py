"""Reading progress and bookmark management service."""

import datetime

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.comic import Comic
from app.models.reading_progress import Bookmark, ReadingProgress
from app.schemas.progress import ReadingHistoryEntry


class ProgressService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_progress(self, comic_id: int) -> ReadingProgress | None:
        result = await self.db.execute(
            select(ReadingProgress).where(ReadingProgress.comic_id == comic_id)
        )
        return result.scalar_one_or_none()

    async def update_progress(self, comic_id: int, current_page: int) -> ReadingProgress | None:
        comic = await self.db.execute(
            select(Comic).where(Comic.id == comic_id)
        )
        comic_obj = comic.scalar_one_or_none()
        if not comic_obj:
            return None

        total_pages = comic_obj.page_count
        if current_page < 0 or current_page >= total_pages:
            current_page = max(0, min(current_page, total_pages - 1))

        percentage = ((current_page + 1) / total_pages * 100) if total_pages > 0 else 0

        result = await self.db.execute(
            select(ReadingProgress).where(ReadingProgress.comic_id == comic_id)
        )
        progress = result.scalar_one_or_none()

        now = datetime.datetime.utcnow()
        if progress:
            progress.current_page = current_page
            progress.total_pages = total_pages
            progress.percentage = round(percentage, 1)
            progress.last_read_at = now
            if percentage >= 100:
                progress.finished_at = now
        else:
            progress = ReadingProgress(
                comic_id=comic_id,
                current_page=current_page,
                total_pages=total_pages,
                percentage=round(percentage, 1),
                started_at=now,
                last_read_at=now,
                finished_at=now if percentage >= 100 else None,
            )
            self.db.add(progress)

        await self.db.commit()
        await self.db.refresh(progress)
        return progress

    async def get_reading_history(self, limit: int = 20) -> list[ReadingHistoryEntry]:
        result = await self.db.execute(
            select(ReadingProgress)
            .options(selectinload(ReadingProgress.comic))
            .order_by(ReadingProgress.last_read_at.desc())
            .limit(limit)
        )
        entries = []
        for progress in result.scalars().all():
            entries.append(
                ReadingHistoryEntry(
                    comic_id=progress.comic_id,
                    comic_title=progress.comic.title,
                    current_page=progress.current_page,
                    total_pages=progress.total_pages,
                    percentage=progress.percentage,
                    last_read_at=progress.last_read_at,
                    cover_path=progress.comic.cover_path,
                )
            )
        return entries

    async def add_bookmark(
        self, comic_id: int, page_number: int, label: str | None = None, note: str | None = None
    ) -> Bookmark | None:
        comic = await self.db.execute(select(Comic).where(Comic.id == comic_id))
        if not comic.scalar_one_or_none():
            return None

        bookmark = Bookmark(
            comic_id=comic_id,
            page_number=page_number,
            label=label,
            note=note,
        )
        self.db.add(bookmark)
        await self.db.commit()
        await self.db.refresh(bookmark)
        return bookmark

    async def get_bookmarks(self, comic_id: int) -> list[Bookmark]:
        result = await self.db.execute(
            select(Bookmark)
            .where(Bookmark.comic_id == comic_id)
            .order_by(Bookmark.page_number)
        )
        return list(result.scalars().all())

    async def delete_bookmark(self, bookmark_id: int) -> bool:
        result = await self.db.execute(select(Bookmark).where(Bookmark.id == bookmark_id))
        bookmark = result.scalar_one_or_none()
        if not bookmark:
            return False
        await self.db.delete(bookmark)
        await self.db.commit()
        return True
