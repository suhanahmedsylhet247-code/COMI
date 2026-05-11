import { useCallback, useRef, useState } from "react";
import { api } from "../api/client";
import type { FitMode, ViewMode } from "../types";
import "./ComicViewer.css";

interface ComicViewerProps {
  comicId: number;
  currentPage: number;
  totalPages: number;
  onNextPage: () => void;
  onPrevPage: () => void;
}

export function ComicViewer({
  comicId,
  currentPage,
  totalPages,
  onNextPage,
  onPrevPage,
}: ComicViewerProps) {
  const [viewMode, setViewMode] = useState<ViewMode>("single");
  const [fitMode, setFitMode] = useState<FitMode>("width");
  const [zoom, setZoom] = useState(100);
  const containerRef = useRef<HTMLDivElement>(null);

  const handleClick = useCallback(
    (e: React.MouseEvent) => {
      const rect = (e.target as HTMLElement).getBoundingClientRect();
      const clickX = e.clientX - rect.left;
      const halfWidth = rect.width / 2;

      if (clickX > halfWidth) {
        onNextPage();
      } else {
        onPrevPage();
      }
    },
    [onNextPage, onPrevPage]
  );

  const zoomIn = () => setZoom((z) => Math.min(300, z + 25));
  const zoomOut = () => setZoom((z) => Math.max(25, z - 25));
  const resetZoom = () => setZoom(100);

  const fitClass = fitMode === "width" ? "fit-width" : fitMode === "height" ? "fit-height" : "fit-original";

  const renderPages = () => {
    if (viewMode === "double") {
      const pages = [currentPage];
      if (currentPage + 1 < totalPages) {
        pages.push(currentPage + 1);
      }
      return (
        <div className="double-page">
          {pages.map((p) => (
            <img
              key={p}
              src={api.getPageUrl(comicId, p)}
              alt={`Page ${p + 1}`}
              className={fitClass}
              style={fitMode === "original" ? { transform: `scale(${zoom / 100})` } : {}}
              draggable={false}
            />
          ))}
        </div>
      );
    }

    if (viewMode === "vertical") {
      return (
        <div className="vertical-scroll">
          {Array.from({ length: totalPages }, (_, i) => (
            <img
              key={i}
              src={api.getPageUrl(comicId, i)}
              alt={`Page ${i + 1}`}
              className="fit-width"
              loading="lazy"
              draggable={false}
            />
          ))}
        </div>
      );
    }

    return (
      <img
        src={api.getPageUrl(comicId, currentPage)}
        alt={`Page ${currentPage + 1}`}
        className={fitClass}
        style={fitMode === "original" ? { transform: `scale(${zoom / 100})` } : {}}
        onClick={handleClick}
        draggable={false}
      />
    );
  };

  return (
    <div className="comic-viewer" ref={containerRef}>
      <div className="viewer-toolbar">
        <div className="view-modes">
          <button className={viewMode === "single" ? "active" : ""} onClick={() => setViewMode("single")}>
            Single
          </button>
          <button className={viewMode === "double" ? "active" : ""} onClick={() => setViewMode("double")}>
            Double
          </button>
          <button className={viewMode === "vertical" ? "active" : ""} onClick={() => setViewMode("vertical")}>
            Vertical
          </button>
        </div>
        <div className="fit-modes">
          <button className={fitMode === "width" ? "active" : ""} onClick={() => setFitMode("width")}>
            Fit Width
          </button>
          <button className={fitMode === "height" ? "active" : ""} onClick={() => setFitMode("height")}>
            Fit Height
          </button>
          <button className={fitMode === "original" ? "active" : ""} onClick={() => setFitMode("original")}>
            Original
          </button>
        </div>
        {fitMode === "original" && (
          <div className="zoom-controls">
            <button onClick={zoomOut}>-</button>
            <button onClick={resetZoom}>{zoom}%</button>
            <button onClick={zoomIn}>+</button>
          </div>
        )}
      </div>
      <div className="viewer-content">{renderPages()}</div>
    </div>
  );
}
