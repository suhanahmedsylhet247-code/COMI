import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useTheme } from "../context/ThemeContext";
import "./SettingsPage.css";

export function SettingsPage() {
  const { theme, toggleTheme } = useTheme();
  const [health, setHealth] = useState<{ status: string; app: string } | null>(null);

  useEffect(() => {
    api.healthCheck().then(setHealth).catch(() => {});
  }, []);

  return (
    <div className="settings-page">
      <h1>Settings</h1>

      <section className="settings-section">
        <h2>Appearance</h2>
        <div className="setting-row">
          <div>
            <h3>Theme</h3>
            <p>Switch between light and dark mode</p>
          </div>
          <button onClick={toggleTheme} className="setting-btn">
            {theme === "dark" ? "Switch to Light" : "Switch to Dark"}
          </button>
        </div>
      </section>

      <section className="settings-section">
        <h2>Keyboard Shortcuts</h2>
        <div className="shortcuts-grid">
          <div className="shortcut"><kbd>&#x2192;</kbd> / <kbd>D</kbd> / <kbd>Space</kbd><span>Next page</span></div>
          <div className="shortcut"><kbd>&#x2190;</kbd> / <kbd>A</kbd><span>Previous page</span></div>
          <div className="shortcut"><kbd>Home</kbd><span>First page</span></div>
          <div className="shortcut"><kbd>End</kbd><span>Last page</span></div>
          <div className="shortcut"><kbd>F</kbd><span>Toggle fullscreen</span></div>
        </div>
      </section>

      <section className="settings-section">
        <h2>Supported Formats</h2>
        <div className="formats-list">
          <div className="format-item">
            <strong>CBZ</strong> - Comic Book ZIP archive (most common)
          </div>
          <div className="format-item">
            <strong>CBR</strong> - Comic Book RAR archive
          </div>
          <div className="format-item">
            <strong>PDF</strong> - Portable Document Format
          </div>
        </div>
      </section>

      <section className="settings-section">
        <h2>About</h2>
        <div className="about-info">
          <p><strong>COMI</strong> - Comic Reader v1.0.0</p>
          <p>A comprehensive comic reader with library management, reading progress tracking, and multiple viewing modes.</p>
          {health && (
            <p className="health-status">
              Server: <span className="healthy">{health.status}</span>
            </p>
          )}
        </div>
      </section>
    </div>
  );
}
