package com.svu.firstaidhero.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.KitItem;
import com.svu.firstaidhero.utils.ImageResources;
import com.svu.firstaidhero.utils.KitStatus;

import java.util.ArrayList;
import java.util.List;

/** Kit inventory grouped by category, with a header row per category. */
public class KitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    /** Called when the user taps + or − on an item. */
    public interface OnQuantityChangeListener {
        void onQuantityChange(KitItem item, int newQuantity);
    }

    // Each row is either a category key (String) or a KitItem
    private final List<Object> rows = new ArrayList<>();
    private OnQuantityChangeListener listener;

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.listener = listener;
    }

    /** Groups the items by category, in the order bleeding, burns, tools. */
    public void submit(List<KitItem> items) {
        rows.clear();
        for (String category : new String[]{
                KitItem.CATEGORY_BLEEDING, KitItem.CATEGORY_BURNS, KitItem.CATEGORY_TOOLS}) {
            List<KitItem> group = new ArrayList<>();
            for (KitItem item : items) {
                if (category.equals(item.getCategory())) group.add(item);
            }
            if (group.isEmpty()) continue;
            rows.add(category);
            rows.addAll(group);
        }
        notifyDataSetChanged();
    }

    public static int categoryTitle(String category) {
        if (KitItem.CATEGORY_BLEEDING.equals(category)) return R.string.kit_cat_bleeding;
        if (KitItem.CATEGORY_BURNS.equals(category)) return R.string.kit_cat_burns;
        return R.string.kit_cat_tools;
    }

    private static int categoryIcon(String category) {
        if (KitItem.CATEGORY_BLEEDING.equals(category)) return R.drawable.ic_emergency;
        if (KitItem.CATEGORY_BURNS.equals(category)) return R.drawable.ic_fire;
        return R.drawable.ic_shield;
    }

    private static int categoryColor(String category) {
        if (KitItem.CATEGORY_BLEEDING.equals(category)) return R.color.nabd_emergency;
        if (KitItem.CATEGORY_BURNS.equals(category)) return R.color.nabd_amber;
        return R.color.nabd_primary;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_kit_header, parent, false));
        }
        return new ItemHolder(inflater.inflate(R.layout.item_kit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind((String) row, sectionHasProblem(position));
        } else {
            ((ItemHolder) holder).bind((KitItem) row);
        }
    }

    private boolean sectionHasProblem(int headerPosition) {
        for (int i = headerPosition + 1; i < rows.size() && rows.get(i) instanceof KitItem; i++) {
            if (KitStatus.of((KitItem) rows.get(i)).isProblem()) return true;
        }
        return false;
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvStatus;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivKitSectionIcon);
            tvTitle = itemView.findViewById(R.id.tvKitSectionTitle);
            tvStatus = itemView.findViewById(R.id.tvKitSectionStatus);
        }

        void bind(String category, boolean hasProblem) {
            Context context = itemView.getContext();
            ivIcon.setImageResource(categoryIcon(category));
            ivIcon.setColorFilter(ContextCompat.getColor(context, categoryColor(category)));
            tvTitle.setText(categoryTitle(category));
            tvStatus.setText(hasProblem ? R.string.kit_section_missing : R.string.kit_section_ready);
            tvStatus.setBackgroundResource(hasProblem ? R.drawable.bg_pill_pink : R.drawable.bg_pill_green);
            tvStatus.setTextColor(ContextCompat.getColor(context,
                    hasProblem ? R.color.nabd_emergency : R.color.nabd_green));
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPhoto;
        private final ImageView ivIcon;
        private final TextView tvQtyBadge;
        private final TextView tvName;
        private final TextView tvStatus;
        private final TextView tvNameEn;
        private final TextView tvMeta;
        private final TextView tvRemaining;
        private final TextView tvExpiry;
        private final TextView tvQty;
        private final View btnMinus;
        private final View btnPlus;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivKitPhoto);
            ivIcon = itemView.findViewById(R.id.ivKitIcon);
            tvQtyBadge = itemView.findViewById(R.id.tvKitQtyBadge);
            tvName = itemView.findViewById(R.id.tvKitName);
            tvStatus = itemView.findViewById(R.id.tvKitStatus);
            tvNameEn = itemView.findViewById(R.id.tvKitNameEn);
            tvMeta = itemView.findViewById(R.id.tvKitMeta);
            tvRemaining = itemView.findViewById(R.id.tvKitRemaining);
            tvExpiry = itemView.findViewById(R.id.tvKitExpiry);
            tvQty = itemView.findViewById(R.id.tvKitQty);
            btnMinus = itemView.findViewById(R.id.btnKitMinus);
            btnPlus = itemView.findViewById(R.id.btnKitPlus);
        }

        void bind(KitItem item) {
            Context context = itemView.getContext();
            KitStatus status = KitStatus.of(item);

            int photo = ImageResources.find(context, item.getImageName());
            ivPhoto.setVisibility(photo != 0 ? View.VISIBLE : View.GONE);
            ivIcon.setVisibility(photo != 0 ? View.GONE : View.VISIBLE);
            if (photo != 0) {
                ivPhoto.setImageResource(photo);
            } else {
                ivIcon.setImageResource(categoryIcon(item.getCategory()));
                ivIcon.setColorFilter(ContextCompat.getColor(context, categoryColor(item.getCategory())));
            }

            int qty = item.getQuantity();
            tvQtyBadge.setText(context.getString(R.string.kit_qty_badge, qty));
            tvName.setText(item.getName());
            tvNameEn.setText(item.getNameEn());
            tvStatus.setText(status.label);
            tvStatus.setTextColor(ContextCompat.getColor(context, status.color));
            tvStatus.setBackgroundResource(status.pill);
            tvMeta.setText(context.getString(R.string.kit_available, qty, item.getUnit()));

            boolean low = qty < item.getRequiredQuantity();
            tvRemaining.setVisibility(low ? View.VISIBLE : View.GONE);
            if (low) {
                tvRemaining.setText(context.getString(
                        R.string.kit_remaining, qty, item.getRequiredQuantity()));
            }

            tvExpiry.setText(item.getExpiry() != null
                    ? context.getString(R.string.kit_expires, item.getExpiry())
                    : context.getString(R.string.kit_no_expiry));
            tvExpiry.setTextColor(ContextCompat.getColor(context,
                    status == KitStatus.EXPIRED ? R.color.nabd_emergency : R.color.nabd_on_surface_variant));

            tvQty.setText(String.valueOf(qty));
            btnMinus.setEnabled(qty > 0);
            btnMinus.setOnClickListener(v -> {
                if (listener != null && item.getQuantity() > 0) {
                    listener.onQuantityChange(item, item.getQuantity() - 1);
                }
            });
            btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onQuantityChange(item, item.getQuantity() + 1);
            });
        }
    }
}
