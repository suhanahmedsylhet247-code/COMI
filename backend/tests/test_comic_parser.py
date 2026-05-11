"""Tests for comic parser service."""

import os

import pytest

from app.services.comic_parser import ComicParser


class TestCBZParser:
    def test_get_format_cbz(self, sample_cbz):
        assert ComicParser.get_format(sample_cbz) == ".cbz"

    def test_page_count(self, sample_cbz):
        assert ComicParser.get_page_count(sample_cbz) == 5

    def test_extract_page(self, sample_cbz):
        page_data = ComicParser.extract_page(sample_cbz, 0)
        assert isinstance(page_data, bytes)
        assert len(page_data) > 0

    def test_extract_page_out_of_range(self, sample_cbz):
        with pytest.raises(IndexError):
            ComicParser.extract_page(sample_cbz, 10)

    def test_extract_page_negative(self, sample_cbz):
        with pytest.raises(IndexError):
            ComicParser.extract_page(sample_cbz, -1)

    def test_generate_thumbnail(self, sample_cbz, tmp_path):
        thumb_path = os.path.join(str(tmp_path), "thumb.jpg")
        result = ComicParser.generate_thumbnail(sample_cbz, thumb_path)
        assert result == thumb_path
        assert os.path.exists(thumb_path)

    def test_extract_metadata(self, sample_cbz):
        meta = ComicParser.extract_metadata(sample_cbz)
        assert meta["page_count"] == 5
        assert meta["file_format"] == ".cbz"
        assert meta["file_size"] > 0

    def test_extract_metadata_with_comicinfo(self, sample_cbz_with_metadata):
        meta = ComicParser.extract_metadata(sample_cbz_with_metadata)
        assert meta["title"] == "Test Comic"
        assert meta["author"] == "Test Author"
        assert meta["series"] == "Test Series"
        assert meta["volume"] == 1
        assert meta["publisher"] == "Test Publisher"
        assert meta["year"] == 2024
        assert meta["description"] == "A test comic for unit testing."


class TestPDFParser:
    def test_get_format_pdf(self, sample_pdf):
        assert ComicParser.get_format(sample_pdf) == ".pdf"

    def test_page_count(self, sample_pdf):
        assert ComicParser.get_page_count(sample_pdf) == 3

    def test_extract_page(self, sample_pdf):
        page_data = ComicParser.extract_page(sample_pdf, 0)
        assert isinstance(page_data, bytes)
        assert len(page_data) > 0

    def test_extract_page_out_of_range(self, sample_pdf):
        with pytest.raises(IndexError):
            ComicParser.extract_page(sample_pdf, 10)

    def test_generate_thumbnail(self, sample_pdf, tmp_path):
        thumb_path = os.path.join(str(tmp_path), "thumb.jpg")
        result = ComicParser.generate_thumbnail(sample_pdf, thumb_path)
        assert result == thumb_path
        assert os.path.exists(thumb_path)

    def test_extract_metadata(self, sample_pdf):
        meta = ComicParser.extract_metadata(sample_pdf)
        assert meta["page_count"] == 3
        assert meta["file_format"] == ".pdf"


class TestUnsupportedFormat:
    def test_unsupported_format(self, tmp_path):
        bad_file = os.path.join(str(tmp_path), "test.txt")
        with open(bad_file, "w") as f:
            f.write("not a comic")
        with pytest.raises(ValueError, match="Unsupported format"):
            ComicParser.get_page_count(bad_file)
