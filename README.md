# ⚡ FastPDF

A fast, modern document viewer app built with **Kotlin** and **Jetpack Compose**.

Open and manage **PDF, Word, Excel, PowerPoint, Images, and Text files** — all in one app.

---

## 📱 Features

- 🏠 **Home** — Recent files, favorites, AI summary cards
- 🛠️ **Tools** — Merge, Split, Compress, OCR, Sign, Convert
- 📁 **Files** — Browse, sort, and organize all your documents
- 👤 **Profile** — Settings, storage stats, cloud sync

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
- [ ] Day 4: Search, favorites persistence
- [ ] Day 5: Tools implementation (merge, split, compress)
- [ ] Day 6: Cloud sync, dark mode
- [ ] Day 7: Polish, testing, release build

## 📄 License

This project is for educational and personal use.

---

Built with ❤️ by **Yashraj Sherke**
