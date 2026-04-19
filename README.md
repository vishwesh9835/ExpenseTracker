# Expense Tracker 

A modern Android expense tracking application built with Java and Material Design 3, featuring transaction management, budget tracking, analytics, and PDF export capabilities.

## 📱 Features

### Core Functionality
- **Transaction Management**
  - Add income and expense transactions
  - Edit and delete existing transactions
  - Categorize transactions (Food, Transport, Shopping, Bills, Entertainment, etc.)
  - Add optional notes to transactions
  - Date selection with calendar picker
  - Search and filter transactions

- **Recurring Transactions**
  - Mark transactions as recurring monthly
  - Automatic insertion of recurring transactions

- **Budget Management**
  - Set monthly budgets per category
  - Visual progress tracking
  - Budget alerts and status indicators
  - Budget vs actual spending comparison

- **Analytics Dashboard**
  - Category-wise expense breakdown with pie charts
  - Monthly spending trends with bar charts
  - Top spending categories
  - Budget performance analysis
  - Income vs Expense comparison

- **Data Management**
  - Export transactions to PDF
  - Month-by-month navigation
  - Data persistence using Room Database
  - Reset all data option

- **User Experience**
  - Material Design 3 UI/UX
  - Dark mode support
  - Real-time search and filtering
  - Smooth animations and transitions
  - Indian Rupee (₹) currency format

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Java 11
- **UI Framework**: Android SDK (API 24-36)
- **Design**: Material Design 3 Components
- **Database**: Room Persistence Library 2.6.1
- **Charts**: MPAndroidChart v3.1.0
- **Build System**: Gradle with Kotlin DSL

### Project Structure
```
ExpenseTracker_v2/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/expense_tracker/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── adapter/
│   │   │   │   │   └── TransactionAdapter.java
│   │   │   │   ├── database/
│   │   │   │   │   ├── AppDatabase.java
│   │   │   │   │   ├── TransactionDao.java
│   │   │   │   │   └── BudgetDao.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   └── Budget.java
│   │   │   │   └── ui/
│   │   │   │       ├── AddTransactionActivity.java
│   │   │   │       ├── AnalyticsActivity.java
│   │   │   │       └── BudgetActivity.java
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── activity_add_transaction.xml
│   │   │       │   ├── activity_analytics.xml
│   │   │       │   ├── activity_budget.xml
│   │   │       │   └── item_transaction.xml
│   │   │       ├── values/
│   │   │       │   ├── colors.xml
│   │   │       │   ├── themes.xml
│   │   │       │   └── strings.xml
│   │   │       └── menu/
│   │   │           └── main_menu.xml
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 🐛 Known Issues and Fixes

### ❌ BUILD ERROR: Resource Linking Failed

**Error Description:**
The app fails to build with the following error:
```
error: resource attr/colorTextPrimary (aka com.example.expense_tracker:attr/colorTextPrimary) not found.
error: resource attr/colorTextSecondary (aka com.example.expense_tracker:attr/colorTextSecondary) not found.
```

**Affected Files:**
- `app/src/main/res/layout/activity_add_transaction.xml` (lines 52, 64)
- `app/src/main/res/layout/activity_main.xml` (lines 161, 178)
- `app/src/main/res/layout/item_transaction.xml` (lines 58, 66, 77)
- `app/src/main/res/layout/activity_analytics.xml`
- `app/src/main/res/layout/activity_budget.xml`

**Root Cause:**
The XML layout files reference `?attr/colorTextPrimary` and `?attr/colorTextSecondary` as theme attributes, but these attributes are not defined in `themes.xml`. The colors only exist as static color resources in `colors.xml`.

**Solution:**

**Option 1: Define Theme Attributes (Recommended)**

Add the missing color attributes to your theme. Update `app/src/main/res/values/themes.xml`:

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Base.Theme.ExpenseTracker" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/md_theme_light_primary</item>
        <item name="colorOnPrimary">@color/md_theme_light_onPrimary</item>
        <item name="colorPrimaryContainer">@color/md_theme_light_primaryContainer</item>
        <item name="colorOnPrimaryContainer">@color/md_theme_light_onPrimaryContainer</item>
        <item name="android:statusBarColor">@color/md_theme_light_primary</item>
        <item name="android:windowBackground">@color/colorBackground</item>
        
        <!-- Add these lines -->
        <item name="colorTextPrimary">@color/colorTextPrimary</item>
        <item name="colorTextSecondary">@color/colorTextSecondary</item>
    </style>

    <style name="Theme.ExpenseTracker" parent="Base.Theme.ExpenseTracker" />
</resources>
```

Then create `app/src/main/res/values/attrs.xml` if it doesn't exist:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <attr name="colorTextPrimary" format="color" />
    <attr name="colorTextSecondary" format="color" />
</resources>
```

**Option 2: Use Direct Color References**

Replace all `?attr/colorTextPrimary` and `?attr/colorTextSecondary` references with `@color/colorTextPrimary` and `@color/colorTextSecondary` in the following files:
- activity_add_transaction.xml (lines 42, 83, 136, 213)
- activity_main.xml
- item_transaction.xml (lines 74, 86)
- activity_analytics.xml
- activity_budget.xml

Example change in `activity_add_transaction.xml`:
```xml
<!-- Before -->
<TextView
    android:textColor="?attr/colorTextSecondary"
    .../>

<!-- After -->
<TextView
    android:textColor="@color/colorTextSecondary"
    .../>
```

### 🔧 Additional Recommendations

1. **Dark Mode Support Enhancement**
   - Create a `values-night/colors.xml` file with dark theme color values
   - The current dark mode only toggles the system theme but doesn't provide custom dark colors

2. **MPAndroidChart Repository**
   - The dependency uses JitPack (`com.github.PhilJay:MPAndroidChart`)
   - Ensure JitPack repository is added in `settings.gradle.kts`:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```

3. **Potential Runtime Issues**
   - File provider configuration in AndroidManifest.xml should be verified
   - Check if `file_paths.xml` is properly configured for PDF export

## 📋 Requirements

### Development Environment
- **Android Studio**: Arctic Fox or later
- **JDK**: 11 or higher
- **Gradle**: 8.0+
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)

### Dependencies
```gradle
// Core Android
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.activity:activity:1.8.2")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

// Lifecycle Components
implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")

// Charts
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

## 🚀 Installation & Setup

### 1. Clone/Extract the Project
```bash
unzip ExpenseTracker.zip
cd ExpenseTracker_v2
```

### 2. Fix Build Errors
Apply the fixes mentioned in the "Known Issues" section above.

### 3. Open in Android Studio
- Open Android Studio
- File → Open → Select `ExpenseTracker_v2` folder
- Wait for Gradle sync to complete

### 4. Configure JitPack (if not present)
Add to `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 5. Build the Project
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### 6. Run on Device/Emulator
- Connect Android device (USB Debugging enabled) or start emulator
- Click Run ▶️ in Android Studio
- Or via command line:
```bash
./gradlew installDebug
```

## 💡 Usage Guide

### Adding a Transaction
1. Tap the **+** (FAB) button on the main screen
2. Select transaction type (Expense/Income)
3. Enter amount
4. Select category from dropdown
5. Choose date (defaults to today)
6. Add optional note
7. Toggle "Recurring Monthly" if needed
8. Tap "Save Transaction"

### Setting Budgets
1. Tap menu (⋮) → Budget
2. Select category
3. Enter budget amount
4. Tap "Save Budget"
5. View budget progress on main screen

### Viewing Analytics
1. Tap menu (⋮) → Analytics
2. View pie chart for category breakdown
3. View bar chart for monthly trends
4. See top spending categories

### Exporting Data
1. Tap menu (⋮) → Export PDF
2. Choose app to view/share PDF
3. PDF includes all transactions for current month

### Search & Filter
1. Use search bar to find transactions by category/note
2. Tap filter chips (All/Income/Expense) to filter by type
3. Navigate months using ← → arrows

## 🎨 Customization

### Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="md_theme_light_primary">#5B21B6</color>  <!-- Purple -->
<color name="colorIncome">#34D399</color>             <!-- Green -->
<color name="colorExpense">#FB923C</color>            <!-- Orange -->
```

### Categories
Edit category arrays in respective Activity files:
```java
// In AddTransactionActivity.java
String[] expenseCategories = {"Food", "Transport", "Shopping", ...};
String[] incomeCategories = {"Salary", "Freelance", "Investment", ...};
```

## 📊 Database Schema

### Transaction Table
```java
@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true) int id;
    String type;        // "Income" or "Expense"
    double amount;
    String category;
    String date;        // Format: "dd-MM-yyyy"
    String note;
    boolean isRecurring;
}
```

### Budget Table
```java
@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true) int id;
    String category;
    double budgetAmount;
    String monthKey;    // Format: "MM-yyyy"
}
```

## 🤝 Contributing

Contributions are welcome! Areas for improvement:
- [ ] Multi-currency support
- [ ] Cloud backup/sync
- [ ] Expense splitting
- [ ] Receipt photo attachment
- [ ] Customizable themes
- [ ] Widget for home screen
- [ ] Biometric authentication

## 📄 License

This project is provided as-is for educational purposes.

## 🔗 Resources

- [Material Design 3](https://m3.material.io/)
- [Android Room Database](https://developer.android.com/training/data-storage/room)
- [MPAndroidChart Documentation](https://github.com/PhilJay/MPAndroidChart)
- [Android Developer Guide](https://developer.android.com/)

## ⚠️ Important Notes

1. **First-time users**: Apply the theme attribute fix before building
2. **Data Privacy**: All data is stored locally on device
3. **Permissions**: App requires storage permission for PDF export
4. **Backup**: No cloud backup - data is device-specific

## 📞 Support

For issues or questions:
- Check the "Known Issues" section above
- Review Android Studio build logs
- Verify all dependencies are properly synced

---

