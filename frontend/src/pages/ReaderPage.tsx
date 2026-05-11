import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ComicViewer } from "../components/ComicViewer";
import { PageControls } from "../components/PageControls";
import { useComic } from "../hooks/useComic";
import { useKeyboardNav } from "../hooks/useKeyboardNav";
import "./ReaderPage.css";

export function ReaderPage() {
  const { id } = useParams<{ id: string }>();
  const comicId = parseInt(id || "0", 10);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const {
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
  } = useComic(comicId);

  const toggleFullscreen = useCallback(() => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen().then(() => setIsFullscreen(true)).catch(() => {});
    } else {
      document.exitFullscreen().then(() => setIsFullscreen(false)).catch(() => {});
    }
  }, []);

  useKeyboardNav({
    onNext: nextPage,
    onPrev: prevPage,
    onFirst: firstPage,
    onLast: lastPage,
    onToggleFullscreen: toggleFullscreen,
    enabled: !!comic,
  });

  if (loading) {
    return <div className="reader-loading">Loading comic...</div>;
  }

  if (error || !comic) {
    return (
      <div className="reader-error">
        <h2>Error</h2>
        <p>{error || "Comic not found"}</p>
        <Link to="/">Back to Library</Link>
      </div>
    );
  }

  return (
    <div className={`reader-page ${isFullscreen ? "fullscreen" : ""}`}>
      <div className="reader-header">
        <Link to="/" className="back-link">
          &#x2190; Library
        </Link>
        <h1 className="reader-title">{comic.title}</h1>
        <div className="reader-meta">
          {comic.author && <span>{comic.author}</span>}
          {comic.series && (
            <span>
              {comic.series}
              {comic.volume ? ` Vol. ${comic.volume}` : ""}
            </span>
          )}
        </div>
      </div>

      <ComicViewer
        comicId={comicId}
        currentPage={currentPage}
        totalPages={comic.page_count}
        onNextPage={nextPage}
        onPrevPage={prevPage}
      />

      <PageControls
        currentPage={currentPage}
        totalPages={comic.page_count}
        bookmarks={bookmarks}
        onGoToPage={goToPage}
        onNextPage={nextPage}
        onPrevPage={prevPage}
        onFirstPage={firstPage}
        onLastPage={lastPage}
        onAddBookmark={(label) => addBookmark(label)}
        onRemoveBookmark={removeBookmark}
      />
    </div>
  );
}
