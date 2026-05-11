# COMI - Comic Reader

A comprehensive, high-feature comic reader web application with support for CBZ, CBR, and PDF formats.

## Features

### Core Reader
- **Multi-format support**: CBZ (ZIP), CBR (RAR), and PDF comic files
- **Three viewing modes**: Single page, double page spread, and vertical scroll
- **Fit modes**: Fit to width, fit to height, original size with zoom controls
- **Keyboard navigation**: Arrow keys, A/D, Space, Home/End, F for fullscreen
- **Click navigation**: Click left/right halves of the page to navigate

### Library Management
- **Upload comics**: Drag-and-drop or browse to upload comic files
- **Library scanning**: Automatically scan a directory for comic files
- **Search & filter**: Search by title, author, or series
- **Sort options**: Sort by date added, title, year, author, or series
- **Tag system**: Organize comics with custom tags
- **Grid/list views**: Switch between grid and list display modes
- **Metadata extraction**: Automatic extraction from ComicInfo.xml (CBZ) and PDF metadata

### Reading Progress
- **Automatic tracking**: Reading progress saved automatically as you read
- **Continue reading**: Quick access to recently read comics on the home page
- **Progress indicators**: Visual progress bars on comic cards
- **Completion tracking**: Tracks when you finish reading a comic

### Bookmarks
- **Page bookmarks**: Bookmark any page with optional labels and notes
- **Bookmark panel**: Quick access to all bookmarks within the reader
- **Jump to bookmark**: Click any bookmark to jump to that page

### UI/UX
- **Dark/light themes**: Toggle between dark and light modes
- **Responsive design**: Works on desktop and tablet screens
- **Fullscreen mode**: Immersive reading with fullscreen support
- **Cover thumbnails**: Automatic thumbnail generation for library view

## Tech Stack

### Backend
- **FastAPI** - Modern async Python web framework
- **SQLAlchemy** (async) - Database ORM with SQLite
- **PyMuPDF** (fitz) - PDF parsing and rendering
- **Pillow** - Image processing and thumbnail generation
- **rarfile** - CBR (RAR) archive support

### Frontend
- **React 18** - UI framework
- **TypeScript** - Type-safe JavaScript
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **Vitest** - Testing framework

## Getting Started

### Prerequisites
- Python 3.11+
- Node.js 22+
- `unrar` (for CBR support): `sudo apt-get install unrar-free`

### Backend Setup

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
uvicorn app.main:app --reload
```

The API server runs at http://localhost:8000. API docs at http://localhost:8000/docs.

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

The frontend dev server runs at http://localhost:5173 with API proxy to the backend.

### Docker Setup

```bash
docker-compose up --build
```

Access the app at http://localhost:3000.

## Running Tests

### Backend Tests
```bash
cd backend
pytest -v
```

### Frontend Tests
```bash
cd frontend
npm test
```

### Linting
```bash
# Backend
cd backend
ruff check app/ tests/

# Frontend
cd frontend
npx tsc --noEmit
```

## API Endpoints

### Library
- `GET /api/library/comics` - List comics (paginated, searchable, sortable)
- `GET /api/library/comics/{id}` - Get comic details
- `PUT /api/library/comics/{id}` - Update comic metadata
- `DELETE /api/library/comics/{id}` - Delete a comic
- `POST /api/library/upload` - Upload a comic file
- `POST /api/library/scan` - Scan library directory for comics
- `GET /api/library/tags` - List all tags
- `GET /api/library/series` - List all series

### Reader
- `GET /api/comics/{id}/page/{page}` - Get a page image
- `GET /api/comics/{id}/page-info/{page}` - Get page metadata
- `GET /api/comics/{id}/thumbnail` - Get cover thumbnail

### Progress & Bookmarks
- `GET /api/progress/{comic_id}` - Get reading progress
- `PUT /api/progress/{comic_id}` - Update reading progress
- `GET /api/progress/` - Get reading history
- `POST /api/progress/{comic_id}/bookmarks` - Add bookmark
- `GET /api/progress/{comic_id}/bookmarks` - List bookmarks
- `DELETE /api/progress/bookmarks/{id}` - Delete bookmark

### System
- `GET /api/health` - Health check

## Project Structure

```
COMI/
├── backend/
│   ├── app/
│   │   ├── models/        # SQLAlchemy models
│   │   ├── routers/       # FastAPI route handlers
│   │   ├── schemas/       # Pydantic schemas
│   │   ├── services/      # Business logic
│   │   ├── config.py      # App configuration
│   │   ├── database.py    # Database setup
│   │   └── main.py        # App entry point
│   ├── tests/             # Backend tests
│   └── pyproject.toml
├── frontend/
│   ├── src/
│   │   ├── api/           # API client
│   │   ├── components/    # React components
│   │   ├── context/       # React contexts
│   │   ├── hooks/         # Custom hooks
│   │   ├── pages/         # Page components
│   │   └── types/         # TypeScript types
│   ├── __tests__/         # Frontend tests
│   └── package.json
├── .github/workflows/     # CI configuration
├── docker-compose.yml     # Docker setup
└── README.md
```

## License

MIT
