package com.svu.firstaidhero.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.svu.firstaidhero.R;
import com.svu.firstaidhero.database.entities.CustomContact;
import com.svu.firstaidhero.utils.Dialer;

import java.util.Objects;

public class ContactAdapter extends ListAdapter<CustomContact, ContactAdapter.ContactViewHolder> {

    /** Edit, delete, and send SMS actions on a contact card. */
    public interface Listener {
        void onEdit(CustomContact contact);

        void onDelete(CustomContact contact);

        void onSendSms(CustomContact contact);
    }

    private Listener listener;

    public ContactAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<CustomContact> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CustomContact>() {
                @Override
                public boolean areItemsTheSame(@NonNull CustomContact oldItem,
                                               @NonNull CustomContact newItem) {
                    return oldItem.getContactId() == newItem.getContactId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CustomContact oldItem,
                                                  @NonNull CustomContact newItem) {
                    return Objects.equals(oldItem.getName(), newItem.getName())
                            && Objects.equals(oldItem.getPhoneNumber(), newItem.getPhoneNumber())
                            && Objects.equals(oldItem.getRelationship(), newItem.getRelationship());
                }
            };

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitial;
        private final TextView tvName;
        private final TextView tvRelation;
        private final TextView tvPhone;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvContactInitial);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvRelation = itemView.findViewById(R.id.tvContactRelation);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);

            itemView.findViewById(R.id.btnCallContact).setOnClickListener(v -> {
                if (hasPosition()) Dialer.dial(v.getContext(), current().getPhoneNumber());
            });
            itemView.findViewById(R.id.btnMessageContact).setOnClickListener(v -> {
                if (listener != null && hasPosition()) {
                    listener.onSendSms(current());
                } else if (hasPosition()) {
                    Dialer.sendSms(v.getContext(), current().getPhoneNumber(),
                            v.getContext().getString(R.string.sos_text));
                }
            });
            itemView.findViewById(R.id.btnEditContact).setOnClickListener(v -> {
                if (listener != null && hasPosition()) listener.onEdit(current());
            });
            itemView.findViewById(R.id.btnContactMore).setOnClickListener(this::showMenu);
        }

        private boolean hasPosition() {
            return getAdapterPosition() != RecyclerView.NO_POSITION;
        }

        private CustomContact current() {
            return getItem(getAdapterPosition());
        }

        private void showMenu(View anchor) {
            if (!hasPosition() || listener == null) return;
            CustomContact contact = current();
            PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
            menu.getMenu().add(0, 1, 0, R.string.edit);
            menu.getMenu().add(0, 2, 1, R.string.delete);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) listener.onEdit(contact);
                else listener.onDelete(contact);
                return true;
            });
            menu.show();
        }

        void bind(CustomContact contact) {
            String name = contact.getName().trim();
            tvInitial.setText(name.isEmpty() ? "" : name.substring(0, name.offsetByCodePoints(0, 1)));
            tvName.setText(name);
            tvPhone.setText(contact.getPhoneNumber());
            tvRelation.setText(contact.getRelationship());
            tvRelation.setVisibility(contact.getRelationship().isEmpty() ? View.GONE : View.VISIBLE);
        }
    }
}
