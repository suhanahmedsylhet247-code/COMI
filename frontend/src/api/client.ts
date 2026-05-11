import type {
  Bookmark,
  Comic,
  ComicListResponse,
  ReadingHistoryEntry,
  ReadingProgress,
  SortField,
  SortOrder,
} from "../types";

const BASE = "/api";

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const resp = await fetch(`${BASE}${url}`, {
    headers: { "Content-Type": "application/json", ...options?.headers },
    ...options,
  });
  if (!resp.ok) {
    const error = await resp.json().catch(() => ({ detail: resp.statusText }));
    throw new Error(error.detail || "Request failed");
  }
  return resp.json();
}

export const api = {
  // Library
  listComics: (params?: {
    page?: number;
    page_size?: number;
    search?: string;
    sort_by?: SortField;
    sort_order?: SortOrder;
    tag?: string;
    series?: string;
  }) => {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== "") {
          searchParams.set(k, String(v));
        }
      });
    }
    const qs = searchParams.toString();
    return request<ComicListResponse>(`/library/comics${qs ? `?${qs}` : ""}`);
  },

  getComic: (id: number) => request<Comic>(`/library/comics/${id}`),

  updateComic: (id: number, data: Partial<Comic> & { tags?: string[] }) =>
    request<Comic>(`/library/comics/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),

  deleteComic: (id: number) =>
    request<{ status: string }>(`/library/comics/${id}`, { method: "DELETE" }),

  uploadComic: async (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    const resp = await fetch(`${BASE}/library/upload`, {
      method: "POST",
      body: formData,
    });
    if (!resp.ok) {
      const error = await resp.json().catch(() => ({ detail: resp.statusText }));
      throw new Error(error.detail || "Upload failed");
    }
    return resp.json() as Promise<Comic>;
  },

  scanLibrary: () =>
    request<{ status: string; imported: number }>("/library/scan", { method: "POST" }),

  getTags: () => request<{ id: number; name: string }[]>("/library/tags"),
  getSeries: () => request<string[]>("/library/series"),

  // Pages
  getPageUrl: (comicId: number, page: number) =>
    `${BASE}/comics/${comicId}/page/${page}`,

  getThumbnailUrl: (comicId: number) =>
    `${BASE}/comics/${comicId}/thumbnail`,

  // Progress
  getProgress: (comicId: number) =>
    request<ReadingProgress | null>(`/progress/${comicId}`),

  updateProgress: (comicId: number, currentPage: number) =>
    request<ReadingProgress>(`/progress/${comicId}`, {
      method: "PUT",
      body: JSON.stringify({ current_page: currentPage }),
    }),

  getHistory: (limit?: number) =>
    request<ReadingHistoryEntry[]>(`/progress/${limit ? `?limit=${limit}` : ""}`),

  // Bookmarks
  addBookmark: (comicId: number, pageNumber: number, label?: string, note?: string) =>
    request<Bookmark>(`/progress/${comicId}/bookmarks`, {
      method: "POST",
      body: JSON.stringify({ page_number: pageNumber, label, note }),
    }),

  getBookmarks: (comicId: number) =>
    request<Bookmark[]>(`/progress/${comicId}/bookmarks`),

  deleteBookmark: (bookmarkId: number) =>
    request<{ status: string }>(`/progress/bookmarks/${bookmarkId}`, {
      method: "DELETE",
    }),

  // Health
  healthCheck: () => request<{ status: string; app: string }>("/health"),
};
