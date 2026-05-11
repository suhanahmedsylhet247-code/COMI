"""Shared test fixtures."""

import io
import os
import tempfile
import zipfile

import pytest
import pytest_asyncio
from httpx import ASGITransport, AsyncClient
from PIL import Image
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.database import get_db
from app.main import app
from app.models.comic import Base


@pytest_asyncio.fixture
async def db_session():
    engine = create_async_engine("sqlite+aiosqlite:///:memory:")
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    session_factory = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    async with session_factory() as session:
        yield session

    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
    await engine.dispose()


@pytest_asyncio.fixture
async def client(db_session):
    async def override_get_db():
        yield db_session

    app.dependency_overrides[get_db] = override_get_db
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac
    app.dependency_overrides.clear()


@pytest.fixture
def sample_cbz(tmp_path):
    """Create a sample CBZ file with test images."""
    cbz_path = os.path.join(str(tmp_path), "test_comic.cbz")
    with zipfile.ZipFile(cbz_path, "w") as zf:
        for i in range(5):
            img = Image.new("RGB", (100, 150), color=(i * 50, 100, 200))
            buf = io.BytesIO()
            img.save(buf, "JPEG")
            zf.writestr(f"page_{i:03d}.jpg", buf.getvalue())
    return cbz_path


@pytest.fixture
def sample_cbz_with_metadata(tmp_path):
    """Create a CBZ file with ComicInfo.xml metadata."""
    cbz_path = os.path.join(str(tmp_path), "meta_comic.cbz")
    comic_info = """<?xml version="1.0" encoding="utf-8"?>
<ComicInfo>
    <Title>Test Comic</Title>
    <Series>Test Series</Series>
    <Volume>1</Volume>
    <Writer>Test Author</Writer>
    <Publisher>Test Publisher</Publisher>
    <Year>2024</Year>
    <Summary>A test comic for unit testing.</Summary>
</ComicInfo>"""

    with zipfile.ZipFile(cbz_path, "w") as zf:
        zf.writestr("ComicInfo.xml", comic_info)
        for i in range(3):
            img = Image.new("RGB", (100, 150), color=(i * 80, 50, 150))
            buf = io.BytesIO()
            img.save(buf, "JPEG")
            zf.writestr(f"page_{i:03d}.jpg", buf.getvalue())
    return cbz_path


@pytest.fixture
def sample_pdf(tmp_path):
    """Create a sample PDF file."""
    import fitz

    pdf_path = os.path.join(str(tmp_path), "test_comic.pdf")
    doc = fitz.open()
    for i in range(3):
        page = doc.new_page(width=400, height=600)
        page.insert_text((50, 100), f"Page {i + 1}", fontsize=36)
    doc.save(pdf_path)
    doc.close()
    return pdf_path


@pytest.fixture
def temp_dir():
    with tempfile.TemporaryDirectory() as d:
        yield d
