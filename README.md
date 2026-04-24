# Born To Track – Expense Tracker

A smart Android expense tracking app to help you stay on top of your finances — track income, manage budgets, visualise spending, and export reports.

## Features

- **Transactions**: Add / edit / delete income and expenses with categories, notes, and dates
- **Monthly Navigation**: Browse any past or future month with ← → arrows
- **Budgets**: Set monthly budgets per category with colour-coded progress bars (green → orange → red)
- **Analytics**: Pie chart (spending by category) + bar chart (monthly expense trend over 6 months)
- **Recurring**: Auto-insert monthly recurring transactions at the start of each month
- **PDF Export**: Generate and share a branded "Born To Track" PDF report of transactions
- **Search & Filter**: Real-time search bar + filter chips (All / Income / Expense)
- **Dark Mode**: Toggle between light and dark themes, persisted across launches

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Min / Target SDK | 24 (Android 7.0) / 36 (Android 14) |
| UI | Material Design 3 (MDC-Android) |
| Database | Room 2.6.1 (SQLite ORM) |
| Charts | MPAndroidChart v3.1.0 |
| Build | Gradle 9 with Kotlin DSL |

## Requirements

- Android Studio Hedgehog (2023.1) or later
- JDK 11+
- Min SDK: 24 (Android 7.0 Nougat)
- Target SDK: 36 (Android 14)

## Setup

1. Clone or download the project and open it in Android Studio
2. JitPack is already configured in `settings.gradle.kts` (required for MPAndroidChart):
   ```kotlin
   maven { url = uri("https://jitpack.io") }
   ```
3. Click **Sync Now** when prompted
4. Select a device / emulator and click **Run**

## Usage

| Action | How To |
|---|---|
| Add transaction | Tap the **+** FAB → fill in type, amount, category, date → **Save** |
| Edit transaction | Tap any transaction row → update fields → **Update Transaction** |
| Delete transaction | Long-press any transaction row → confirm delete |
| Set budgets | Menu (⋮) → **Budget** → enter limits per category → **Save Budgets** |
| View analytics | Menu (⋮) → **Analytics** |
| Export PDF | Menu (⋮) → **Export PDF** |
| Search | Type in the search bar at the top of the home screen |
| Filter by type | Tap **All / Income / Expense** chips |
| Toggle dark mode | Menu (⋮) → **Dark Mode** |
| Reset all data | Menu (⋮) → **Reset** → confirm |

## Database Schema

**transactions**

| Column | Type | Description |
|---|---|---|
| id | INTEGER PK | Auto-generated |
| type | TEXT | `"Income"` or `"Expense"` |
| amount | REAL | Transaction value |
| category | TEXT | e.g. Food, Travel, Bills … |
| date | TEXT | Format: `dd-MM-yyyy` |
| note | TEXT | Optional free-text note |
| isRecurring | INTEGER | 1 = auto-repeat each month |

**budgets**

| Column | Type | Description |
|---|---|---|
| id | INTEGER PK | Auto-generated |
| category | TEXT | e.g. Overall, Food, Travel … |
| limitAmount | REAL | Monthly spend limit (0 = disabled) |
| monthKey | TEXT | Format: `MM-yyyy` |

## Project Structure

```
app/src/main/
├── java/com/example/expense_tracker/
│   ├── MainActivity.java              # Home screen, month nav, charts, search
│   ├── adapter/
│   │   └── TransactionAdapter.java    # RecyclerView adapter
│   ├── database/
│   │   ├── AppDatabase.java           # Room singleton
│   │   ├── TransactionDao.java        # SQL queries for transactions
│   │   └── BudgetDao.java             # SQL queries for budgets
│   ├── model/
│   │   ├── Transaction.java           # Room entity
│   │   └── Budget.java                # Room entity
│   └── ui/
│       ├── AddTransactionActivity.java  # Add / edit transaction form
│       ├── AnalyticsActivity.java       # Charts & spending breakdown
│       └── BudgetActivity.java          # Budget management per category
└── res/
    ├── layout/          # XML layouts
    ├── values/          # Colors, strings, themes
    ├── values-night/    # Dark-mode theme overrides
    └── menu/            # Toolbar menu items
```

## Changelog

### v3.0 — Born To Track Rebrand
- 🏷️ Renamed app from "Expense Tracker" to **Born To Track**
- 🐛 Fixed crash in Budget screen when non-numeric limit is entered
- 🐛 Fixed transaction edit screen showing `100.0` instead of `100` for whole-number amounts
- 🐛 Fixed "Overall Budget" label showing incorrectly in Budget screen
- 📄 PDF report header and filename now branded as "Born To Track"
- 🔢 Version bumped to 3.0 (versionCode 4)

### v2.0
- Added budget management with per-category limits
- Added analytics screen with bar chart (6-month trend)
- Added recurring transactions
- Added dark mode toggle
- Added PDF export

### v1.0
- Initial release: transactions, pie chart, search & filter

---

**License**: Educational use

**Author**: Vishwesh Rajopadhye
