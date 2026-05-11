import { useCallback, useEffect, useState } from "react";
import { api } from "../api/client";
import type { Bookmark, Comic, ReadingProgress } from "../types";

export function useComic(comicId: number) {
  const [comic, setComic] = useState<Comic | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bookmarks, setBookmarks] = useState<Bookmark[]>([]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    Promise.all([api.getComic(comicId), api.getBookmarks(comicId)])
      .then(([comicData, bookmarkData]) => {
        if (cancelled) return;
        setComic(comicData);
        setBookmarks(bookmarkData);
        if (comicData.progress) {
          setCurrentPage(comicData.progress.current_page);
        }
        setLoading(false);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err.message);
        setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [comicId]);

  const goToPage = useCallback(
    (page: number) => {
      if (!comic) return;
      const clamped = Math.max(0, Math.min(page, comic.page_count - 1));
      setCurrentPage(clamped);
      api.updateProgress(comicId, clamped).catch(() => {});
    },
    [comic, comicId]
  );

  const nextPage = useCallback(() => {
    goToPage(currentPage + 1);
  }, [currentPage, goToPage]);

  const prevPage = useCallback(() => {
    goToPage(currentPage - 1);
  }, [currentPage, goToPage]);

  const firstPage = useCallback(() => goToPage(0), [goToPage]);

  const lastPage = useCallback(() => {
    if (comic) goToPage(comic.page_count - 1);
  }, [comic, goToPage]);

  const addBookmark = useCallback(
    async (label?: string, note?: string) => {
      const bm = await api.addBookmark(comicId, currentPage, label, note);
      setBookmarks((prev) => [...prev, bm]);
      return bm;
    },
    [comicId, currentPage]
  );

  const removeBookmark = useCallback(async (bookmarkId: number) => {
    await api.deleteBookmark(bookmarkId);
    setBookmarks((prev) => prev.filter((b) => b.id !== bookmarkId));
  }, []);

  return {
    comic,
    currentPage,
    loading,
    error,
    bookmarks,
    goToPage,
    nextPage,
    prevPage,
    firstPage,
    lastPage,
    addBookmark,
    removeBookmark,
    pageUrl: comic ? api.getPageUrl(comicId, currentPage) : null,
  };
}
