package com.example.voyage.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.voyage.R;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.ui.more.EmergencyActivity;
import com.example.voyage.ui.trips.CreateTripActivity;
import com.example.voyage.ui.trips.TripDetailActivity;
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.TripViewModel;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TripViewModel tripViewModel;
    private SessionManager session;
    private Trip currentUpcomingTrip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        tripViewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setupGreeting(view);
        setupQuickActions(view);
        setupUpcomingTrip(view);
        animateEntrance(view);
    }

    private void setupGreeting(View view) {
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvProfileInitial = view.findViewById(R.id.tvProfileInitial);
        CardView cvProfile = view.findViewById(R.id.cvProfile);

        String name = session.getUserName();
        tvUserName.setText(name);
        tvProfileInitial.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) tvGreeting.setText(getString(R.string.good_morning));
        else if (hour < 17) tvGreeting.setText(getString(R.string.good_afternoon));
        else tvGreeting.setText(getString(R.string.good_evening));

        cvProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(),
                    com.example.voyage.ui.more.ProfileActivity.class));
        });
    }

    private void setupQuickActions(View view) {
        LinearLayout btnPlanTrip = view.findViewById(R.id.btnPlanTrip);
        LinearLayout btnAskAi = view.findViewById(R.id.btnAskAi);
        LinearLayout btnNearby = view.findViewById(R.id.btnNearby);
        LinearLayout btnEmergency = view.findViewById(R.id.btnEmergency);

        btnPlanTrip.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            startActivity(new Intent(requireContext(), CreateTripActivity.class));
        });

        btnAskAi.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            Navigation.findNavController(view).navigate(R.id.nav_ai);
        });

        btnNearby.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            Navigation.findNavController(view).navigate(R.id.nav_map);
        });

        btnEmergency.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up));
            startActivity(new Intent(requireContext(), EmergencyActivity.class));
        });
    }

    private void setupUpcomingTrip(View view) {
        TextView tvTitle = view.findViewById(R.id.tvUpcomingTripTitle);
        TextView tvDays = view.findViewById(R.id.tvUpcomingTripDays);
        TextView tvBudget = view.findViewById(R.id.tvUpcomingBudget);
        TextView tvViewTrip = view.findViewById(R.id.tvViewTrip);
        LinearLayout budgetRow = view.findViewById(R.id.tripBudgetRow);
        CardView upcomingCard = view.findViewById(R.id.upcomingTripCard);

        TextView tvSeeAll = view.findViewById(R.id.tvSeeAllTrips);
        tvSeeAll.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.nav_trips));

        tripViewModel.getLatestUpcomingTrip().observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                currentUpcomingTrip = trip;
                tvTitle.setText(trip.title);
                tvDays.setText(trip.destination + " · " + trip.days + " days");
                tvBudget.setText("Budget: $" + (int) trip.budget);
                budgetRow.setVisibility(View.VISIBLE);
                tvViewTrip.setOnClickListener(v -> openTripDetail(trip));
                upcomingCard.setOnClickListener(v -> openTripDetail(trip));
            } else {
                tvTitle.setText("No upcoming trips");
                tvDays.setText("Tap Plan Trip to start!");
                budgetRow.setVisibility(View.GONE);
                upcomingCard.setOnClickListener(v ->
                        startActivity(new Intent(requireContext(), CreateTripActivity.class)));
            }
        });
    }

    private void openTripDetail(Trip trip) {
        Intent intent = new Intent(requireContext(), TripDetailActivity.class);
        intent.putExtra("trip_id", trip.tripId);
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void animateEntrance(View view) {
        view.findViewById(R.id.headerLayout)
                .startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
        view.postDelayed(() -> view.findViewById(R.id.btnPlanTrip)
                .getParent().equals(view.getRootView()), 200);
    }
}
