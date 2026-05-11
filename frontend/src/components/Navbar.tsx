import { Link, useLocation } from "react-router-dom";
import { useTheme } from "../context/ThemeContext";
import "./Navbar.css";

export function Navbar() {
  const { theme, toggleTheme } = useTheme();
  const location = useLocation();

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/" className="navbar-logo">
          <svg viewBox="0 0 100 100" width="32" height="32">
            <rect x="10" y="5" width="55" height="75" rx="3" fill="#6366f1" stroke="#4f46e5" strokeWidth="2"/>
            <rect x="35" y="20" width="55" height="75" rx="3" fill="#818cf8" stroke="#6366f1" strokeWidth="2"/>
            <text x="62" y="68" textAnchor="middle" fontFamily="Arial" fontWeight="bold" fontSize="28" fill="white">C</text>
          </svg>
          <span>COMI</span>
        </Link>
      </div>
      <div className="navbar-links">
        <Link to="/" className={location.pathname === "/" ? "active" : ""}>
          Library
        </Link>
        <Link to="/settings" className={location.pathname === "/settings" ? "active" : ""}>
          Settings
        </Link>
      </div>
      <div className="navbar-actions">
        <button onClick={toggleTheme} className="theme-toggle" title="Toggle theme">
          {theme === "dark" ? "\u2600\uFE0F" : "\uD83C\uDF19"}
        </button>
      </div>
    </nav>
  );
}
