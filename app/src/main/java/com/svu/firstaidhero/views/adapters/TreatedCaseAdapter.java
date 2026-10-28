package com.svu.firstaidhero.views.adapters;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.TreatedCase;
import com.svu.firstaidhero.utils.CaseDates;
import com.svu.firstaidhero.utils.ImageLoader;
import com.svu.firstaidhero.utils.Triage;

import java.util.Objects;

public class TreatedCaseAdapter
        extends ListAdapter<TreatedCase, TreatedCaseAdapter.CaseViewHolder> {

    /** Actions on a case card. */
    public interface Listener {
        void onOpenReport(TreatedCase treatedCase);

        void onShare(TreatedCase treatedCase);

        void onEdit(TreatedCase treatedCase);
    }

    private Listener listener;

    public TreatedCaseAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<TreatedCase> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TreatedCase>() {
                @Override
                public boolean areItemsTheSame(@NonNull TreatedCase oldItem,
                                               @NonNull TreatedCase newItem) {
                    return oldItem.getLogId() == newItem.getLogId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull TreatedCase oldItem,
                                                  @NonNull TreatedCase newItem) {
                    return Objects.equals(oldItem.getDescription(), newItem.getDescription())
                            && Objects.equals(oldItem.getDateTreated(), newItem.getDateTreated())
                            && Objects.equals(oldItem.getProceduralNotes(),
                            newItem.getProceduralNotes())
                            && Objects.equals(oldItem.getPhotoUri(), newItem.getPhotoUri())
                            && Objects.equals(oldItem.getTriageLevel(), newItem.getTriageLevel());
                }
            };

    @NonNull
    @Override
    public CaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_treated_case, parent, false);
        return new CaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaseViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class CaseViewHolder extends RecyclerView.ViewHolder {
        private final View stripe;
        private final TextView tvTriage;
        private final TextView tvAgo;
        private final ImageView ivCritical;
        private final TextView tvDescription;
        private final TextView tvDate;
        private final TextView tvNumber;
        private final TextView tvNotes;
        private final View photoBox;
        private final ImageView ivPhoto;

        CaseViewHolder(@NonNull View itemView) {
            super(itemView);
            stripe = itemView.findViewById(R.id.viewCaseStripe);
            tvTriage = itemView.findViewById(R.id.tvCaseTriage);
            tvAgo = itemView.findViewById(R.id.tvCaseAgo);
            ivCritical = itemView.findViewById(R.id.ivCaseCritical);
            tvDescription = itemView.findViewById(R.id.tvCaseDescription);
            tvDate = itemView.findViewById(R.id.tvCaseDate);
            tvNumber = itemView.findViewById(R.id.tvCaseNumber);
            tvNotes = itemView.findViewById(R.id.tvCaseNotes);
            photoBox = itemView.findViewById(R.id.photoBox);
            ivPhoto = itemView.findViewById(R.id.ivCasePhoto);

            itemView.findViewById(R.id.btnCaseReport).setOnClickListener(v -> dispatch(0));
            itemView.findViewById(R.id.btnCaseShare).setOnClickListener(v -> dispatch(1));
            itemView.findViewById(R.id.btnCaseEdit).setOnClickListener(v -> dispatch(2));
            itemView.setOnClickListener(v -> dispatch(0));
        }

        private void dispatch(int action) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION || listener == null) return;
            TreatedCase c = getItem(position);
            if (action == 0) listener.onOpenReport(c);
            else if (action == 1) listener.onShare(c);
            else listener.onEdit(c);
        }

        void bind(TreatedCase c) {
            String level = c.getTriageLevel();
            int color = ContextCompat.getColor(itemView.getContext(), Triage.color(level));

            stripe.setBackgroundColor(color);
            tvTriage.setText(Triage.label(level));
            tvTriage.setTextColor(color);
            tvTriage.setBackgroundResource(Triage.pill(level));
            tvTriage.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.bg_dot_red, 0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(tvTriage,
                    ContextCompat.getColorStateList(itemView.getContext(), Triage.color(level)));
            ivCritical.setVisibility(Triage.CRITICAL.equals(level) ? View.VISIBLE : View.GONE);

            String ago = CaseDates.timeAgo(itemView.getContext(), c.getDateTreated());
            tvAgo.setText(ago);
            tvAgo.setVisibility(ago == null ? View.GONE : View.VISIBLE);

            tvDescription.setText(c.getDescription());
            tvDate.setText(c.getDateTreated());
            tvNumber.setText(itemView.getContext().getString(R.string.case_number, c.getLogId()));
            tvNotes.setText(c.getProceduralNotes());

            String photo = c.getPhotoUri();
            Bitmap bitmap = (photo == null || photo.isEmpty())
                    ? null
                    : ImageLoader.load(itemView.getContext(), Uri.parse(photo), 240);
            photoBox.setVisibility(bitmap == null ? View.GONE : View.VISIBLE);
            ivPhoto.setImageBitmap(bitmap);
        }
    }
}
