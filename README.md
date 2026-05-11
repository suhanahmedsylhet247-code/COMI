# COMI - Comic Reader for Android

A native Android comic reader app inspired by **Tachiyomi**, **Kotatsu**, and **Komikku**. Built with Kotlin, Jetpack Compose, and Material 3.

## Features

### Comic Reader
- **Multi-format support**: CBZ (ZIP archives), CBR (RAR archives), PDF
- **Four reading modes**: Left-to-Right, Right-to-Left (Manga), Vertical, Webtoon (continuous scroll)
- **Tap navigation**: Tap left/right thirds of screen to navigate, center to toggle controls
- **Page slider**: Scrub through pages with the bottom slider
- **Fullscreen reading**: Immersive mode with animated controls overlay
- **Auto-save progress**: Reading position saved automatically

### Library Management
- **Import comics**: Pick CBZ/CBR/PDF files from your device
- **Grid & list views**: Switch between compact grid and detailed list
- **Search**: Find comics by title, author, or series
- **Sort options**: By title, recently added, recently read, or author
- **Favorites**: Mark comics as favorites with quick filter
- **Delete management**: Long-press to remove comics
- **Metadata extraction**: Reads ComicInfo.xml from CBZ files automatically

### Reading Progress
- **Automatic tracking**: Progress saved on every page turn
- **History screen**: See all recently read comics with progress bars
- **Continue reading**: Jump right back where you left off
- **Completion detection**: Tracks when you finish a comic

### Bookmarks
- **Bookmark any page**: Quick-toggle bookmark on current page
- **Bookmark panel**: Bottom sheet showing all bookmarks for the current comic
- **Jump to bookmark**: Tap any bookmark to navigate to that page
- **Labels & notes**: Add context to your bookmarks

### Extension System
- **Source API**: Plugin interface for third-party manga sources (like Tachiyomi)
- **Source registry**: Register/unregister manga source extensions
- **Local source**: Built-in source for local file management
- **Browse screen**: UI placeholder for extension discovery (coming soon)

### Settings
- **Dark/Light theme**: Toggle between themes with Material You dynamic colors
- **Default reading mode**: Set your preferred reading direction
- **Storage management**: Internal storage for imported comics
- **Extension management**: Configure source extensions

### Download Manager
- **Foreground service**: Background download with notification
- **Download queue**: Manage pending and active downloads
- **Progress tracking**: Track download completion percentage

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| DI | Hilt (Dagger) |
| Database | Room |
| Image Loading | Coil |
| Navigation | Compose Navigation |
| PDF | Android PdfRenderer |
| Network | OkHttp |
| Serialization | kotlinx.serialization |
| Testing | JUnit 4 + MockK + Coroutines Test |

## Project Structure

```
COMI/
├── app/src/main/java/com/comi/reader/
│   ├── ComiApplication.kt          # Hilt application
│   ├── data/
│   │   ├── local/
│   │   │   ├── ComiDatabase.kt     # Room database
│   │   │   ├── dao/ComicDao.kt     # Data access objects
│   │   │   ├── entity/Entities.kt  # Database entities
│   │   │   └── converter/          # Type converters
│   │   ├── parser/ComicParser.kt   # CBZ/CBR/PDF parsing
│   │   └── repository/
│   │       ├── ComicRepository.kt  # Data repository
│   │       └── DownloadService.kt  # Download foreground service
│   ├── di/AppModule.kt             # Hilt dependency injection
│   ├── domain/model/Models.kt      # Domain models
│   ├── extension/
│   │   ├── api/                    # Source extension API
│   │   └── model/                  # Extension data models
│   ├── ui/
│   │   ├── MainActivity.kt        # Main activity + navigation
│   │   ├── library/                # Library screen + ViewModel
│   │   ├── reader/                 # Reader screen + ViewModel
│   │   ├── history/                # History screen + ViewModel
│   │   ├── settings/               # Settings screen
│   │   ├── components/             # Shared UI components
│   │   ├── navigation/             # Navigation routes
│   │   └── theme/                  # Material theme
│   └── util/                       # Utility classes
├── app/src/test/                    # Unit tests
├── .github/workflows/ci.yml        # GitHub Actions CI
└── build.gradle.kts                # Build configuration
```

## Building

### Prerequisites
- Android Studio Ladybug (2024.2.1) or later
- JDK 17
- Android SDK 35

### Build from Android Studio
1. Clone the repo: `git clone https://github.com/suhanahmedsylhet247-code/COMI.git`
2. Open in Android Studio
3. Sync Gradle
4. Run on device/emulator (API 26+)

### Build from CLI
```bash
# Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

## Testing

```bash
# Run all unit tests
./gradlew test

# Tests cover:
# - Comic format detection (CBZ, CBR, PDF, ZIP)
# - Domain model validation
# - File utility functions
# - Extension system (source registry, local source)
# - Navigation routes
```

## Architecture

COMI follows **MVVM + Clean Architecture** principles:

```
UI Layer (Compose) → ViewModel → Repository → Data Sources (Room, Parser, Network)
```

- **UI Layer**: Jetpack Compose screens with state hoisting
- **ViewModel**: Manages UI state with StateFlow, handles user actions
- **Repository**: Single source of truth, coordinates data sources
- **Data Layer**: Room database, ComicParser for file reading, network for extensions

## Keyboard Shortcuts (with physical keyboard)

| Key | Action |
|-----|--------|
| Volume Up / D-Pad Right | Next page |
| Volume Down / D-Pad Left | Previous page |

## Supported Formats

| Format | Extension | Support |
|--------|-----------|---------|
| CBZ | .cbz, .zip | Full (ZIP with images) |
| PDF | .pdf | Full (Android PdfRenderer) |
| CBR | .cbr | Partial (requires native RAR lib) |

## License

MIT
