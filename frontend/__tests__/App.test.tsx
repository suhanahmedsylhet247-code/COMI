import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import App from "../src/App";

// Mock fetch for API calls
globalThis.fetch = vi.fn(() =>
  Promise.resolve({
    ok: true,
    json: () => Promise.resolve({ comics: [], total: 0, page: 1, page_size: 20 }),
  } as Response)
);

describe("App", () => {
  it("renders the navbar with COMI brand", () => {
    render(<App />);
    expect(screen.getByText("COMI")).toBeInTheDocument();
  });

  it("renders library and settings nav links", () => {
    render(<App />);
    expect(screen.getByText("Library")).toBeInTheDocument();
    expect(screen.getByText("Settings")).toBeInTheDocument();
  });

  it("renders the theme toggle button", () => {
    render(<App />);
    const toggle = screen.getByTitle("Toggle theme");
    expect(toggle).toBeInTheDocument();
  });
});
