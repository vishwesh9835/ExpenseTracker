package com.example.expense_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.adapter.TransactionAdapter;
import com.example.expense_tracker.database.AppDatabase;
import com.example.expense_tracker.model.Transaction;
import com.example.expense_tracker.ui.AddTransactionActivity;
import com.example.expense_tracker.ui.AnalyticsActivity;
import com.example.expense_tracker.ui.BudgetActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TransactionAdapter adapter;
    List<Transaction> transactionList = new ArrayList<>();

    TextView tvBalance, tvIncome, tvExpense, tvMonthLabel;
    LinearLayout tvEmptyState;
    View chartCard;
    LinearLayout budgetSection;
    TextView tvBudgetUsed, tvBudgetLimit, tvBudgetStatus;
    android.widget.ProgressBar budgetProgressBar;
    com.google.android.material.floatingactionbutton.FloatingActionButton btnAdd;
    PieChart pieChart;
    com.google.android.material.textfield.TextInputEditText etSearch;
    com.google.android.material.chip.ChipGroup chipGroupFilter;
    com.google.android.material.chip.Chip chipAll, chipIncome, chipExpense;

    String currentMonthKey;
    String activeFilter = "All";
    String searchQuery = "";

    static final String PREFS    = "et_prefs";
    static final String KEY_DARK = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean dark = prefs.getBoolean(KEY_DARK, false);
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        setupGradient();
        setupAdapter();
        setupMonthNavigation();
        setupSearchAndFilter();
        setupFab();
        setupRecurringAutoInsert();
    }

    private void bindViews() {
        recyclerView      = findViewById(R.id.recyclerView);
        tvBalance         = findViewById(R.id.tvBalance);
        tvIncome          = findViewById(R.id.tvIncome);
        tvExpense         = findViewById(R.id.tvExpense);
        tvMonthLabel      = findViewById(R.id.tvMonthLabel);
        tvEmptyState      = findViewById(R.id.tvEmptyState);
        chartCard         = findViewById(R.id.chartCard);
        budgetSection     = findViewById(R.id.budgetSection);
        tvBudgetUsed      = findViewById(R.id.tvBudgetUsed);
        tvBudgetLimit     = findViewById(R.id.tvBudgetLimit);
        tvBudgetStatus    = findViewById(R.id.tvBudgetStatus);
        budgetProgressBar = findViewById(R.id.budgetProgressBar);
        btnAdd            = findViewById(R.id.btnAdd);
        pieChart          = findViewById(R.id.pieChart);
        etSearch          = findViewById(R.id.etSearch);
        chipGroupFilter   = findViewById(R.id.chipGroupFilter);
        chipAll           = findViewById(R.id.chipAll);
        chipIncome        = findViewById(R.id.chipIncome);
        chipExpense       = findViewById(R.id.chipExpense);
    }

    private void setupGradient() {
        android.widget.LinearLayout card = findViewById(R.id.balanceCardInnerLayout);
        if (card != null) {
            android.graphics.drawable.GradientDrawable g =
                    new android.graphics.drawable.GradientDrawable(
                            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                            new int[]{android.graphics.Color.parseColor("#4338CA"),
                                    android.graphics.Color.parseColor("#6366F1")});
            g.setCornerRadius(24f * getResources().getDisplayMetrics().density);
            card.setBackground(g);
        }
    }

    private void setupAdapter() {
        adapter = new TransactionAdapter(transactionList, new TransactionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Transaction t) {
                Intent i = new Intent(MainActivity.this, AddTransactionActivity.class);
                i.putExtra("id", t.getId());
                i.putExtra("type", t.getType());
                i.putExtra("amount", t.getAmount());
                i.putExtra("category", t.getCategory());
                i.putExtra("date", t.getDate());
                i.putExtra("note", t.getNote());
                i.putExtra("isRecurring", t.isRecurring());
                startActivity(i);
            }

            @Override
            public void onItemLongClick(Transaction t, int position) {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Transaction")
                        .setMessage("Delete " + t.getType() + " of ₹" + fmt.format(t.getAmount()) + "?")
                        .setPositiveButton("Delete", (d, w) ->
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    AppDatabase.getInstance(MainActivity.this).transactionDao().delete(t);
                                    runOnUiThread(() -> {
                                        transactionList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(position, transactionList.size());
                                        refreshSummary();
                                    });
                                }))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupMonthNavigation() {
        Calendar cal = Calendar.getInstance();
        currentMonthKey = String.format(Locale.getDefault(), "%02d-%04d",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        updateMonthLabel();
        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> shiftMonth(-1));
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> shiftMonth(+1));
    }

    private void shiftMonth(int delta) {
        String[] parts = currentMonthKey.split("-");
        int month = Integer.parseInt(parts[0]) + delta;
        int year  = Integer.parseInt(parts[1]);
        if (month > 12) { month = 1;  year++; }
        if (month < 1)  { month = 12; year--; }
        currentMonthKey = String.format(Locale.getDefault(), "%02d-%04d", month, year);
        updateMonthLabel();
        loadData();
    }

    private void updateMonthLabel() {
        try {
            Date d = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(currentMonthKey);
            tvMonthLabel.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(d));
        } catch (Exception e) {
            tvMonthLabel.setText(currentMonthKey);
        }
    }

    private void setupSearchAndFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                applyFilterAndSearch();
            }
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) { chipAll.setChecked(true); return; }
            int id = ids.get(0);
            if (id == R.id.chipAll)          activeFilter = "All";
            else if (id == R.id.chipIncome)  activeFilter = "Income";
            else if (id == R.id.chipExpense) activeFilter = "Expense";
            applyFilterAndSearch();
        });
    }

    private void setupFab() {
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class)));
    }

    private void setupRecurringAutoInsert() {
        String todayKey = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getString("last_recurring_month", "").equals(todayKey)) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Transaction> recurring = AppDatabase.getInstance(this)
                    .transactionDao().getRecurringTransactions();
            if (recurring.isEmpty()) return;
            String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            for (Transaction t : recurring) {
                Transaction copy = new Transaction(t.getType(), t.getAmount(),
                        t.getCategory(), today, t.getNote() + " (auto)", true);
                AppDatabase.getInstance(this).transactionDao().insert(copy);
            }
            prefs.edit().putString("last_recurring_month", todayKey).apply();
            runOnUiThread(this::loadData);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Transaction> all = AppDatabase.getInstance(this)
                    .transactionDao().getTransactionsByMonth(currentMonthKey);
            runOnUiThread(() -> {
                transactionList.clear();
                transactionList.addAll(all);
                applyFilterAndSearch();
                refreshSummary();
                updateBudgetBar();
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilterAndSearch() {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            boolean typeOk   = activeFilter.equals("All") || t.getType().equalsIgnoreCase(activeFilter);
            boolean searchOk = searchQuery.isEmpty()
                    || t.getDisplayLabel().toLowerCase().contains(searchQuery.toLowerCase())
                    || (t.getNote() != null && t.getNote().toLowerCase().contains(searchQuery.toLowerCase()));
            if (typeOk && searchOk) filtered.add(t);
        }

        adapter.updateList(filtered);
        adapter.notifyDataSetChanged();

        boolean empty = filtered.isEmpty();
        tvEmptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);

        updateChart(filtered);
    }

    @SuppressLint("SetTextI18n")
    private void refreshSummary() {
        double income = 0, expense = 0;
        for (Transaction t : transactionList) {
            if ("Income".equalsIgnoreCase(t.getType())) income += t.getAmount();
            else expense += t.getAmount();
        }
        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        tvIncome.setText("₹" + fmt.format(income));
        tvExpense.setText("₹" + fmt.format(expense));
        double balance = income - expense;
        tvBalance.setText("₹" + fmt.format(Math.abs(balance)));
        tvBalance.setTextColor(balance < 0
                ? android.graphics.Color.parseColor("#FCA5A5")
                : android.graphics.Color.WHITE);
    }

    @SuppressLint("SetTextI18n")
    private void updateBudgetBar() {
        Executors.newSingleThreadExecutor().execute(() -> {
            com.example.expense_tracker.model.Budget b =
                    AppDatabase.getInstance(this).budgetDao()
                            .getBudgetForCategory("Overall", currentMonthKey);
            double totalExpense = AppDatabase.getInstance(this)
                    .transactionDao().getTotalExpenseForMonth(currentMonthKey);

            runOnUiThread(() -> {
                if (b == null || b.getLimitAmount() <= 0) {
                    budgetSection.setVisibility(View.GONE);
                    return;
                }
                budgetSection.setVisibility(View.VISIBLE);
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
                double limit = b.getLimitAmount();
                int pct = (int) Math.min(100, (totalExpense / limit) * 100);
                budgetProgressBar.setProgress(pct);
                tvBudgetUsed.setText("₹" + fmt.format(totalExpense));
                tvBudgetLimit.setText(" / ₹" + fmt.format(limit));

                int color;
                String status;
                if (pct >= 100) {
                    color = android.graphics.Color.parseColor("#DC2626");
                    status = "⚠ Budget exceeded!";
                } else if (pct >= 80) {
                    color = android.graphics.Color.parseColor("#F97316");
                    status = "🔔 " + pct + "% used — nearing limit";
                } else {
                    color = android.graphics.Color.parseColor("#6366F1");
                    status = pct + "% of budget used";
                }
                tvBudgetStatus.setText(status);
                tvBudgetStatus.setTextColor(color);
                budgetProgressBar.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(color));
            });
        });
    }

    private void updateChart(List<Transaction> list) {
        Map<String, Float> map = new LinkedHashMap<>();
        for (Transaction t : list) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                String cat = t.getDisplayLabel();
                map.put(cat, map.getOrDefault(cat, 0f) + (float) t.getAmount());
            }
        }

        if (map.isEmpty()) {
            chartCard.setVisibility(View.GONE);
            return;
        }
        chartCard.setVisibility(View.VISIBLE);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(android.graphics.Color.DKGRAY);
        pieChart.setUsePercentValues(true);

        com.github.mikephil.charting.components.Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : map.entrySet())
            entries.add(new PieEntry(e.getValue(), e.getKey()));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(android.graphics.Color.parseColor("#6366F1"),
                android.graphics.Color.parseColor("#8B5CF6"),
                android.graphics.Color.parseColor("#EC4899"),
                android.graphics.Color.parseColor("#F43F5E"),
                android.graphics.Color.parseColor("#06B6D4"),
                android.graphics.Color.parseColor("#F97316"),
                android.graphics.Color.parseColor("#16A34A"),
                android.graphics.Color.parseColor("#D97706"));
        ds.setValueLinePart1OffsetPercentage(80f);
        ds.setValueLinePart1Length(0.4f);
        ds.setValueLinePart2Length(0.2f);
        ds.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        ds.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        ds.setValueTextColor(android.graphics.Color.DKGRAY);
        ds.setValueLineColor(android.graphics.Color.DKGRAY);

        PieData pd = new PieData(ds);
        pd.setValueTextSize(10f);
        pd.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));
        pieChart.setData(pd);
        pieChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_pdf)  { exportPdf(); return true; }
        if (id == R.id.action_reset)       { confirmReset(); return true; }
        if (id == R.id.action_analytics)   { startActivity(new Intent(this, AnalyticsActivity.class)); return true; }
        if (id == R.id.action_budget)      {
            Intent i = new Intent(this, BudgetActivity.class);
            i.putExtra("monthKey", currentMonthKey);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_dark_mode)   { toggleDarkMode(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void toggleDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);
        prefs.edit().putBoolean(KEY_DARK, !isDark).apply();
        AppCompatDelegate.setDefaultNightMode(isDark
                ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        recreate();
    }

    private void confirmReset() {
        androidx.appcompat.app.AlertDialog dlg =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Reset All Data")
                        .setMessage("This will permanently delete ALL transactions. Continue?")
                        .setPositiveButton("Reset", (d, w) ->
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    AppDatabase.getInstance(this).transactionDao().deleteAll();
                                    runOnUiThread(() -> {
                                        transactionList.clear();
                                        adapter.notifyDataSetChanged();
                                        refreshSummary();
                                        chartCard.setVisibility(View.GONE);
                                        budgetSection.setVisibility(View.GONE);
                                        Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                                    });
                                }))
                        .setNegativeButton("Cancel", null)
                        .show();
        Button b = dlg.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        if (b != null) b.setTextColor(android.graphics.Color.parseColor("#DC2626"));
    }

    private void exportPdf() {
        android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pi =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create();
        android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pi);
        android.graphics.Canvas c = page.getCanvas();

        android.graphics.Paint title = new android.graphics.Paint();
        title.setTextSize(20f); title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setColor(android.graphics.Color.parseColor("#4338CA"));

        android.graphics.Paint body = new android.graphics.Paint();
        body.setTextSize(11f); body.setColor(android.graphics.Color.parseColor("#1F2937"));

        android.graphics.Paint green = new android.graphics.Paint();
        green.setTextSize(11f); green.setColor(android.graphics.Color.parseColor("#16A34A"));

        android.graphics.Paint red = new android.graphics.Paint();
        red.setTextSize(11f); red.setColor(android.graphics.Color.parseColor("#DC2626"));

        android.graphics.Paint line = new android.graphics.Paint();
        line.setColor(android.graphics.Color.parseColor("#E5E7EB")); line.setStrokeWidth(1f);

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        int y = 50;
        c.drawText("Born To Track — " + tvMonthLabel.getText(), 50, y, title); y += 22;
        c.drawText("Exported: " + today, 50, y, body); y += 28;
        c.drawLine(50, y, 545, y, line); y += 20;

        double inc = 0, exp = 0;
        for (Transaction t : transactionList) {
            if ("Income".equalsIgnoreCase(t.getType())) inc += t.getAmount(); else exp += t.getAmount();
        }
        c.drawText("Balance: Rs." + fmt.format(inc - exp), 50, y, body); y += 18;
        c.drawText("Income: Rs." + fmt.format(inc), 50, y, green); y += 18;
        c.drawText("Expenses: Rs." + fmt.format(exp), 50, y, red); y += 28;
        c.drawLine(50, y, 545, y, line); y += 18;

        android.graphics.Paint hdr = new android.graphics.Paint();
        hdr.setTextSize(11f); hdr.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        hdr.setColor(android.graphics.Color.parseColor("#374151"));
        c.drawText("Date", 50, y, hdr); c.drawText("Category", 160, y, hdr);
        c.drawText("Type", 310, y, hdr); c.drawText("Amount", 420, y, hdr);
        y += 8; c.drawLine(50, y, 545, y, line); y += 18;

        for (Transaction t : transactionList) {
            android.graphics.Paint p = "Income".equalsIgnoreCase(t.getType()) ? green : red;
            p.setTextSize(11f); body.setTextSize(11f);
            c.drawText(t.getDate(), 50, y, body); c.drawText(t.getDisplayLabel(), 160, y, body);
            c.drawText(t.getType(), 310, y, p); c.drawText("Rs." + fmt.format(t.getAmount()), 420, y, p);
            y += 20;
            if (y > 810) {
                pdf.finishPage(page);
                pi = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pdf.getPages().size() + 1).create();
                page = pdf.startPage(pi); c = page.getCanvas(); y = 50;
            }
        }
        pdf.finishPage(page);

        String fn = "BornToTrack_" + System.currentTimeMillis() + ".pdf";
        java.io.File file = new java.io.File(
                getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), fn);
        try {
            pdf.writeTo(new java.io.FileOutputStream(file));
            pdf.close();
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF"));
            Toast.makeText(this, "PDF exported!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
