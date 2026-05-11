"""Tests for comic page/thumbnail endpoints."""

import pytest


@pytest.mark.asyncio
async def test_get_page(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/comics/{comic_id}/page/0")
    assert resp.status_code == 200
    assert resp.headers["content-type"] in ["image/jpeg", "image/png"]
    assert len(resp.content) > 0


@pytest.mark.asyncio
async def test_get_page_out_of_range(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/comics/{comic_id}/page/100")
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_get_page_not_found_comic(client):
    resp = await client.get("/api/comics/999/page/0")
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_get_page_info(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/comics/{comic_id}/page-info/0")
    assert resp.status_code == 200
    data = resp.json()
    assert data["page_number"] == 0
    assert data["total_pages"] == 5


@pytest.mark.asyncio
async def test_get_thumbnail(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/comics/{comic_id}/thumbnail")
    assert resp.status_code == 200
    assert len(resp.content) > 0


@pytest.mark.asyncio
async def test_health_check(client):
    resp = await client.get("/api/health")
    assert resp.status_code == 200
    assert resp.json()["status"] == "healthy"


@pytest.mark.asyncio
async def test_get_pdf_page(client, sample_pdf):
    with open(sample_pdf, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.pdf", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/comics/{comic_id}/page/0")
    assert resp.status_code == 200
    assert resp.headers["content-type"] == "image/png"
    assert len(resp.content) > 0
