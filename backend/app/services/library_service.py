"""Library management service."""

import os
import uuid
from pathlib import Path

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.config import settings
from app.models.comic import Comic, Tag
from app.schemas.comic import ComicListResponse, ComicResponse, ComicUpdate
from app.services.comic_parser import ComicParser


class LibraryService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def import_comic(self, file_path: str, original_filename: str | None = None) -> Comic:
        """Import a comic file into the library."""
        metadata = ComicParser.extract_metadata(file_path)
        if original_filename:
            metadata["title"] = Path(original_filename).stem

        thumb_name = f"{uuid.uuid4().hex}.jpg"
        thumb_path = os.path.join(settings.thumbnail_path, thumb_name)
        cover = ComicParser.generate_thumbnail(file_path, thumb_path)

        comic = Comic(
            title=str(metadata.get("title", "")),
            file_path=file_path,
            file_format=str(metadata.get("file_format", "")),
            file_size=int(metadata.get("file_size", 0)),
            page_count=int(metadata.get("page_count", 0)),
            cover_path=cover if cover else None,
            author=metadata.get("author"),  # type: ignore[arg-type]
            series=metadata.get("series"),  # type: ignore[arg-type]
            volume=metadata.get("volume"),  # type: ignore[arg-type]
            description=metadata.get("description"),  # type: ignore[arg-type]
            publisher=metadata.get("publisher"),  # type: ignore[arg-type]
            year=metadata.get("year"),  # type: ignore[arg-type]
        )

        self.db.add(comic)
        await self.db.commit()
        await self.db.refresh(comic)
        return comic

    async def get_comics(
        self,
        page: int = 1,
        page_size: int = 20,
        search: str | None = None,
        sort_by: str = "added_at",
        sort_order: str = "desc",
        tag: str | None = None,
        series: str | None = None,
    ) -> ComicListResponse:
        """Get paginated list of comics with optional filtering."""
        query = select(Comic).options(
            selectinload(Comic.tags),
            selectinload(Comic.progress),
            selectinload(Comic.bookmarks),
        )

        if search:
            query = query.where(
                Comic.title.ilike(f"%{search}%")
                | Comic.author.ilike(f"%{search}%")
                | Comic.series.ilike(f"%{search}%")
            )

        if tag:
            query = query.join(Comic.tags).where(Tag.name == tag)

        if series:
            query = query.where(Comic.series == series)

        count_query = select(func.count()).select_from(query.subquery())
        total_result = await self.db.execute(count_query)
        total = total_result.scalar() or 0

        sort_column = getattr(Comic, sort_by, Comic.added_at)
        if sort_order == "asc":
            query = query.order_by(sort_column.asc())
        else:
            query = query.order_by(sort_column.desc())

        query = query.offset((page - 1) * page_size).limit(page_size)
        result = await self.db.execute(query)
        comics = list(result.scalars().all())

        return ComicListResponse(
            comics=[ComicResponse.model_validate(c) for c in comics],
            total=total,
            page=page,
            page_size=page_size,
        )

    async def get_comic(self, comic_id: int) -> Comic | None:
        query = (
            select(Comic)
            .where(Comic.id == comic_id)
            .options(
                selectinload(Comic.tags),
                selectinload(Comic.progress),
                selectinload(Comic.bookmarks),
            )
        )
        result = await self.db.execute(query)
        return result.scalar_one_or_none()

    async def update_comic(self, comic_id: int, update: ComicUpdate) -> Comic | None:
        comic = await self.get_comic(comic_id)
        if not comic:
            return None

        update_data = update.model_dump(exclude_unset=True, exclude={"tags"})
        for key, value in update_data.items():
            setattr(comic, key, value)

        if update.tags is not None:
            tag_objects = []
            for tag_name in update.tags:
                result = await self.db.execute(select(Tag).where(Tag.name == tag_name))
                tag = result.scalar_one_or_none()
                if not tag:
                    tag = Tag(name=tag_name)
                    self.db.add(tag)
                tag_objects.append(tag)
            comic.tags = tag_objects

        await self.db.commit()
        await self.db.refresh(comic)
        return comic

    async def delete_comic(self, comic_id: int, delete_file: bool = False) -> bool:
        comic = await self.get_comic(comic_id)
        if not comic:
            return False

        if delete_file and comic.file_path and os.path.exists(comic.file_path):
            os.remove(comic.file_path)

        if comic.cover_path and os.path.exists(comic.cover_path):
            os.remove(comic.cover_path)

        await self.db.delete(comic)
        await self.db.commit()
        return True

    async def get_all_tags(self) -> list[Tag]:
        result = await self.db.execute(select(Tag).order_by(Tag.name))
        return list(result.scalars().all())

    async def get_all_series(self) -> list[str]:
        result = await self.db.execute(
            select(Comic.series).where(Comic.series.isnot(None)).distinct().order_by(Comic.series)
        )
        return [row[0] for row in result.all()]

    async def scan_library(self) -> int:
        """Scan the library directory for new comic files."""
        imported = 0
        for root, _dirs, files in os.walk(settings.library_path):
            for file in files:
                file_path = os.path.join(root, file)
                ext = os.path.splitext(file)[1].lower()
                if ext not in settings.supported_formats:
                    continue
                existing = await self.db.execute(
                    select(Comic).where(Comic.file_path == file_path)
                )
                if existing.scalar_one_or_none():
                    continue
                try:
                    await self.import_comic(file_path)
                    imported += 1
                except Exception:
                    continue
        return imported
