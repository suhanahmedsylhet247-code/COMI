import { useCallback, useState } from "react";
import { api } from "../api/client";
import "./UploadModal.css";

interface UploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUploaded: () => void;
}

export function UploadModal({ isOpen, onClose, onUploaded }: UploadModalProps) {
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [dragActive, setDragActive] = useState(false);

  const handleUpload = useCallback(async () => {
    if (!file) return;
    setUploading(true);
    setError(null);
    try {
      await api.uploadComic(file);
      setFile(null);
      onUploaded();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setUploading(false);
    }
  }, [file, onClose, onUploaded]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile) setFile(droppedFile);
  }, []);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Upload Comic</h2>
          <button className="modal-close" onClick={onClose}>
            &times;
          </button>
        </div>

        <div
          className={`drop-zone ${dragActive ? "active" : ""}`}
          onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
          onDragLeave={() => setDragActive(false)}
          onDrop={handleDrop}
        >
          {file ? (
            <div className="file-info">
              <p className="file-name">{file.name}</p>
              <p className="file-size">{(file.size / (1024 * 1024)).toFixed(1)} MB</p>
            </div>
          ) : (
            <>
              <p className="drop-text">Drag & drop a comic file here</p>
              <p className="drop-formats">Supports CBZ, CBR, PDF</p>
              <label className="browse-btn">
                Browse Files
                <input
                  type="file"
                  accept=".cbz,.cbr,.pdf"
                  onChange={(e) => setFile(e.target.files?.[0] || null)}
                  hidden
                />
              </label>
            </>
          )}
        </div>

        {error && <p className="upload-error">{error}</p>}

        <div className="modal-actions">
          <button className="cancel-btn" onClick={onClose} disabled={uploading}>
            Cancel
          </button>
          <button className="upload-submit" onClick={handleUpload} disabled={!file || uploading}>
            {uploading ? "Uploading..." : "Upload"}
          </button>
        </div>
      </div>
    </div>
  );
}
