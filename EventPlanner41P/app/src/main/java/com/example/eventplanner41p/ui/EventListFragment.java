package com.example.eventplanner41p.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventplanner41p.R;
import com.example.eventplanner41p.data.Event;
import com.example.eventplanner41p.databinding.FragmentEventListBinding;

public class EventListFragment extends Fragment implements EventAdapter.OnEventActionListener {

    private FragmentEventListBinding binding;
    private EventViewModel eventViewModel;
    private EventAdapter adapter;

    public EventListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new EventAdapter(this);
        binding.recyclerViewEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewEvents.setAdapter(adapter);

        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        eventViewModel.getUpcomingEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.setEventList(events);

            int count = events == null ? 0 : events.size();
            binding.tvCount.setText(count + " upcoming");

            if (events == null || events.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewEvents.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
                binding.recyclerViewEvents.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditClick(Event event) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", event.getId());
        bundle.putString("title", event.getTitle());
        bundle.putString("category", event.getCategory());
        bundle.putString("location", event.getLocation());
        bundle.putLong("dateTimeMillis", event.getDateTimeMillis());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_eventListFragment_to_addEditEventFragment, bundle);
    }

    @Override
    public void onDeleteClick(Event event) {
        eventViewModel.delete(event);
        Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}