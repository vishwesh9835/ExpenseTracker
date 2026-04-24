package com.example.expense_tracker.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense_tracker.R;
import com.example.expense_tracker.database.AppDatabase;
import com.example.expense_tracker.model.Transaction;

import java.util.concurrent.Executors;

public class AddTransactionActivity extends AppCompatActivity {

    EditText etAmount, etNote;
    AutoCompleteTextView actvCategory;
    com.google.android.material.button.MaterialButtonToggleGroup toggleGroupType;
    com.google.android.material.button.MaterialButton btnDate, btnSave;
    com.google.android.material.textfield.TextInputLayout tilCategory;
    com.google.android.material.switchmaterial.SwitchMaterial switchRecurring;

    String selectedDate = "";
    String[] categories = {"Food", "Travel", "Bills", "Shopping", "Health",
                           "Education", "Entertainment", "Other"};

    int editId = -1;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etAmount       = findViewById(R.id.etAmount);
        etNote         = findViewById(R.id.etNote);
        actvCategory   = findViewById(R.id.actvCategory);
        toggleGroupType = findViewById(R.id.toggleGroupType);
        btnDate        = findViewById(R.id.btnDate);
        btnSave        = findViewById(R.id.btnSave);
        tilCategory    = findViewById(R.id.tilCategory);
        switchRecurring = findViewById(R.id.switchRecurring);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(catAdapter);
        actvCategory.setText(categories[0], false);

        btnDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTransaction());

        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked)
                tilCategory.setVisibility(checkedId == R.id.btnIncome ? View.GONE : View.VISIBLE);
        });

        // Pre-fill for edit
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("id")) {
            isEditMode = true;
            editId = extras.getInt("id");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Edit Transaction");

            String type     = extras.getString("type", "Expense");
            double amount   = extras.getDouble("amount", 0);
            String category = extras.getString("category", "");
            String date     = extras.getString("date", "");
            String note     = extras.getString("note", "");
            boolean recurring = extras.getBoolean("isRecurring", false);

            // Show as integer if whole number (e.g. 100 instead of 100.0)
            etAmount.setText(amount == Math.floor(amount) && !Double.isInfinite(amount)
                    ? String.valueOf((long) amount)
                    : String.valueOf(amount));
            etNote.setText(note);
            switchRecurring.setChecked(recurring);

            if ("Income".equalsIgnoreCase(type)) {
                toggleGroupType.check(R.id.btnIncome);
                tilCategory.setVisibility(View.GONE);
            } else {
                toggleGroupType.check(R.id.btnExpense);
                actvCategory.setText(category, false);
            }

            if (!date.isEmpty()) { selectedDate = date; btnDate.setText(date); }
            btnSave.setText("Update Transaction");
        } else {
            // Default date to today
            String today = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                    .format(new java.util.Date());
            selectedDate = today;
            btnDate.setText(today);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (v, y, m, d) -> {
            selectedDate = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", d, m + 1, y);
            btnDate.setText(selectedDate);
        }, cal.get(java.util.Calendar.YEAR),
           cal.get(java.util.Calendar.MONTH),
           cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String note      = etNote.getText().toString().trim();
        String category  = actvCategory.getText().toString().trim();
        boolean recurring = switchRecurring.isChecked();

        int selId = toggleGroupType.getCheckedButtonId();
        com.google.android.material.button.MaterialButton selBtn = findViewById(selId);
        String type = (selBtn != null) ? selBtn.getText().toString() : "Expense";
        if ("Income".equalsIgnoreCase(type)) category = "";

        // Validation
        if (amountStr.isEmpty()) { etAmount.setError("Enter amount"); etAmount.requestFocus(); return; }
        if (selectedDate.isEmpty()) { Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show(); return; }

        double amount;
        try { amount = Double.parseDouble(amountStr); }
        catch (NumberFormatException e) { etAmount.setError("Invalid number"); return; }
        if (amount <= 0) { etAmount.setError("Must be > 0"); return; }

        AppDatabase db = AppDatabase.getInstance(this);

        if (isEditMode) {
            Transaction updated = new Transaction(type, amount, category, selectedDate, note, recurring);
            updated.setId(editId);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.transactionDao().update(updated);
                runOnUiThread(() -> { Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show(); finish(); });
            });
        } else {
            Transaction t = new Transaction(type, amount, category, selectedDate, note, recurring);
            Executors.newSingleThreadExecutor().execute(() -> {
                db.transactionDao().insert(t);
                runOnUiThread(() -> { Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show(); finish(); });
            });
        }
    }
}
