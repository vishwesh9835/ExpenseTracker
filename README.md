# Expense Tracker

Android expense tracking app with transaction management, budgets, analytics, and PDF export.

## Features

- **Transactions**: Add/edit/delete income and expenses with categories, notes, and dates
- **Budgets**: Set monthly budgets per category with visual progress tracking
- **Analytics**: Pie and bar charts showing spending patterns and trends
- **Recurring**: Auto-insert monthly recurring transactions
- **Export**: Generate PDF reports of transactions
- **Search & Filter**: Find transactions by category, type, or search term
- **Dark Mode**: Toggle between light and dark themes

## Tech Stack

- **Language**: Java 11
- **SDK**: Android 24-36 (Android 7.0 - 14)
- **UI**: Material Design 3
- **Database**: Room 2.6.1
- **Charts**: MPAndroidChart v3.1.0
- **Build**: Gradle with Kotlin DSL

## Requirements

- Android Studio Arctic Fox or later
- JDK 11+
- Min SDK: 24 (Android 7.0)
- Target SDK: 36 (Android 14)

## Setup

1. Open project in Android Studio
2. Add JitPack repository to `settings.gradle.kts`:
   ```kotlin
   maven { url = uri("https://jitpack.io") }
   ```
3. Sync Gradle
4. Build and run

## Usage

**Add Transaction**: Tap + button → Select type → Enter details → Save

**Set Budget**: Menu → Budget → Select category → Enter amount → Save

**View Analytics**: Menu → Analytics (see charts and trends)

**Export PDF**: Menu → Export PDF → Choose app to view

**Search/Filter**: Use search bar or filter chips (All/Income/Expense)

## Database Schema

**Transaction**: id, type, amount, category, date, note, isRecurring

**Budget**: id, category, budgetAmount, monthKey

## Project Structure

```
app/src/main/
├── java/com/example/expense_tracker/
│   ├── MainActivity.java
│   ├── adapter/TransactionAdapter.java
│   ├── database/
│   │   ├── AppDatabase.java
│   │   ├── TransactionDao.java
│   │   └── BudgetDao.java
│   ├── model/
│   │   ├── Transaction.java
│   │   └── Budget.java
│   └── ui/
│       ├── AddTransactionActivity.java
│       ├── AnalyticsActivity.java
│       └── BudgetActivity.java
└── res/
    ├── layout/
    ├── values/
    └── menu/
```

---


**License**: Educational use
**Author** : Vishwesh Santosh Rajopadhye
