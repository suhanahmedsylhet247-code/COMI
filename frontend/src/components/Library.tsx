import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import type { Comic, SortField, SortOrder } from "../types";
import "./Library.css";

interface LibraryProps {
  onUploadClick: () => void;
}

export function Library({ onUploadClick }: LibraryProps) {
  const [comics, setComics] = useState<Comic[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState<SortField>("added_at");
  const [sortOrder, setSortOrder] = useState<SortOrder>("desc");
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");

  const pageSize = 20;

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    api
      .listComics({ page, page_size: pageSize, search: search || undefined, sort_by: sortBy, sort_order: sortOrder })
      .then((data) => {
        if (cancelled) return;
        setComics(data.comics);
        setTotal(data.total);
        setLoading(false);
      })
      .catch(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [page, search, sortBy, sortOrder]);

  const totalPages = Math.ceil(total / pageSize);

  const formatSize = (bytes: number) => {
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <div className="library">
      <div className="library-toolbar">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Search comics..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          />
        </div>
        <div className="toolbar-actions">
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value as SortField)}>
            <option value="added_at">Date Added</option>
            <option value="title">Title</option>
            <option value="year">Year</option>
            <option value="author">Author</option>
            <option value="series">Series</option>
          </select>
          <button onClick={() => setSortOrder((o) => (o === "asc" ? "desc" : "asc"))} className="sort-btn">
            {sortOrder === "asc" ? "\u2191" : "\u2193"}
          </button>
          <button onClick={() => setViewMode((v) => (v === "grid" ? "list" : "grid"))} className="view-btn">
            {viewMode === "grid" ? "\u2630" : "\u25A6"}
          </button>
          <button onClick={onUploadClick} className="upload-btn">
            + Upload
          </button>
          <button onClick={() => api.scanLibrary().then(() => setPage(1))} className="scan-btn">
            Scan Library
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading">Loading comics...</div>
      ) : comics.length === 0 ? (
        <div className="empty-state">
          <h2>No comics found</h2>
          <p>Upload a comic or scan your library to get started.</p>
          <button onClick={onUploadClick} className="upload-btn large">
            Upload Comic
          </button>
        </div>
      ) : (
        <div className={`comic-grid ${viewMode}`}>
          {comics.map((comic) => (
            <Link to={`/read/${comic.id}`} key={comic.id} className="comic-card">
              <div className="comic-cover">
                <img
                  src={api.getThumbnailUrl(comic.id)}
                  alt={comic.title}
                  loading="lazy"
                  onError={(e) => {
                    (e.target as HTMLImageElement).src =
                      "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 300 450'%3E%3Crect fill='%23334155' width='300' height='450'/%3E%3Ctext x='150' y='225' text-anchor='middle' fill='%2394a3b8' font-size='20'%3ENo Cover%3C/text%3E%3C/svg%3E";
                  }}
                />
                {comic.progress && (
                  <div className="progress-overlay">
                    <div
                      className="progress-bar"
                      style={{ width: `${comic.progress.percentage}%` }}
                    />
                  </div>
                )}
                <span className="format-badge">{comic.file_format.replace(".", "").toUpperCase()}</span>
              </div>
              <div className="comic-info">
                <h3 className="comic-title">{comic.title}</h3>
                {comic.author && <p className="comic-author">{comic.author}</p>}
                <div className="comic-meta">
                  <span>{comic.page_count} pages</span>
                  <span>{formatSize(comic.file_size)}</span>
                </div>
                {comic.tags.length > 0 && (
                  <div className="comic-tags">
                    {comic.tags.slice(0, 3).map((tag) => (
                      <span key={tag.id} className="tag">{tag.name}</span>
                    ))}
                  </div>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage((p) => Math.max(1, p - 1))} disabled={page === 1}>
            Previous
          </button>
          <span>
            Page {page} of {totalPages}
          </span>
          <button onClick={() => setPage((p) => Math.min(totalPages, p + 1))} disabled={page === totalPages}>
            Next
          </button>
        </div>
      )}
    </div>
  );
}
