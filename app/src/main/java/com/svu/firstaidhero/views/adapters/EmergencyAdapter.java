package com.svu.firstaidhero.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;
import com.svu.firstaidhero.utils.EmergencyCategory;
import com.svu.firstaidhero.utils.TextSanitizer;

import java.util.Objects;

public class EmergencyAdapter
        extends ListAdapter<Emergency, EmergencyAdapter.EmergencyViewHolder> {

    /** Called when the user taps an emergency card. */
    public interface OnEmergencyClickListener {
        void onEmergencyClick(Emergency emergency);
    }

    private OnEmergencyClickListener listener;

    public EmergencyAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnEmergencyClickListener(OnEmergencyClickListener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Emergency> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Emergency>() {
                @Override
                public boolean areItemsTheSame(@NonNull Emergency oldItem,
                                               @NonNull Emergency newItem) {
                    return oldItem.getCaseId() == newItem.getCaseId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Emergency oldItem,
                                                  @NonNull Emergency newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getSymptoms(), newItem.getSymptoms())
                            && Objects.equals(oldItem.getDangerLevel(), newItem.getDangerLevel())
                            && oldItem.isCommon() == newItem.isCommon();
                }
            };

    @NonNull
    @Override
    public EmergencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency, parent, false);
        return new EmergencyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class EmergencyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvSymptoms;
        private final TextView tvDangerBadge;
        private final View viewStripe;
        private final View iconBox;
        private final ImageView ivIcon;
        private final TextView tvUrgentTag;
        private final ImageView ivCategory;
        private final TextView tvCategory;

        EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSymptoms = itemView.findViewById(R.id.tvSymptoms);
            tvDangerBadge = itemView.findViewById(R.id.tvDangerBadge);
            viewStripe = itemView.findViewById(R.id.viewStripe);
            iconBox = itemView.findViewById(R.id.iconBox);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvUrgentTag = itemView.findViewById(R.id.tvUrgentTag);
            ivCategory = itemView.findViewById(R.id.ivCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEmergencyClick(getItem(position));
                }
            });
        }

        void bind(Emergency emergency) {
            String title = TextSanitizer.cleanTitle(emergency.getTitle());
            String symptoms = TextSanitizer.cleanSymptoms(emergency.getSymptoms());
            tvTitle.setText(title);
            // Many entries repeat the question as their only "symptom"; don't show it twice
            boolean repeatsTitle = symptoms.replace("• ", "").trim().equals(title.trim());
            tvSymptoms.setText(symptoms);
            tvSymptoms.setVisibility(symptoms.isEmpty() || repeatsTitle ? View.GONE : View.VISIBLE);
            tvDangerBadge.setText(
                    TextSanitizer.getArabicDangerLevel(emergency.getDangerLevel()));

            boolean high = "High".equalsIgnoreCase(emergency.getDangerLevel());
            int accent = ContextCompat.getColor(itemView.getContext(),
                    high ? R.color.nabd_emergency : R.color.nabd_info_blue);
            EmergencyCategory category = EmergencyCategory.of(emergency);

            tvDangerBadge.setBackgroundResource(
                    high ? R.drawable.bg_badge_high : R.drawable.bg_badge_minor);
            tvDangerBadge.setTextColor(accent);
            tvUrgentTag.setVisibility(high ? View.VISIBLE : View.GONE);
            viewStripe.setBackgroundColor(accent);
            iconBox.setBackgroundResource(
                    high ? R.drawable.bg_icon_box_pink : R.drawable.bg_icon_box_blue);
            ivIcon.setImageResource(category.icon);
            ivIcon.setColorFilter(accent);
            ivCategory.setImageResource(category.icon);
            tvCategory.setText(category.label);
        }
    }
}