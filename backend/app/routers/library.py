"""Library management endpoints."""

import os
import shutil
import uuid

from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.database import get_db
from app.schemas.comic import ComicListResponse, ComicResponse, ComicUpdate, TagSchema
from app.services.library_service import LibraryService

router = APIRouter(prefix="/api/library", tags=["library"])


@router.get("/comics", response_model=ComicListResponse)
async def list_comics(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    search: str | None = None,
    sort_by: str = Query("added_at", pattern="^(title|added_at|year|series|author)$"),
    sort_order: str = Query("desc", pattern="^(asc|desc)$"),
    tag: str | None = None,
    series: str | None = None,
    db: AsyncSession = Depends(get_db),
) -> ComicListResponse:
    """Get paginated list of comics with filtering and sorting."""
    service = LibraryService(db)
    return await service.get_comics(
        page=page,
        page_size=page_size,
        search=search,
        sort_by=sort_by,
        sort_order=sort_order,
        tag=tag,
        series=series,
    )


@router.get("/comics/{comic_id}", response_model=ComicResponse)
async def get_comic(
    comic_id: int,
    db: AsyncSession = Depends(get_db),
) -> ComicResponse:
    """Get details of a specific comic."""
    service = LibraryService(db)
    comic = await service.get_comic(comic_id)
    if not comic:
        raise HTTPException(status_code=404, detail="Comic not found")
    return ComicResponse.model_validate(comic)


@router.put("/comics/{comic_id}", response_model=ComicResponse)
async def update_comic(
    comic_id: int,
    update: ComicUpdate,
    db: AsyncSession = Depends(get_db),
) -> ComicResponse:
    """Update comic metadata."""
    service = LibraryService(db)
    comic = await service.update_comic(comic_id, update)
    if not comic:
        raise HTTPException(status_code=404, detail="Comic not found")
    return ComicResponse.model_validate(comic)


@router.delete("/comics/{comic_id}")
async def delete_comic(
    comic_id: int,
    delete_file: bool = False,
    db: AsyncSession = Depends(get_db),
) -> dict[str, str]:
    """Delete a comic from the library."""
    service = LibraryService(db)
    success = await service.delete_comic(comic_id, delete_file=delete_file)
    if not success:
        raise HTTPException(status_code=404, detail="Comic not found")
    return {"status": "deleted"}


@router.post("/upload", response_model=ComicResponse)
async def upload_comic(
    file: UploadFile,
    db: AsyncSession = Depends(get_db),
) -> ComicResponse:
    """Upload a comic file to the library."""
    if not file.filename:
        raise HTTPException(status_code=400, detail="No filename provided")

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in settings.supported_formats:
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported format: {ext}. Supported: {settings.supported_formats}",
        )

    unique_name = f"{uuid.uuid4().hex}{ext}"
    file_path = os.path.join(settings.upload_path, unique_name)

    with open(file_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    try:
        service = LibraryService(db)
        comic = await service.import_comic(file_path, original_filename=file.filename)
        return ComicResponse.model_validate(comic)
    except Exception as e:
        if os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(status_code=500, detail=f"Failed to import comic: {e}")


@router.post("/scan")
async def scan_library(
    db: AsyncSession = Depends(get_db),
) -> dict[str, int | str]:
    """Scan the library directory for new comic files."""
    service = LibraryService(db)
    imported = await service.scan_library()
    return {"status": "complete", "imported": imported}


@router.get("/tags", response_model=list[TagSchema])
async def list_tags(
    db: AsyncSession = Depends(get_db),
) -> list[TagSchema]:
    """Get all available tags."""
    service = LibraryService(db)
    tags = await service.get_all_tags()
    return [TagSchema.model_validate(t) for t in tags]


@router.get("/series")
async def list_series(
    db: AsyncSession = Depends(get_db),
) -> list[str]:
    """Get all available series names."""
    service = LibraryService(db)
    return await service.get_all_series()
