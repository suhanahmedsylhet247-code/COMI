"""Comic file operations: page extraction, thumbnail serving."""

import os

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.schemas.comic import ComicPageResponse
from app.services.comic_parser import ComicParser
from app.services.library_service import LibraryService

router = APIRouter(prefix="/api/comics", tags=["comics"])


@router.get("/{comic_id}/page/{page_number}")
async def get_page(
    comic_id: int,
    page_number: int,
    db: AsyncSession = Depends(get_db),
) -> Response:
    """Get a specific page image from a comic."""
    service = LibraryService(db)
    comic = await service.get_comic(comic_id)
    if not comic:
        raise HTTPException(status_code=404, detail="Comic not found")

    if not os.path.exists(comic.file_path):
        raise HTTPException(status_code=404, detail="Comic file not found on disk")

    try:
        image_data = ComicParser.extract_page(comic.file_path, page_number)
    except IndexError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error extracting page: {e}")

    content_type = "image/png" if comic.file_format == ".pdf" else "image/jpeg"
    return Response(content=image_data, media_type=content_type)


@router.get("/{comic_id}/page-info/{page_number}")
async def get_page_info(
    comic_id: int,
    page_number: int,
    db: AsyncSession = Depends(get_db),
) -> ComicPageResponse:
    """Get page info including image URL."""
    service = LibraryService(db)
    comic = await service.get_comic(comic_id)
    if not comic:
        raise HTTPException(status_code=404, detail="Comic not found")

    if page_number < 0 or page_number >= comic.page_count:
        raise HTTPException(status_code=404, detail="Page out of range")

    return ComicPageResponse(
        page_number=page_number,
        total_pages=comic.page_count,
        image_url=f"/api/comics/{comic_id}/page/{page_number}",
    )


@router.get("/{comic_id}/thumbnail")
async def get_thumbnail(
    comic_id: int,
    db: AsyncSession = Depends(get_db),
) -> Response:
    """Get comic cover thumbnail."""
    service = LibraryService(db)
    comic = await service.get_comic(comic_id)
    if not comic:
        raise HTTPException(status_code=404, detail="Comic not found")

    if comic.cover_path and os.path.exists(comic.cover_path):
        with open(comic.cover_path, "rb") as f:
            return Response(content=f.read(), media_type="image/jpeg")

    try:
        image_data = ComicParser.extract_page(comic.file_path, 0)
        return Response(content=image_data, media_type="image/jpeg")
    except Exception:
        raise HTTPException(status_code=404, detail="Thumbnail not available")
