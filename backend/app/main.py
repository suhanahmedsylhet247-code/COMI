"""COMI - Comic Reader Application."""

from contextlib import asynccontextmanager
from typing import AsyncGenerator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.config import settings
from app.database import engine
from app.models.comic import Base
from app.routers import comics, library, progress


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield
    await engine.dispose()


app = FastAPI(
    title=settings.app_name,
    description="A comprehensive comic reader with support for CBZ, CBR, and PDF formats.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(comics.router)
app.include_router(library.router)
app.include_router(progress.router)

app.mount("/thumbnails", StaticFiles(directory=settings.thumbnail_path), name="thumbnails")


@app.get("/api/health")
async def health_check() -> dict[str, str]:
    return {"status": "healthy", "app": settings.app_name}
