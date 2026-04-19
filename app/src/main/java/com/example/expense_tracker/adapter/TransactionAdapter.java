package com.example.expense_tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expense_tracker.R;
import com.example.expense_tracker.model.Transaction;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> displayList = new ArrayList<>();
    private final OnItemClickListener listener;

    public TransactionAdapter(List<Transaction> list, OnItemClickListener listener) {
        this.displayList = new ArrayList<>(list);
        this.listener = listener;
    }

    public void updateList(List<Transaction> newList) {
        this.displayList = new ArrayList<>(newList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Transaction t = displayList.get(position);
        String label = t.getDisplayLabel();

        h.tvCategory.setText(label + (t.isRecurring() ? " 🔁" : ""));
        h.tvDate.setText(t.getDate());

        String note = t.getNote();
        if (note != null && !note.trim().isEmpty()) {
            h.tvNote.setVisibility(View.VISIBLE);
            h.tvNote.setText(note);
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        // Icon + colors
        int iconBg    = android.graphics.Color.parseColor("#E5E7EB");
        int iconColor = android.graphics.Color.parseColor("#374151");

        if ("Income".equalsIgnoreCase(t.getType())) {
            h.tvInitial.setText("💵");
            iconBg    = android.graphics.Color.parseColor("#DCFCE7");
            iconColor = android.graphics.Color.parseColor("#16A34A");
        } else {
            switch (label) {
                case "Food":
                    h.tvInitial.setText("🍔");
                    iconBg = android.graphics.Color.parseColor("#FFEDD5");
                    iconColor = android.graphics.Color.parseColor("#F97316"); break;
                case "Travel":
                    h.tvInitial.setText("✈️");
                    iconBg = android.graphics.Color.parseColor("#DBEAFE");
                    iconColor = android.graphics.Color.parseColor("#3B82F6"); break;
                case "Bills":
                    h.tvInitial.setText("🧾");
                    iconBg = android.graphics.Color.parseColor("#F3E8FF");
                    iconColor = android.graphics.Color.parseColor("#A855F7"); break;
                case "Shopping":
                    h.tvInitial.setText("🛍️");
                    iconBg = android.graphics.Color.parseColor("#FCE7F3");
                    iconColor = android.graphics.Color.parseColor("#EC4899"); break;
                case "Health":
                    h.tvInitial.setText("💊");
                    iconBg = android.graphics.Color.parseColor("#FEE2E2");
                    iconColor = android.graphics.Color.parseColor("#DC2626"); break;
                case "Education":
                    h.tvInitial.setText("📚");
                    iconBg = android.graphics.Color.parseColor("#FEF3C7");
                    iconColor = android.graphics.Color.parseColor("#D97706"); break;
                case "Entertainment":
                    h.tvInitial.setText("🎬");
                    iconBg = android.graphics.Color.parseColor("#CFFAFE");
                    iconColor = android.graphics.Color.parseColor("#0891B2"); break;
                default:
                    h.tvInitial.setText("💰");
                    iconBg = android.graphics.Color.parseColor("#F3F4F6");
                    iconColor = android.graphics.Color.parseColor("#6B7280");
            }
        }

        h.cvIcon.setCardBackgroundColor(iconBg);
        h.tvInitial.setTextColor(iconColor);

        NumberFormat fmt = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        if ("Income".equalsIgnoreCase(t.getType())) {
            int green = android.graphics.Color.parseColor("#16A34A");
            h.tvAmount.setTextColor(green);
            h.vAccentBar.setBackgroundColor(green);
            h.tvAmount.setText("+ ₹" + fmt.format(t.getAmount()));
        } else {
            int red = android.graphics.Color.parseColor("#DC2626");
            h.tvAmount.setTextColor(red);
            h.vAccentBar.setBackgroundColor(red);
            h.tvAmount.setText("- ₹" + fmt.format(t.getAmount()));
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(t); });
        h.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(t, h.getAdapterPosition());
            return true;
        });
    }

    @Override public int getItemCount() { return displayList.size(); }

    public interface OnItemClickListener {
        void onItemClick(Transaction t);
        void onItemLongClick(Transaction t, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvAmount, tvDate, tvInitial, tvNote;
        View vAccentBar;
        com.google.android.material.card.MaterialCardView cvIcon;

        public ViewHolder(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvAmount   = v.findViewById(R.id.tvAmount);
            tvDate     = v.findViewById(R.id.tvDate);
            tvInitial  = v.findViewById(R.id.tvInitial);
            tvNote     = v.findViewById(R.id.tvNote);
            vAccentBar = v.findViewById(R.id.vAccentBar);
            cvIcon     = v.findViewById(R.id.cvIcon);
        }
    }
}
