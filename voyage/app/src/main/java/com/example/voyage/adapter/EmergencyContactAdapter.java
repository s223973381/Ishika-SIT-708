package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    public interface Listener {
        void onCall(EmergencyContact contact);
        void onDelete(EmergencyContact contact);
    }

    private List<EmergencyContact> contacts = new ArrayList<>();
    private final Listener listener;

    public EmergencyContactAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setContacts(List<EmergencyContact> list) {
        contacts = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        EmergencyContact c = contacts.get(pos);

        String initial = c.name != null && !c.name.isEmpty()
                ? String.valueOf(c.name.charAt(0)).toUpperCase() : "?";
        h.tvInitial.setText(initial);
        h.tvName.setText(c.name);
        h.tvRelation.setText(c.relation != null ? c.relation : "");
        h.tvPhone.setText(c.phone != null ? c.phone : "");
        h.tvPrimaryBadge.setVisibility(c.isPrimary ? View.VISIBLE : View.GONE);

        h.btnCall.setOnClickListener(v -> listener.onCall(c));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvRelation, tvPhone, tvPrimaryBadge, btnCall, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvInitial = v.findViewById(R.id.tvContactInitial);
            tvName = v.findViewById(R.id.tvContactName);
            tvRelation = v.findViewById(R.id.tvContactRelation);
            tvPhone = v.findViewById(R.id.tvContactPhone);
            tvPrimaryBadge = v.findViewById(R.id.tvPrimaryBadge);
            btnCall = v.findViewById(R.id.btnCallContact);
            btnDelete = v.findViewById(R.id.btnDeleteContact);
        }
    }
}
