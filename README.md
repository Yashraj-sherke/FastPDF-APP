# ⚡ FastPDF

A fast, modern document viewer app built with **Kotlin** and **Jetpack Compose**.

Open and manage **PDF, Word, Excel, PowerPoint, Images, and Text files** — all in one app.

---

## 📸 Screenshots

<p align="center">
  <img src="screenshots/home.jpg" width="200" alt="Home Screen" />
  <img src="screenshots/tools.jpg" width="200" alt="Tools Screen" />
  <img src="screenshots/profile.jpg" width="200" alt="Profile Screen" />
  <img src="screenshots/drawer.jpg" width="200" alt="Navigation Drawer" />
</p>

<p align="center">
  <b>Home</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <b>Tools</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <b>Profile</b> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <b>Drawer</b>
</p>

---

## 📱 Features

- 🏠 **Home** — Recent files, favorites, AI summary cards, continue reading
- 🛠️ **Tools** — Merge, Split, Compress, OCR, Sign, Convert, Reorder Pages
- 📁 **Files** — Browse, sort, organize, multi-select batch operations
- 👤 **Profile** — Settings, storage stats, dark mode toggle
- 📷 **Scan** — Built-in document scanner with one-tap access from the bottom nav
- 🌙 **Dark Mode** — Full dark theme support across all screens
- 🗑️ **Recycle Bin** — Soft-deleted files with restore & auto-purge
- 🎉 **Onboarding** — First-launch welcome carousel
- 📊 **Storage Manager** — Visual storage breakdown, cache management
- ℹ️ **About** — App info, credits, rate & share
- 📋 **Document Info** — File metadata sheet with quick actions
- 📤 **Share** — Share documents and the app itself

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Architecture | Clean Architecture (UI / Domain / Data) |
| Database | Room (SQLite) |
| Preferences | Jetpack DataStore |
| ML | ML Kit (OCR, Document Scanner) |
| Min SDK | 24 (Android 7.0+) |
| Build | Gradle 8.5 + AGP 8.2.2 |

## 📂 Supported File Types

| Type | Extensions |
|------|-----------|
| PDF | `.pdf` |
| Word | `.doc`, `.docx` |
| Excel | `.xls`, `.xlsx`, `.csv` |
| PowerPoint | `.ppt`, `.pptx` |
| Images | `.jpg`, `.png`, `.webp`, `.gif` |
| Text | `.txt`, `.md`, `.rtf` |

## 🚀 Getting Started

1. Clone this repository
2. Open in **Android Studio** (latest stable)
3. Wait for Gradle sync to complete
4. Run on emulator or device

```bash
git clone https://github.com/Yashraj-sherke/FastPDF-APP.git
```

## 📁 Project Structure

```
com.fastpdf/
├── ui/
│   ├── screens/       # Home, Tools, Files, Profile, Reader, Scanner, About…
│   ├── components/    # BottomNavBar, DocumentFileItem, ToolCard, ProBanner…
│   ├── editor/        # AnnotationToolbar, DrawingCanvas, SignaturePad
│   ├── viewer/        # PdfViewer, ImageViewer, OfficeViewer, TextViewer
│   └── theme/         # Color, Typography, Shape, Theme (Light + Dark)
├── navigation/        # NavGraph, Screen routes, BottomNavItem
├── domain/model/      # DocumentFile, DocumentType, DrawingStroke, TextNote
└── data/
    ├── db/            # Room Database, FileDAO, FileRepository
    ├── CurrentFile.kt # Global file state holder
    └── ThemePreferences.kt  # DataStore-backed dark mode & onboarding prefs
```

## 📋 Roadmap

- [x] Project setup, navigation, base UI, theme
- [x] File system access, PDF rendering
- [x] Document viewer (Word, Excel, PPT, Images, Text)
- [x] Search, favorites persistence (Room DB)
- [x] Tools implementation (Merge, Split, Compress, OCR, Watermark, Protect, Convert)
- [x] AI features, dark mode, splash screen, share, app shortcuts
- [x] Batch operations, recycle bin, page reorder, onboarding
- [x] Storage manager, about screen, document info sheet
- [x] Center scan button in bottom nav bar
- [x] Full dark mode support across all screens
- [x] All button & tool functionality wired up

## 📄 License

This project is for educational and personal use.

---

Built with ❤️ by **Yashraj Sherke**
