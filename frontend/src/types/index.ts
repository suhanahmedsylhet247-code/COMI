export interface Tag {
  id: number;
  name: string;
}

export interface ProgressSummary {
  current_page: number;
  total_pages: number;
  percentage: number;
  last_read_at: string | null;
}

export interface BookmarkSummary {
  id: number;
  page_number: number;
  label: string | null;
}

export interface Comic {
  id: number;
  title: string;
  file_format: string;
  file_size: number;
  page_count: number;
  cover_path: string | null;
  author: string | null;
  series: string | null;
  volume: number | null;
  description: string | null;
  publisher: string | null;
  year: number | null;
  added_at: string;
  updated_at: string;
  tags: Tag[];
  progress: ProgressSummary | null;
  bookmarks: BookmarkSummary[];
}

export interface ComicListResponse {
  comics: Comic[];
  total: number;
  page: number;
  page_size: number;
}

export interface ReadingProgress {
  id: number;
  comic_id: number;
  current_page: number;
  total_pages: number;
  percentage: number;
  last_read_at: string | null;
  started_at: string | null;
  finished_at: string | null;
}

export interface Bookmark {
  id: number;
  comic_id: number;
  page_number: number;
  label: string | null;
  note: string | null;
  created_at: string;
}

export interface ReadingHistoryEntry {
  comic_id: number;
  comic_title: string;
  current_page: number;
  total_pages: number;
  percentage: number;
  last_read_at: string | null;
  cover_path: string | null;
}

export type ViewMode = "single" | "double" | "vertical";
export type FitMode = "width" | "height" | "original";
export type ThemeMode = "light" | "dark";
export type SortField = "title" | "added_at" | "year" | "series" | "author";
export type SortOrder = "asc" | "desc";
