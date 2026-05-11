"""Comic file parser supporting CBZ, CBR, and PDF formats."""

import io
import os
import zipfile
from pathlib import Path

import fitz  # PyMuPDF
from PIL import Image

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff"}
THUMBNAIL_SIZE = (300, 450)


def _is_image_file(name: str) -> bool:
    return Path(name).suffix.lower() in IMAGE_EXTENSIONS


def _sort_pages(names: list[str]) -> list[str]:
    return sorted(names, key=lambda n: n.lower())


class ComicParser:
    """Unified parser for comic book archives and PDFs."""

    @staticmethod
    def get_format(file_path: str) -> str:
        return Path(file_path).suffix.lower()

    @staticmethod
    def get_page_count(file_path: str) -> int:
        fmt = ComicParser.get_format(file_path)
        if fmt == ".cbz":
            return ComicParser._cbz_page_count(file_path)
        elif fmt == ".cbr":
            return ComicParser._cbr_page_count(file_path)
        elif fmt == ".pdf":
            return ComicParser._pdf_page_count(file_path)
        raise ValueError(f"Unsupported format: {fmt}")

    @staticmethod
    def extract_page(file_path: str, page_number: int) -> bytes:
        fmt = ComicParser.get_format(file_path)
        if fmt == ".cbz":
            return ComicParser._cbz_extract_page(file_path, page_number)
        elif fmt == ".cbr":
            return ComicParser._cbr_extract_page(file_path, page_number)
        elif fmt == ".pdf":
            return ComicParser._pdf_extract_page(file_path, page_number)
        raise ValueError(f"Unsupported format: {fmt}")

    @staticmethod
    def generate_thumbnail(file_path: str, output_path: str) -> str:
        try:
            page_data = ComicParser.extract_page(file_path, 0)
            img = Image.open(io.BytesIO(page_data))
            img.thumbnail(THUMBNAIL_SIZE, Image.Resampling.LANCZOS)
            if img.mode in ("RGBA", "P"):
                img = img.convert("RGB")
            img.save(output_path, "JPEG", quality=85)
            return output_path
        except Exception:
            return ""

    # --- CBZ (ZIP) ---
    @staticmethod
    def _cbz_page_list(file_path: str) -> list[str]:
        with zipfile.ZipFile(file_path, "r") as zf:
            return _sort_pages([n for n in zf.namelist() if _is_image_file(n)])

    @staticmethod
    def _cbz_page_count(file_path: str) -> int:
        return len(ComicParser._cbz_page_list(file_path))

    @staticmethod
    def _cbz_extract_page(file_path: str, page_number: int) -> bytes:
        pages = ComicParser._cbz_page_list(file_path)
        if page_number < 0 or page_number >= len(pages):
            raise IndexError(f"Page {page_number} out of range (0-{len(pages) - 1})")
        with zipfile.ZipFile(file_path, "r") as zf:
            return zf.read(pages[page_number])

    # --- CBR (RAR) ---
    @staticmethod
    def _cbr_page_list(file_path: str) -> list[str]:
        import rarfile

        with rarfile.RarFile(file_path, "r") as rf:
            return _sort_pages([n for n in rf.namelist() if _is_image_file(n)])

    @staticmethod
    def _cbr_page_count(file_path: str) -> int:
        return len(ComicParser._cbr_page_list(file_path))

    @staticmethod
    def _cbr_extract_page(file_path: str, page_number: int) -> bytes:
        import rarfile

        pages = ComicParser._cbr_page_list(file_path)
        if page_number < 0 or page_number >= len(pages):
            raise IndexError(f"Page {page_number} out of range (0-{len(pages) - 1})")
        with rarfile.RarFile(file_path, "r") as rf:
            return rf.read(pages[page_number])

    # --- PDF ---
    @staticmethod
    def _pdf_page_count(file_path: str) -> int:
        doc = fitz.open(file_path)
        count = len(doc)
        doc.close()
        return count

    @staticmethod
    def _pdf_extract_page(file_path: str, page_number: int) -> bytes:
        doc = fitz.open(file_path)
        total = len(doc)
        if page_number < 0 or page_number >= total:
            doc.close()
            raise IndexError(f"Page {page_number} out of range (0-{total - 1})")
        page = doc[page_number]
        pix = page.get_pixmap(dpi=150)
        img_data = pix.tobytes("png")
        doc.close()
        return img_data

    @staticmethod
    def extract_metadata(file_path: str) -> dict[str, str | int | None]:
        """Extract metadata from comic file if available."""
        fmt = ComicParser.get_format(file_path)
        metadata: dict[str, str | int | None] = {
            "title": Path(file_path).stem,
            "file_size": os.path.getsize(file_path),
            "page_count": ComicParser.get_page_count(file_path),
            "file_format": fmt,
        }

        if fmt == ".pdf":
            try:
                doc = fitz.open(file_path)
                pdf_meta = doc.metadata
                if pdf_meta:
                    metadata["title"] = pdf_meta.get("title") or metadata["title"]
                    metadata["author"] = pdf_meta.get("author")
                doc.close()
            except Exception:
                pass

        if fmt == ".cbz":
            try:
                with zipfile.ZipFile(file_path, "r") as zf:
                    if "ComicInfo.xml" in zf.namelist():
                        import xml.etree.ElementTree as ET

                        xml_data = zf.read("ComicInfo.xml")
                        root = ET.fromstring(xml_data)
                        title_el = root.find("Title")
                        if title_el is not None and title_el.text:
                            metadata["title"] = title_el.text
                        author_el = root.find("Writer")
                        if author_el is not None and author_el.text:
                            metadata["author"] = author_el.text
                        series_el = root.find("Series")
                        if series_el is not None and series_el.text:
                            metadata["series"] = series_el.text
                        vol_el = root.find("Volume")
                        if vol_el is not None and vol_el.text:
                            try:
                                metadata["volume"] = int(vol_el.text)
                            except ValueError:
                                pass
                        pub_el = root.find("Publisher")
                        if pub_el is not None and pub_el.text:
                            metadata["publisher"] = pub_el.text
                        year_el = root.find("Year")
                        if year_el is not None and year_el.text:
                            try:
                                metadata["year"] = int(year_el.text)
                            except ValueError:
                                pass
                        desc_el = root.find("Summary")
                        if desc_el is not None and desc_el.text:
                            metadata["description"] = desc_el.text
            except Exception:
                pass

        return metadata
