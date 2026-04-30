# ⚡ FastPDF

A fast, modern document viewer app built with **Kotlin** and **Jetpack Compose**.

Open and manage **PDF, Word, Excel, PowerPoint, Images, and Text files** — all in one app.

---

## 📱 Features

- 🏠 **Home** — Recent files, favorites, AI summary cards, continue reading
- 🛠️ **Tools** — Merge, Split, Compress, OCR, Sign, Convert, Reorder Pages
- 📁 **Files** — Browse, sort, organize, multi-select batch operations
- 👤 **Profile** — Settings, storage stats, dark mode
- 🗑️ **Recycle Bin** — Soft-deleted files with restore & auto-purge
- 🎉 **Onboarding** — First-launch welcome carousel
- 📊 **Storage Manager** — Visual storage breakdown, cache management
- ℹ️ **About** — App info, credits, rate & share
- 📋 **Document Info** — File metadata sheet with quick actions

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Architecture | Clean Architecture (UI / Domain / Data) |
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
│   ├── screens/     # HomeScreen, ToolsScreen, FilesScreen, ProfileScreen, ReaderScreen
│   ├── components/  # DocumentFileItem, ToolCard, AiSummaryCard, BottomNavBar
│   └── theme/       # Color, Typography, Shape, Theme
├── navigation/      # NavGraph, Screen routes, BottomNavItem
├── domain/model/    # DocumentFile, DocumentType
└── data/            # Repository layer (coming soon)
```

## 📋 Roadmap

- [x] Day 1: Project setup, navigation, base UI, theme
- [ ] Day 2: File system access, PDF rendering
- [ ] Day 3: Document viewer (Word, Excel, PPT)
- [x] Day 4: Search, favorites persistence
- [x] Day 5: Tools implementation (merge, split, compress, OCR, watermark, protect, convert)
- [x] Day 6: AI features, dark mode, splash screen, share, app shortcuts
- [x] Day 7: Batch operations, recycle bin, page reorder, onboarding
- [x] Day 8: Storage manager, about screen, document info sheet, dark mode polish

## 📄 License

This project is for educational and personal use.

---

Built with ❤️ by **Yashraj Sherke**
