import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { Library } from "../components/Library";
import { UploadModal } from "../components/UploadModal";
import type { ReadingHistoryEntry } from "../types";
import "./HomePage.css";

export function HomePage() {
  const [showUpload, setShowUpload] = useState(false);
  const [history, setHistory] = useState<ReadingHistoryEntry[]>([]);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    api.getHistory(5).then(setHistory).catch(() => {});
  }, [refreshKey]);

  const handleUploaded = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  return (
    <div className="home-page">
      {history.length > 0 && (
        <section className="continue-reading">
          <h2>Continue Reading</h2>
          <div className="history-cards">
            {history.map((entry) => (
              <Link to={`/read/${entry.comic_id}`} key={entry.comic_id} className="history-card">
                <div className="history-cover">
                  <img
                    src={api.getThumbnailUrl(entry.comic_id)}
                    alt={entry.comic_title}
                    onError={(e) => {
                      (e.target as HTMLImageElement).src =
                        "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 120 180'%3E%3Crect fill='%23334155' width='120' height='180'/%3E%3Ctext x='60' y='90' text-anchor='middle' fill='%2394a3b8' font-size='12'%3ENo Cover%3C/text%3E%3C/svg%3E";
                    }}
                  />
                  <div className="history-progress-bar">
                    <div className="history-progress-fill" style={{ width: `${entry.percentage}%` }} />
                  </div>
                </div>
                <div className="history-info">
                  <h4>{entry.comic_title}</h4>
                  <p>
                    Page {entry.current_page + 1}/{entry.total_pages} ({entry.percentage.toFixed(0)}%)
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      <Library key={refreshKey} onUploadClick={() => setShowUpload(true)} />
      <UploadModal isOpen={showUpload} onClose={() => setShowUpload(false)} onUploaded={handleUploaded} />
    </div>
  );
}
