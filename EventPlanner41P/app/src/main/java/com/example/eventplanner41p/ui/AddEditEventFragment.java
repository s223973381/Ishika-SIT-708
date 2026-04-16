package com.example.eventplanner41p.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.eventplanner41p.R;
import com.example.eventplanner41p.data.Event;
import com.example.eventplanner41p.databinding.FragmentAddEditEventBinding;
import com.example.eventplanner41p.util.DateTimeHelper;

import java.util.Calendar;

public class AddEditEventFragment extends Fragment {

    private FragmentAddEditEventBinding binding;
    private EventViewModel eventViewModel;

    private long selectedDateTimeMillis = -1;
    private boolean isEditMode = false;
    private int eventId = -1;

    public AddEditEventFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        setupSpinner();
        checkIfEditMode();
        setupClicks();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_item,
                getResources().getStringArray(R.array.event_categories)
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void checkIfEditMode() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("id")) {
            isEditMode = true;
            eventId = args.getInt("id");
            binding.etTitle.setText(args.getString("title", ""));
            binding.etLocation.setText(args.getString("location", ""));
            selectedDateTimeMillis = args.getLong("dateTimeMillis", -1);

            String category = args.getString("category", "Work");
            ArrayAdapter<CharSequence> adapter =
                    (ArrayAdapter<CharSequence>) binding.spinnerCategory.getAdapter();
            int position = adapter.getPosition(category);
            binding.spinnerCategory.setSelection(Math.max(position, 0));

            if (selectedDateTimeMillis != -1) {
                binding.tvSelectedDateTime.setText(
                        DateTimeHelper.formatDateTime(selectedDateTimeMillis)
                );
            }

            binding.btnSave.setText("Update Event");
            binding.tvScreenTitle.setText("Edit Event");
        }
    }

    private void setupClicks() {
        binding.btnPickDate.setOnClickListener(v -> openDatePicker());
        binding.btnPickTime.setOnClickListener(v -> openTimePicker());
        binding.btnSave.setOnClickListener(v -> saveEvent());

        binding.chipWork.setOnClickListener(v -> setCategorySelection("Work"));
        binding.chipPersonal.setOnClickListener(v -> setCategorySelection("Personal"));
        binding.chipHealth.setOnClickListener(v -> setCategorySelection("Health"));
        binding.chipSocial.setOnClickListener(v -> setCategorySelection("Social"));
    }

    private void setCategorySelection(String category) {
        ArrayAdapter<CharSequence> adapter =
                (ArrayAdapter<CharSequence>) binding.spinnerCategory.getAdapter();
        int position = adapter.getPosition(category);
        if (position >= 0) {
            binding.spinnerCategory.setSelection(position);
        }
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();

        if (selectedDateTimeMillis != -1) {
            calendar.setTimeInMillis(selectedDateTimeMillis);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar tempCalendar = Calendar.getInstance();

                    if (selectedDateTimeMillis != -1) {
                        tempCalendar.setTimeInMillis(selectedDateTimeMillis);
                    }

                    tempCalendar.set(Calendar.YEAR, selectedYear);
                    tempCalendar.set(Calendar.MONTH, selectedMonth);
                    tempCalendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    selectedDateTimeMillis = tempCalendar.getTimeInMillis();
                    updateSelectedDateTimeText();
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();

        if (selectedDateTimeMillis != -1) {
            calendar.setTimeInMillis(selectedDateTimeMillis);
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    Calendar tempCalendar = Calendar.getInstance();

                    if (selectedDateTimeMillis != -1) {
                        tempCalendar.setTimeInMillis(selectedDateTimeMillis);
                    }

                    tempCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    tempCalendar.set(Calendar.MINUTE, selectedMinute);
                    tempCalendar.set(Calendar.SECOND, 0);
                    tempCalendar.set(Calendar.MILLISECOND, 0);

                    selectedDateTimeMillis = tempCalendar.getTimeInMillis();
                    updateSelectedDateTimeText();
                },
                hour, minute, false
        );

        timePickerDialog.show();
    }

    private void updateSelectedDateTimeText() {
        if (selectedDateTimeMillis != -1) {
            binding.tvSelectedDateTime.setText(DateTimeHelper.formatDateTime(selectedDateTimeMillis));
        }
    }

    private void saveEvent() {
        String title = binding.etTitle.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        if (category.equals("Select category")) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateTimeMillis == -1) {
            Toast.makeText(requireContext(), "Date and time are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEditMode && selectedDateTimeMillis < System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Please choose a future date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            Event updatedEvent = new Event(title, category, location, selectedDateTimeMillis);
            updatedEvent.setId(eventId);
            eventViewModel.update(updatedEvent);
            Toast.makeText(requireContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Event newEvent = new Event(title, category, location, selectedDateTimeMillis);
            eventViewModel.insert(newEvent);
            Toast.makeText(requireContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}