"""Tests for reading progress and bookmark API endpoints."""

import pytest


@pytest.mark.asyncio
async def test_update_and_get_progress(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.put(
        f"/api/progress/{comic_id}",
        json={"current_page": 2},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["current_page"] == 2
    assert data["comic_id"] == comic_id
    assert data["percentage"] > 0

    resp = await client.get(f"/api/progress/{comic_id}")
    assert resp.status_code == 200
    assert resp.json()["current_page"] == 2


@pytest.mark.asyncio
async def test_progress_not_found(client):
    resp = await client.get("/api/progress/999")
    assert resp.status_code == 200
    assert resp.json() is None


@pytest.mark.asyncio
async def test_update_progress_comic_not_found(client):
    resp = await client.put(
        "/api/progress/999",
        json={"current_page": 0},
    )
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_reading_history(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    await client.put(f"/api/progress/{comic_id}", json={"current_page": 1})

    resp = await client.get("/api/progress/")
    assert resp.status_code == 200
    history = resp.json()
    assert len(history) >= 1
    assert history[0]["comic_id"] == comic_id


@pytest.mark.asyncio
async def test_add_bookmark(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.post(
        f"/api/progress/{comic_id}/bookmarks",
        json={"page_number": 2, "label": "Great panel", "note": "Love this art"},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["page_number"] == 2
    assert data["label"] == "Great panel"
    assert data["note"] == "Love this art"


@pytest.mark.asyncio
async def test_get_bookmarks(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    await client.post(
        f"/api/progress/{comic_id}/bookmarks",
        json={"page_number": 0, "label": "Cover"},
    )
    await client.post(
        f"/api/progress/{comic_id}/bookmarks",
        json={"page_number": 3, "label": "Fight scene"},
    )

    resp = await client.get(f"/api/progress/{comic_id}/bookmarks")
    assert resp.status_code == 200
    bookmarks = resp.json()
    assert len(bookmarks) == 2


@pytest.mark.asyncio
async def test_delete_bookmark(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    create_resp = await client.post(
        f"/api/progress/{comic_id}/bookmarks",
        json={"page_number": 1, "label": "Test"},
    )
    bookmark_id = create_resp.json()["id"]

    resp = await client.delete(f"/api/progress/bookmarks/{bookmark_id}")
    assert resp.status_code == 200
    assert resp.json()["status"] == "deleted"


@pytest.mark.asyncio
async def test_delete_bookmark_not_found(client):
    resp = await client.delete("/api/progress/bookmarks/999")
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_progress_completion(client, sample_cbz):
    """Test that progress shows 100% when on the last page."""
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]
    total_pages = upload_resp.json()["page_count"]

    resp = await client.put(
        f"/api/progress/{comic_id}",
        json={"current_page": total_pages - 1},
    )
    assert resp.status_code == 200
    assert resp.json()["percentage"] == 100.0
    assert resp.json()["finished_at"] is not None
