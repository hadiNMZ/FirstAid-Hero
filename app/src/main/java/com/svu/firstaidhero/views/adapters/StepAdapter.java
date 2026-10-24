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
import com.svu.firstaidhero.database.entities.Step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StepAdapter extends ListAdapter<Step, StepAdapter.StepViewHolder> {

    public StepAdapter() {
        super(DIFF_CALLBACK);
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

    static class StepViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStepNumber;
        private final TextView tvInstruction;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
        }

        void bind(Step step) {
            tvStepNumber.setText(String.valueOf(step.getStepNumber()));
            tvInstruction.setText(step.getInstructionText());
        }
    }
}