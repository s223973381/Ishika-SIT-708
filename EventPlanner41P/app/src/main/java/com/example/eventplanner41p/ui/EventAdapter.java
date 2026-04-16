package com.example.eventplanner41p.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner41p.R;
import com.example.eventplanner41p.data.Event;
import com.example.eventplanner41p.databinding.ItemEventBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventActionListener {
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    private final List<Event> eventList = new ArrayList<>();
    private final OnEventActionListener listener;

    public EventAdapter(OnEventActionListener listener) {
        this.listener = listener;
    }

    public void setEventList(List<Event> events) {
        eventList.clear();
        if (events != null) {
            eventList.addAll(events);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(eventList.get(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private final ItemEventBinding binding;
        private boolean actionsVisible = false;

        public EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event) {
            binding.tvTitle.setText(event.getTitle());
            binding.tvCategory.setText(event.getCategory());
            binding.tvLocation.setText(event.getLocation());

            binding.tvTime.setText(formatTime(event.getDateTimeMillis()));
            binding.tvDate.setText(formatDate(event.getDateTimeMillis()));

            applyCategoryStyle(event.getCategory());

            binding.layoutActions.setVisibility(actionsVisible ? View.VISIBLE : View.GONE);

            binding.contentRoot.setOnClickListener(v -> {
                actionsVisible = !actionsVisible;
                binding.layoutActions.setVisibility(actionsVisible ? View.VISIBLE : View.GONE);
            });

            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(event));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(event));
        }

        private void applyCategoryStyle(String category) {
            int accentColor;
            int chipDrawable;

            switch (category.toLowerCase()) {
                case "personal":
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.personal_color);
                    chipDrawable = R.drawable.bg_chip_personal;
                    break;
                case "health":
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.health_color);
                    chipDrawable = R.drawable.bg_chip_health;
                    break;
                case "social":
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.social_color);
                    chipDrawable = R.drawable.bg_chip_social;
                    break;
                default:
                    accentColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.work_color);
                    chipDrawable = R.drawable.bg_chip_work;
                    break;
            }

            binding.tvCategory.setBackgroundResource(chipDrawable);
            binding.viewAccent.setBackgroundColor(accentColor);
        }

        private String formatTime(long millis) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(millis));
        }

        private String formatDate(long millis) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            return sdf.format(new Date(millis));
        }
    }
}