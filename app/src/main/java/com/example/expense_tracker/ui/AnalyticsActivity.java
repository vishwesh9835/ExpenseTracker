package com.example.expense_tracker.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker.R;
import com.example.expense_tracker.database.AppDatabase;
import com.example.expense_tracker.model.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class AnalyticsActivity extends AppCompatActivity {

    BarChart barChart;
    LinearLayout topExpensesContainer, categoryBreakdownContainer;
    TextView tvSummaryMonth, tvAvgExpense, tvTopCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        com.google.android.material.appbar.MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }

        barChart = findViewById(R.id.barChart);
        topExpensesContainer = findViewById(R.id.topExpensesContainer);
        categoryBreakdownContainer = findViewById(R.id.categoryBreakdownContainer);
        tvSummaryMonth = findViewById(R.id.tvSummaryMonth);
        tvAvgExpense   = findViewById(R.id.tvAvgExpense);
        tvTopCategory  = findViewById(R.id.tvTopCategory);

        loadAnalytics();
    }

    @SuppressLint("SetTextI18n")
    private void loadAnalytics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<String> months = db.transactionDao().getDistinctMonths();
            List<Transaction> allTx = db.transactionDao().getAllTransactions();

            // Last 6 months bar data
            List<String> last6 = months.size() > 6 ? months.subList(0, 6) : months;
            Collections.reverse(last6);
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> barLabels  = new ArrayList<>();
            int idx = 0;
            for (String mk : last6) {
                double exp = db.transactionDao().getTotalExpenseForMonth(mk);
                barEntries.add(new BarEntry(idx++, (float) exp));
                try {
                    Date d = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(mk);
                    barLabels.add(new SimpleDateFormat("MMM yy", Locale.getDefault()).format(d));
                } catch (Exception e) { barLabels.add(mk); }
            }

            // Category breakdown + top expenses
            Map<String, Double> catMap = new LinkedHashMap<>();
            double totalExp = 0;
            for (Transaction t : allTx) {
                if ("Expense".equalsIgnoreCase(t.getType())) {
                    String cat = t.getDisplayLabel();
                    catMap.put(cat, catMap.getOrDefault(cat, 0.0) + t.getAmount());
                    totalExp += t.getAmount();
                }
            }
            // Sort categories by amount descending
            List<Map.Entry<String, Double>> sorted = new ArrayList<>(catMap.entrySet());
            sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

            // Top 5 individual expenses
            List<Transaction> expenses = new ArrayList<>();
            for (Transaction t : allTx) if ("Expense".equalsIgnoreCase(t.getType())) expenses.add(t);
            expenses.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
            List<Transaction> top5 = expenses.size() > 5 ? expenses.subList(0, 5) : expenses;

            double finalTotalExp = totalExp;
            final String topCat = sorted.isEmpty() ? "—" : sorted.get(0).getKey();
            double avgExp = months.isEmpty() ? 0 : totalExp / months.size();

            runOnUiThread(() -> {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
                tvSummaryMonth.setText("Last " + months.size() + " month(s) tracked");
                tvAvgExpense.setText("₹" + fmt.format(avgExp));
                tvTopCategory.setText(topCat);

                // Bar chart
                setupBarChart(barEntries, barLabels);

                // Category breakdown rows
                categoryBreakdownContainer.removeAllViews();
                for (Map.Entry<String, Double> e : sorted) {
                    double pct = finalTotalExp > 0 ? (e.getValue() / finalTotalExp) * 100 : 0;
                    addBreakdownRow(categoryBreakdownContainer, e.getKey(),
                            e.getValue(), pct, fmt);
                }

                // Top expenses rows
                topExpensesContainer.removeAllViews();
                for (Transaction t : top5) {
                    addTopExpenseRow(topExpensesContainer, t, fmt);
                }
            });
        });
    }

    private void setupBarChart(List<BarEntry> entries, List<String> labels) {
        if (entries.isEmpty()) { barChart.setVisibility(android.view.View.GONE); return; }
        BarDataSet ds = new BarDataSet(entries, "Monthly Expenses");
        ds.setColor(android.graphics.Color.parseColor("#6366F1"));
        ds.setValueTextColor(android.graphics.Color.parseColor("#374151"));
        ds.setValueTextSize(10f);

        BarData bd = new BarData(ds);
        bd.setBarWidth(0.6f);

        barChart.setData(bd);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setTextColor(android.graphics.Color.parseColor("#6B7280"));
        barChart.getAxisLeft().setGridColor(android.graphics.Color.parseColor("#E5E7EB"));

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        x.setLabelCount(labels.size());

        barChart.invalidate();
    }

    @SuppressLint("SetTextI18n")
    private void addBreakdownRow(LinearLayout container, String cat, double amount, double pct, NumberFormat fmt) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        row.setLayoutParams(lp);

        // Label row
        LinearLayout lr = new LinearLayout(this);
        lr.setOrientation(LinearLayout.HORIZONTAL);
        lr.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvCat = new TextView(this);
        tvCat.setText(cat);
        tvCat.setTextSize(13f);
        tvCat.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvCat.setTextColor(android.graphics.Color.parseColor("#1F2937"));
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvAmt = new TextView(this);
        tvAmt.setText("₹" + fmt.format(amount) + " (" + String.format("%.0f", pct) + "%)");
        tvAmt.setTextSize(12f);
        tvAmt.setTextColor(android.graphics.Color.parseColor("#DC2626"));
        lr.addView(tvCat); lr.addView(tvAmt);
        row.addView(lr);

        // Progress
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress((int) pct);
        pb.setProgressTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#6366F1")));
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 16);
        pbLp.setMargins(0, 6, 0, 0);
        pb.setLayoutParams(pbLp);
        row.addView(pb);
        container.addView(row);
    }

    @SuppressLint("SetTextI18n")
    private void addTopExpenseRow(LinearLayout container, Transaction t, NumberFormat fmt) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        row.setLayoutParams(lp);

        TextView tvCat = new TextView(this);
        tvCat.setText(t.getDisplayLabel());
        tvCat.setTextSize(13f);
        tvCat.setTextColor(android.graphics.Color.parseColor("#1F2937"));
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvDate = new TextView(this);
        tvDate.setText(t.getDate());
        tvDate.setTextSize(11f);
        tvDate.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        tvDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDate.setPadding(0, 0, 24, 0);

        TextView tvAmt = new TextView(this);
        tvAmt.setText("₹" + fmt.format(t.getAmount()));
        tvAmt.setTextSize(13f);
        tvAmt.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvAmt.setTextColor(android.graphics.Color.parseColor("#DC2626"));

        row.addView(tvCat); row.addView(tvDate); row.addView(tvAmt);
        container.addView(row);

        // Divider
        android.view.View div = new android.view.View(this);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dLp.setMargins(0, 0, 0, 20);
        div.setLayoutParams(dLp);
        div.setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"));
        container.addView(div);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
