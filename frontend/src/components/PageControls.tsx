import { useState } from "react";
import type { Bookmark } from "../types";
import "./PageControls.css";

interface PageControlsProps {
  currentPage: number;
  totalPages: number;
  bookmarks: Bookmark[];
  onGoToPage: (page: number) => void;
  onNextPage: () => void;
  onPrevPage: () => void;
  onFirstPage: () => void;
  onLastPage: () => void;
  onAddBookmark: (label?: string) => void;
  onRemoveBookmark: (id: number) => void;
}

export function PageControls({
  currentPage,
  totalPages,
  bookmarks,
  onGoToPage,
  onNextPage,
  onPrevPage,
  onFirstPage,
  onLastPage,
  onAddBookmark,
  onRemoveBookmark,
}: PageControlsProps) {
  const [showBookmarks, setShowBookmarks] = useState(false);
  const [pageInput, setPageInput] = useState("");
  const [bookmarkLabel, setBookmarkLabel] = useState("");

  const percentage = totalPages > 0 ? ((currentPage + 1) / totalPages) * 100 : 0;
  const isBookmarked = bookmarks.some((b) => b.page_number === currentPage);

  const handlePageSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const p = parseInt(pageInput, 10);
    if (!isNaN(p) && p >= 1 && p <= totalPages) {
      onGoToPage(p - 1);
      setPageInput("");
    }
  };

  const handleAddBookmark = () => {
    onAddBookmark(bookmarkLabel || undefined);
    setBookmarkLabel("");
  };

  return (
    <div className="page-controls">
      <div className="controls-main">
        <button onClick={onFirstPage} disabled={currentPage === 0} title="First page (Home)">
          &#x23EE;
        </button>
        <button onClick={onPrevPage} disabled={currentPage === 0} title="Previous page (Left/A)">
          &#x25C0;
        </button>

        <div className="page-indicator">
          <span className="current-page">{currentPage + 1}</span>
          <span className="separator">/</span>
          <span className="total-pages">{totalPages}</span>
        </div>

        <button onClick={onNextPage} disabled={currentPage >= totalPages - 1} title="Next page (Right/D)">
          &#x25B6;
        </button>
        <button onClick={onLastPage} disabled={currentPage >= totalPages - 1} title="Last page (End)">
          &#x23ED;
        </button>

        <div className="page-jump">
          <form onSubmit={handlePageSubmit}>
            <input
              type="number"
              placeholder="Go to..."
              min={1}
              max={totalPages}
              value={pageInput}
              onChange={(e) => setPageInput(e.target.value)}
            />
          </form>
        </div>

        <button
          onClick={() => (isBookmarked ? undefined : handleAddBookmark())}
          className={`bookmark-btn ${isBookmarked ? "bookmarked" : ""}`}
          title={isBookmarked ? "Page bookmarked" : "Add bookmark"}
        >
          {isBookmarked ? "\u2605" : "\u2606"}
        </button>

        <button
          onClick={() => setShowBookmarks(!showBookmarks)}
          className="bookmarks-toggle"
          title="Show bookmarks"
        >
          Bookmarks ({bookmarks.length})
        </button>

        <div className="progress-indicator">
          <div className="progress-track">
            <div className="progress-fill" style={{ width: `${percentage}%` }} />
          </div>
          <span className="progress-text">{percentage.toFixed(0)}%</span>
        </div>
      </div>

      {showBookmarks && (
        <div className="bookmarks-panel">
          <div className="bookmark-add">
            <input
              type="text"
              placeholder="Bookmark label..."
              value={bookmarkLabel}
              onChange={(e) => setBookmarkLabel(e.target.value)}
            />
            <button onClick={handleAddBookmark}>Add</button>
          </div>
          {bookmarks.length === 0 ? (
            <p className="no-bookmarks">No bookmarks yet</p>
          ) : (
            <ul className="bookmark-list">
              {bookmarks.map((bm) => (
                <li key={bm.id}>
                  <button className="bookmark-goto" onClick={() => onGoToPage(bm.page_number)}>
                    Page {bm.page_number + 1}
                    {bm.label && <span className="bookmark-label"> - {bm.label}</span>}
                  </button>
                  <button className="bookmark-delete" onClick={() => onRemoveBookmark(bm.id)}>
                    &times;
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="keyboard-hints">
        <span>Arrow Keys / A/D: Navigate</span>
        <span>Home/End: First/Last</span>
        <span>Space: Next</span>
        <span>F: Fullscreen</span>
      </div>
    </div>
  );
}
