"""Tests for library API endpoints."""

import os

import pytest


@pytest.mark.asyncio
async def test_list_comics_empty(client):
    resp = await client.get("/api/library/comics")
    assert resp.status_code == 200
    data = resp.json()
    assert data["comics"] == []
    assert data["total"] == 0


@pytest.mark.asyncio
async def test_upload_comic(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    assert resp.status_code == 200
    data = resp.json()
    assert data["page_count"] == 5
    assert data["file_format"] == ".cbz"
    assert data["id"] is not None


@pytest.mark.asyncio
async def test_upload_unsupported_format(client, tmp_path):
    txt_path = os.path.join(str(tmp_path), "test.txt")
    with open(txt_path, "w") as f:
        f.write("not a comic")
    with open(txt_path, "rb") as f:
        resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.txt", f, "text/plain")},
        )
    assert resp.status_code == 400


@pytest.mark.asyncio
async def test_get_comic_details(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.get(f"/api/library/comics/{comic_id}")
    assert resp.status_code == 200
    assert resp.json()["id"] == comic_id


@pytest.mark.asyncio
async def test_get_comic_not_found(client):
    resp = await client.get("/api/library/comics/999")
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_update_comic_metadata(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.put(
        f"/api/library/comics/{comic_id}",
        json={
            "title": "Updated Title",
            "author": "New Author",
            "tags": ["action", "superhero"],
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["title"] == "Updated Title"
    assert data["author"] == "New Author"
    assert len(data["tags"]) == 2


@pytest.mark.asyncio
async def test_delete_comic(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        upload_resp = await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )
    comic_id = upload_resp.json()["id"]

    resp = await client.delete(f"/api/library/comics/{comic_id}")
    assert resp.status_code == 200
    assert resp.json()["status"] == "deleted"

    resp = await client.get(f"/api/library/comics/{comic_id}")
    assert resp.status_code == 404


@pytest.mark.asyncio
async def test_list_comics_with_search(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )

    resp = await client.get("/api/library/comics", params={"search": "test"})
    assert resp.status_code == 200
    assert resp.json()["total"] >= 1


@pytest.mark.asyncio
async def test_list_comics_pagination(client, sample_cbz):
    with open(sample_cbz, "rb") as f:
        await client.post(
            "/api/library/upload",
            files={"file": ("test.cbz", f, "application/octet-stream")},
        )

    resp = await client.get("/api/library/comics", params={"page": 1, "page_size": 1})
    assert resp.status_code == 200
    data = resp.json()
    assert data["page"] == 1
    assert data["page_size"] == 1


@pytest.mark.asyncio
async def test_list_tags_empty(client):
    resp = await client.get("/api/library/tags")
    assert resp.status_code == 200
    assert resp.json() == []


@pytest.mark.asyncio
async def test_list_series_empty(client):
    resp = await client.get("/api/library/series")
    assert resp.status_code == 200
    assert resp.json() == []
