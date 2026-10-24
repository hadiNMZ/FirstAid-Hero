package com.svu.firstaidhero.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Emergency;
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

        EmergencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSymptoms = itemView.findViewById(R.id.tvSymptoms);
            tvDangerBadge = itemView.findViewById(R.id.tvDangerBadge);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEmergencyClick(getItem(position));
                }
            });
        }

        void bind(Emergency emergency) {
            tvTitle.setText(TextSanitizer.cleanTitle(emergency.getTitle()));
            tvSymptoms.setText(TextSanitizer.cleanSymptoms(emergency.getSymptoms()));
            tvDangerBadge.setText(
                    TextSanitizer.getArabicDangerLevel(emergency.getDangerLevel()));

            if ("High".equalsIgnoreCase(emergency.getDangerLevel())) {
                tvDangerBadge.setBackgroundResource(R.drawable.bg_badge_high);
            } else {
                tvDangerBadge.setBackgroundResource(R.drawable.bg_badge_minor);
            }
        }
    }
}