package com.example.expense_tracker.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker.R;
import com.example.expense_tracker.database.AppDatabase;
import com.example.expense_tracker.model.Budget;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class BudgetActivity extends AppCompatActivity {

    String monthKey;
    String[] categories = {"Overall", "Food", "Travel", "Bills", "Shopping",
                           "Health", "Education", "Entertainment"};

    // Map: category → TextInputEditText
    Map<String, TextInputEditText> fieldMap = new LinkedHashMap<>();
    Map<String, android.widget.ProgressBar> progressMap = new LinkedHashMap<>();
    Map<String, TextView> statusMap = new LinkedHashMap<>();

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        com.google.android.material.appbar.MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Monthly Budgets");
        }

        monthKey = getIntent().getStringExtra("monthKey");
        if (monthKey == null) {
            Calendar c = Calendar.getInstance();
            monthKey = String.format(Locale.getDefault(), "%02d-%04d",
                    c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
        }

        container = findViewById(R.id.budgetContainer);
        buildUI();

        findViewById(R.id.btnSaveBudgets).setOnClickListener(v -> saveBudgets());
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            Map<String, Budget> existing = new HashMap<>();
            for (Budget b : db.budgetDao().getBudgetsForMonth(monthKey)) {
                existing.put(b.getCategory(), b);
            }
            // compute actual spend per category
            double totalExpense = db.transactionDao().getTotalExpenseForMonth(monthKey);
            Map<String, Double> spendMap = new HashMap<>();
            for (com.example.expense_tracker.model.Transaction t :
                    db.transactionDao().getExpensesByMonth(monthKey)) {
                String cat = t.getDisplayLabel();
                spendMap.put(cat, spendMap.getOrDefault(cat, 0.0) + t.getAmount());
            }
            spendMap.put("Overall", totalExpense);

            runOnUiThread(() -> {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
                for (String cat : categories) {
                    // Section card
                    com.google.android.material.card.MaterialCardView card =
                            new com.google.android.material.card.MaterialCardView(this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 24);
                    card.setLayoutParams(lp);
                    card.setCardElevation(2f);
                    card.setRadius(16f * getResources().getDisplayMetrics().density);

                    LinearLayout inner = new LinearLayout(this);
                    inner.setOrientation(LinearLayout.VERTICAL);
                    inner.setPadding(48, 40, 48, 40);

                    // Label
                    TextView lbl = new TextView(this);
                    lbl.setText(cat.equals("Overall") ? "Overall Monthly Budget" : cat + " Budget");
                    lbl.setTextSize(14f);
                    lbl.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    lbl.setTextColor(android.graphics.Color.parseColor("#1F2937"));
                    inner.addView(lbl);

                    // Amount field
                    com.google.android.material.textfield.TextInputLayout til =
                            new com.google.android.material.textfield.TextInputLayout(this);
                    til.setHint("Limit (₹) — 0 to disable");
                    LinearLayout.LayoutParams tilLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    tilLp.setMargins(0, 16, 0, 0);
                    til.setLayoutParams(tilLp);

                    TextInputEditText et = new TextInputEditText(this);
                    et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    Budget b = existing.get(cat);
                    if (b != null && b.getLimitAmount() > 0)
                        et.setText(String.valueOf((int) b.getLimitAmount()));
                    til.addView(et);
                    inner.addView(til);
                    fieldMap.put(cat, et);

                    // Progress bar
                    double spend = spendMap.getOrDefault(cat, 0.0);
                    double limit = (b != null) ? b.getLimitAmount() : 0;
                    if (limit > 0) {
                        int pct = (int) Math.min(100, (spend / limit) * 100);
                        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                        pb.setMax(100);
                        pb.setProgress(pct);
                        int color = pct >= 100
                                ? android.graphics.Color.parseColor("#DC2626")
                                : pct >= 80
                                    ? android.graphics.Color.parseColor("#F97316")
                                    : android.graphics.Color.parseColor("#6366F1");
                        pb.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
                        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 24);
                        pbLp.setMargins(0, 16, 0, 0);
                        pb.setLayoutParams(pbLp);
                        inner.addView(pb);
                        progressMap.put(cat, pb);

                        TextView status = new TextView(this);
                        status.setText("₹" + fmt.format(spend) + " of ₹" + fmt.format(limit) + " (" + pct + "%)");
                        status.setTextSize(11f);
                        status.setTextColor(color);
                        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        sLp.setMargins(0, 4, 0, 0);
                        status.setLayoutParams(sLp);
                        inner.addView(status);
                        statusMap.put(cat, status);
                    }

                    card.addView(inner);
                    container.addView(card);
                }
            });
        });
    }

    private void saveBudgets() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            for (String cat : categories) {
                TextInputEditText et = fieldMap.get(cat);
                if (et == null) continue;
                String val = et.getText() != null ? et.getText().toString().trim() : "";
                double limit;
                try {
                    limit = val.isEmpty() ? 0 : Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    limit = 0;
                }
                Budget existing = db.budgetDao().getBudgetForCategory(cat, monthKey);
                if (existing == null) {
                    db.budgetDao().insert(new Budget(cat, limit, monthKey));
                } else {
                    existing.setLimitAmount(limit);
                    db.budgetDao().update(existing);
                }
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Budgets saved!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
