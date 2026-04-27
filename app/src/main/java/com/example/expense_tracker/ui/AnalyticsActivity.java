package com.example.expense_tracker.ui;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.expense_tracker.R;
import com.example.expense_tracker.database.AppDatabase;
import com.example.expense_tracker.model.Transaction;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class AnalyticsActivity extends AppCompatActivity {

    BarChart barChart;
    PieChart pieChart;
    LinearLayout topExpensesContainer, categoryBreakdownContainer;
    TextView tvSummaryMonth, tvSummaryMonthCount, tvAvgExpense, tvTopCategory;
    View pieChartCard;

    // Chart color palette
    private static final int[] CHART_COLORS = {
            0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899,
            0xFFF43F5E, 0xFF06B6D4, 0xFFF97316,
            0xFF16A34A, 0xFFD97706
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        com.google.android.material.appbar.MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        pieChartCard = findViewById(R.id.pieChartCard);
        topExpensesContainer = findViewById(R.id.topExpensesContainer);
        categoryBreakdownContainer = findViewById(R.id.categoryBreakdownContainer);
        tvSummaryMonthCount = findViewById(R.id.tvSummaryMonthCount);
        tvSummaryMonth = findViewById(R.id.tvSummaryMonth);
        tvAvgExpense = findViewById(R.id.tvAvgExpense);
        tvTopCategory = findViewById(R.id.tvTopCategory);

        loadAnalytics();
    }

    private boolean isDarkMode() {
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    private int textColor() {
        return isDarkMode()
                ? Color.parseColor("#E5E7EB")
                : Color.parseColor("#1F2937");
    }

    private int secondaryTextColor() {
        return isDarkMode()
                ? Color.parseColor("#9CA3AF")
                : Color.parseColor("#6B7280");
    }

    private int gridColor() {
        return isDarkMode()
                ? Color.parseColor("#374151")
                : Color.parseColor("#E5E7EB");
    }

    private int dividerColor() {
        return isDarkMode()
                ? Color.parseColor("#1F2937")
                : Color.parseColor("#F3F4F6");
    }

    @SuppressLint("SetTextI18n")
    private void loadAnalytics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            List<String> months = db.transactionDao().getDistinctMonths();
            List<Transaction> allTx = db.transactionDao().getAllTransactions();

            // Last 6 months bar data
            List<String> last6 = months.size() > 6 ? new ArrayList<>(months.subList(0, 6)) : new ArrayList<>(months);
            Collections.reverse(last6);
            List<BarEntry> barEntries = new ArrayList<>();
            List<String> barLabels = new ArrayList<>();
            int idx = 0;
            for (String mk : last6) {
                double exp = db.transactionDao().getTotalExpenseForMonth(mk);
                barEntries.add(new BarEntry(idx++, (float) exp));
                try {
                    Date d = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(mk);
                    barLabels.add(new SimpleDateFormat("MMM yy", Locale.getDefault()).format(d));
                } catch (Exception e) {
                    barLabels.add(mk);
                }
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

            // Sort categories descending
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
            int monthCount = months.size();
            boolean hasPie = !sorted.isEmpty();

            // Pie chart entries
            List<PieEntry> pieEntries = new ArrayList<>();
            for (int i = 0; i < Math.min(sorted.size(), 8); i++) {
                Map.Entry<String, Double> e = sorted.get(i);
                pieEntries.add(new PieEntry((float) e.getValue().doubleValue(), e.getKey()));
            }

            runOnUiThread(() -> {
                NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));

                // Summary banner
                tvSummaryMonthCount.setText(String.valueOf(monthCount));
                tvSummaryMonth.setText(monthCount == 1 ? "Month" : "Months");
                tvAvgExpense.setText("₹" + fmt.format(avgExp));
                tvTopCategory.setText(topCat);

                // Bar chart
                setupBarChart(barEntries, barLabels);

                // Pie chart
                if (hasPie) {
                    pieChartCard.setVisibility(View.VISIBLE);
                    setupPieChart(pieEntries);
                }

                // Category breakdown rows
                categoryBreakdownContainer.removeAllViews();
                for (int i = 0; i < sorted.size(); i++) {
                    Map.Entry<String, Double> e = sorted.get(i);
                    double pct = finalTotalExp > 0 ? (e.getValue() / finalTotalExp) * 100 : 0;
                    int color = CHART_COLORS[i % CHART_COLORS.length];
                    addBreakdownRow(categoryBreakdownContainer, e.getKey(), e.getValue(), pct, fmt, color, i == sorted.size() - 1);
                }

                // Top expenses rows
                topExpensesContainer.removeAllViews();
                for (int i = 0; i < top5.size(); i++) {
                    addTopExpenseRow(topExpensesContainer, top5.get(i), fmt, i + 1, i == top5.size() - 1);
                }
            });
        });
    }

    private void setupBarChart(List<BarEntry> entries, List<String> labels) {
        if (entries.isEmpty()) {
            barChart.setVisibility(View.GONE);
            return;
        }

        BarDataSet ds = new BarDataSet(entries, "Monthly Expenses");
        ds.setColors(new int[]{
                0xFF6366F1, 0xFF7C3AED, 0xFF8B5CF6, 0xFF6366F1, 0xFF818CF8, 0xFF7C3AED
        });
        ds.setValueTextColor(textColor());
        ds.setValueTextSize(10f);
        ds.setDrawValues(true);

        BarData bd = new BarData(ds);
        bd.setBarWidth(0.55f);

        barChart.setData(bd);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setExtraBottomOffset(8f);
        barChart.setExtraTopOffset(8f);
        barChart.getAxisRight().setEnabled(false);

        barChart.getAxisLeft().setTextColor(secondaryTextColor());
        barChart.getAxisLeft().setGridColor(gridColor());
        barChart.getAxisLeft().setGridLineWidth(0.5f);
        barChart.getAxisLeft().setAxisLineColor(gridColor());
        barChart.getAxisLeft().setSpaceTop(20f);
        barChart.getAxisLeft().setDrawZeroLine(true);

        XAxis x = barChart.getXAxis();
        x.setValueFormatter(new IndexAxisValueFormatter(labels));
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setTextColor(secondaryTextColor());
        x.setLabelCount(labels.size());
        x.setAvoidFirstLastClipping(true);
        x.setAxisLineColor(gridColor());

        barChart.setTouchEnabled(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.animateY(800, Easing.EaseInOutQuad);
        barChart.invalidate();
    }

    private void setupPieChart(List<PieEntry> entries) {
        int[] colors = new int[Math.min(entries.size(), CHART_COLORS.length)];
        for (int i = 0; i < colors.length; i++) colors[i] = CHART_COLORS[i % CHART_COLORS.length];

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setColors(colors);
        ds.setSliceSpace(2f);
        ds.setSelectionShift(6f);
        ds.setValueTextSize(11f);
        ds.setValueTextColor(Color.WHITE);
        ds.setValueLinePart1OffsetPercentage(80f);
        ds.setValueLinePart1Length(0.3f);
        ds.setValueLinePart2Length(0.4f);
        ds.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        ds.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        ds.setValueLineColor(secondaryTextColor());

        PieData pd = new PieData(ds);
        pd.setValueFormatter(new PercentFormatter(pieChart));
        pd.setValueTextSize(11f);

        pieChart.setData(pd);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(44f);
        pieChart.setTransparentCircleRadius(48f);
        pieChart.setTransparentCircleColor(isDarkMode() ? Color.parseColor("#1F2937") : Color.parseColor("#F9FAFB"));
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setCenterText("Expenses");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(textColor());
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelColor(textColor());
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setDrawEntryLabels(false);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(secondaryTextColor());
        legend.setTextSize(11f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(8f);
        legend.setXEntrySpace(12f);
        legend.setYEntrySpace(4f);
        legend.setWordWrapEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        pieChart.animateY(900, Easing.EaseInOutQuad);
        pieChart.invalidate();
    }

    @SuppressLint("SetTextI18n")
    private void addBreakdownRow(LinearLayout container, String cat, double amount, double pct, NumberFormat fmt, int accentColor, boolean isLast) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, isLast ? 0 : dpToPx(20));
        row.setLayoutParams(lp);

        // Label + amount row
        LinearLayout lr = new LinearLayout(this);
        lr.setOrientation(LinearLayout.HORIZONTAL);
        lr.setGravity(android.view.Gravity.CENTER_VERTICAL);
        lr.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Color dot
        View dot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dpToPx(10), dpToPx(10));
        dotLp.setMarginEnd(dpToPx(10));
        dot.setLayoutParams(dotLp);
        dot.setBackgroundColor(accentColor);

        TextView tvCat = new TextView(this);
        tvCat.setText(cat);
        tvCat.setTextSize(13f);
        tvCat.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvCat.setTextColor(textColor());
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvAmt = new TextView(this);
        tvAmt.setText("₹" + fmt.format(amount));
        tvAmt.setTextSize(13f);
        tvAmt.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvAmt.setTextColor(0xFFDC2626);

        TextView tvPct = new TextView(this);
        tvPct.setText("  " + String.format("%.0f", pct) + "%");
        tvPct.setTextSize(12f);
        tvPct.setTextColor(secondaryTextColor());

        lr.addView(dot);
        lr.addView(tvCat);
        lr.addView(tvAmt);
        lr.addView(tvPct);
        row.addView(lr);

        // Progress bar
        ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        pb.setMax(100);
        pb.setProgress((int) pct);
        pb.setProgressDrawable(
                ContextCompat.getDrawable(this, R.drawable.progress_bar_rounded));
        pb.getProgressDrawable().setColorFilter(
                accentColor, android.graphics.PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(8));
        pbLp.setMargins(0, dpToPx(8), 0, 0);
        pb.setLayoutParams(pbLp);
        row.addView(pb);

        container.addView(row);
    }

    @SuppressLint("SetTextI18n")
    private void addTopExpenseRow(LinearLayout container, Transaction t, NumberFormat fmt, int rank, boolean isLast) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        row.setLayoutParams(lp);
        row.setPadding(0, dpToPx(12), 0, dpToPx(12));

        // Rank badge
        TextView tvRank = new TextView(this);
        tvRank.setText(String.valueOf(rank));
        tvRank.setTextSize(11f);
        tvRank.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvRank.setTextColor(rank == 1 ? 0xFFD97706 : secondaryTextColor());
        tvRank.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams rankLp = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        rankLp.setMarginEnd(dpToPx(12));
        tvRank.setLayoutParams(rankLp);
        tvRank.setBackground(createRankBackground(rank));

        // Category + date column
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvCat = new TextView(this);
        tvCat.setText(t.getDisplayLabel());
        tvCat.setTextSize(13f);
        tvCat.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvCat.setTextColor(textColor());

        TextView tvDate = new TextView(this);
        tvDate.setText(t.getDate());
        tvDate.setTextSize(11f);
        tvDate.setTextColor(secondaryTextColor());
        tvDate.setPadding(0, dpToPx(2), 0, 0);

        info.addView(tvCat);
        info.addView(tvDate);

        // Amount
        TextView tvAmt = new TextView(this);
        tvAmt.setText("₹" + fmt.format(t.getAmount()));
        tvAmt.setTextSize(14f);
        tvAmt.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvAmt.setTextColor(0xFFDC2626);

        row.addView(tvRank);
        row.addView(info);
        row.addView(tvAmt);
        container.addView(row);

        // Divider (not after last)
        if (!isLast) {
            View div = new View(this);
            LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            div.setLayoutParams(dLp);
            div.setBackgroundColor(dividerColor());
            container.addView(div);
        }
    }

    private android.graphics.drawable.Drawable createRankBackground(int rank) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        int bgColor;
        if (rank == 1) bgColor = 0x22D97706;
        else if (rank == 2) bgColor = 0x226366F1;
        else bgColor = isDarkMode() ? 0x22FFFFFF : 0x11000000;
        shape.setColor(bgColor);
        return shape;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
