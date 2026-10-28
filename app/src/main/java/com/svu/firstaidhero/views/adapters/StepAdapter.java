package com.svu.firstaidhero.views.adapters;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.Step;
import com.svu.firstaidhero.utils.ImageResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StepAdapter extends ListAdapter<Step, StepAdapter.StepViewHolder> {

    // Critical protocols get pink number tiles and an "immediate action" tag on step 1
    private boolean critical;
    private boolean largeText;

    public StepAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setCritical(boolean critical) {
        if (this.critical == critical) return;
        this.critical = critical;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setLargeText(boolean largeText) {
        this.largeText = largeText;
    }

    private static final DiffUtil.ItemCallback<Step> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Step>() {
                @Override
                public boolean areItemsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
                    return oldItem.getStepId() == newItem.getStepId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Step oldItem, @NonNull Step newItem) {
                    return oldItem.getStepNumber() == newItem.getStepNumber()
                            && Objects.equals(oldItem.getInstructionText(),
                            newItem.getInstructionText());
                }
            };

    /** Always shows the steps in order of step number, whatever order the database returns. */
    @Override
    public void submitList(List<Step> list) {
        if (list == null) {
            super.submitList(null);
            return;
        }
        List<Step> sorted = new ArrayList<>(list);
        Collections.sort(sorted,
                (a, b) -> Integer.compare(a.getStepNumber(), b.getStepNumber()));
        super.submitList(sorted);
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStepNumber;
        private final TextView tvStepLabel;
        private final TextView tvInstruction;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepLabel = itemView.findViewById(R.id.tvStepLabel);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
        }

        void bind(Step step) {
            int number = step.getStepNumber();
            int accent = ContextCompat.getColor(itemView.getContext(),
                    critical ? R.color.nabd_emergency : R.color.nabd_primary);

            tvStepNumber.setText(ImageResources.arabicDigits(number));
            tvStepNumber.setTextColor(accent);
            tvStepNumber.setBackgroundResource(
                    critical ? R.drawable.bg_tile_number_pink : R.drawable.bg_tile_number);

            String label = itemView.getContext().getString(R.string.step_label,
                    String.format(Locale.US, "%02d", number));
            if (critical && number == 1) {
                label += " • " + itemView.getContext().getString(R.string.immediate_action);
            }
            tvStepLabel.setText(label);
            tvStepLabel.setTextColor(accent);

            tvInstruction.setText(step.getInstructionText());
            tvInstruction.setTextSize(TypedValue.COMPLEX_UNIT_SP, largeText ? 20 : 16);
        }
    }
}
