package com.example.voyage.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.voyage.R;
import com.example.voyage.ai.WeatherClient;
import com.example.voyage.database.entities.Trip;
import com.example.voyage.ui.island.SmartIsland;
import com.example.voyage.ui.map.NearbyPlace;
import com.example.voyage.ui.map.OverpassClient;
import com.example.voyage.ui.more.EmergencyActivity;
import com.example.voyage.ui.trips.CreateTripActivity;
import com.example.voyage.ui.trips.TripDetailActivity;
import com.example.voyage.util.AppContext;
import com.example.voyage.util.ContextManager;
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.TripViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TripViewModel tripViewModel;
    private SessionManager session;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermLauncher;

    // Weather card views
    private TextView tvLocation, tvWeatherIcon, tvWeatherDesc, tvWeatherTip;

    // Suggestion card 1 views
    private TextView tvSugg1Emoji, tvSugg1Title, tvSugg1Sub;
    private CardView suggestionCard1;

    // Suggestion card 2 views (weather alert)
    private TextView tvSugg2Emoji, tvSugg2Title, tvSugg2Sub;

    // Context banner chips
    private TextView chipConnectivity, chipBattery, chipStyle;
    private View contextBannerScroll;

    // Runtime state
    private int lastWeatherCode = 0;
    private double lastPreciseLat = 0, lastPreciseLng = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) fetchLocation();
                    else if (tvLocation != null) tvLocation.setText("Location unavailable");
                }
        );
    }

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Bind views
        tvLocation    = view.findViewById(R.id.tvLocation);
        tvWeatherIcon = view.findViewById(R.id.tvWeatherIcon);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvWeatherTip  = view.findViewById(R.id.tvWeatherTip);

        suggestionCard1 = view.findViewById(R.id.suggestionCard1);
        tvSugg1Emoji    = view.findViewById(R.id.tvSugg1Emoji);
        tvSugg1Title    = view.findViewById(R.id.tvSugg1Title);
        tvSugg1Sub      = view.findViewById(R.id.tvSugg1Sub);
        tvSugg2Emoji    = view.findViewById(R.id.tvSugg2Emoji);
        tvSugg2Title    = view.findViewById(R.id.tvSugg2Title);
        tvSugg2Sub      = view.findViewById(R.id.tvSugg2Sub);

        chipConnectivity  = view.findViewById(R.id.chipConnectivity);
        chipBattery       = view.findViewById(R.id.chipBattery);
        chipStyle         = view.findViewById(R.id.chipStyle);
        contextBannerScroll = view.findViewById(R.id.contextBannerScroll);

        // Initial state
        tvLocation.setText("Locating...");
        tvWeatherDesc.setText("Fetching weather...");
        tvWeatherTip.setText("");
        setCard1Initial();

        // Context banner: connectivity + battery + travel style
        updateContextBanner();

        // Card 1 taps → open Map tab
        suggestionCard1.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.nav_map));

        setupGreeting(view);
        setupQuickActions(view);
        setupUpcomingTrip(view);
        animateEntrance(view);
        checkAndFetchLocation();
    }

    // ── Context Banner ────────────────────────────────────────────

    private void updateContextBanner() {
        boolean online    = ContextManager.isOnline(requireContext());
        int battery       = ContextManager.getBatteryPercent(requireContext());
        String style      = session.getTravelStyle();
        String styleLabel = ContextManager.travelStyleChipLabel(style);

        boolean anyVisible = false;

        // 📡 Connectivity chip
        if (!online) {
            chipConnectivity.setVisibility(View.VISIBLE);
            chipConnectivity.setText("📴 Offline · AI uses Ollama");
            anyVisible = true;
            // Island: offline alert (shows once per session when fragment loads)
            if (isAdded()) {
                SmartIsland.show(requireActivity(), new SmartIsland.Config()
                        .icon("📴").title("Offline mode active")
                        .subtitle("AI will use Ollama on your device")
                        .autoDismiss(5000));
            }
        } else {
            chipConnectivity.setVisibility(View.GONE);
        }

        // 🔋 Battery chip (< 25%)
        if (battery > 0 && battery < 25) {
            chipBattery.setVisibility(View.VISIBLE);
            chipBattery.setText("🔋 " + battery + "% · Save maps offline");
            anyVisible = true;
        } else {
            chipBattery.setVisibility(View.GONE);
        }

        // 👤 Travel style chip
        if (styleLabel != null) {
            chipStyle.setVisibility(View.VISIBLE);
            chipStyle.setText(styleLabel);
            anyVisible = true;
        } else {
            chipStyle.setVisibility(View.GONE);
        }

        contextBannerScroll.setVisibility(anyVisible ? View.VISIBLE : View.GONE);
    }

    // ── Greeting ──────────────────────────────────────────────────

    private void setupGreeting(View view) {
        TextView tvGreeting    = view.findViewById(R.id.tvGreeting);
        TextView tvUserName    = view.findViewById(R.id.tvUserName);
        TextView tvProfileInit = view.findViewById(R.id.tvProfileInitial);
        CardView cvProfile     = view.findViewById(R.id.cvProfile);

        String name = session.getUserName();
        tvUserName.setText(name);
        tvProfileInit.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12)      tvGreeting.setText(getString(R.string.good_morning));
        else if (hour < 17) tvGreeting.setText(getString(R.string.good_afternoon));
        else                tvGreeting.setText(getString(R.string.good_evening));

        cvProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(),
                        com.example.voyage.ui.more.ProfileActivity.class)));
    }

    // ── Quick Actions ─────────────────────────────────────────────

    private void setupQuickActions(View view) {
        LinearLayout btnPlanTrip  = view.findViewById(R.id.btnPlanTrip);
        LinearLayout btnAskAi     = view.findViewById(R.id.btnAskAi);
        LinearLayout btnNearby    = view.findViewById(R.id.btnNearby);
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

    // ── Upcoming Trip ─────────────────────────────────────────────

    private void setupUpcomingTrip(View view) {
        TextView tvTitle    = view.findViewById(R.id.tvUpcomingTripTitle);
        TextView tvDays     = view.findViewById(R.id.tvUpcomingTripDays);
        TextView tvBudget   = view.findViewById(R.id.tvUpcomingBudget);
        TextView tvViewTrip = view.findViewById(R.id.tvViewTrip);
        LinearLayout budgetRow = view.findViewById(R.id.tripBudgetRow);
        CardView upcomingCard  = view.findViewById(R.id.upcomingTripCard);
        TextView tvSeeAll      = view.findViewById(R.id.tvSeeAllTrips);

        tvSeeAll.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.nav_trips));

        tripViewModel.getLatestUpcomingTrip().observe(getViewLifecycleOwner(), trip -> {
            if (trip != null) {
                tvTitle.setText(trip.title);
                tvDays.setText(trip.destination + " · " + trip.days + " days");
                tvBudget.setText("Budget: $" + (int) trip.budget);
                budgetRow.setVisibility(View.VISIBLE);
                tvViewTrip.setOnClickListener(v -> openTripDetail(trip));
                upcomingCard.setOnClickListener(v -> openTripDetail(trip));
                // Island: trip starting soon (≤ 3 days)
                int daysLeft = daysUntil(trip.startDate);
                if (daysLeft >= 0 && daysLeft <= 3 && isAdded()) {
                    String sub = daysLeft == 0 ? "Pack up — you leave today!"
                               : daysLeft == 1 ? "One sleep to go!"
                               : "Starts in " + daysLeft + " days";
                    SmartIsland.show(requireActivity(), new SmartIsland.Config()
                            .icon("✈️").title(trip.destination + " soon!")
                            .subtitle(sub)
                            .action("View Trip", () -> openTripDetail(trip))
                            .autoDismiss(8000));
                }
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
    }

    // ── Location ──────────────────────────────────────────────────

    private void checkAndFetchLocation() {
        boolean fine = ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (fine || coarse) fetchLocation();
        else locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        // Coarse cached fix → header label + weather
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (!isAdded()) return;
            if (location != null) {
                reverseGeocode(location.getLatitude(), location.getLongitude());
                fetchWeather(location.getLatitude(), location.getLongitude());
            } else {
                if (tvLocation != null) tvLocation.setText("Location unavailable");
                if (tvWeatherDesc != null) tvWeatherDesc.setText("Weather unavailable");
            }
        });

        // Precise GPS → time+weather+preference-aware Overpass suggestion
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), precise -> {
                    if (!isAdded()) return;
                    if (precise != null) {
                        lastPreciseLat = precise.getLatitude();
                        lastPreciseLng = precise.getLongitude();
                        AppContext.userLat = lastPreciseLat;
                        AppContext.userLng = lastPreciseLng;
                        fetchContextSuggestion();
                    } else {
                        requireActivity().runOnUiThread(this::setCard1Unavailable);
                    }
                });
    }

    // ── Weather ───────────────────────────────────────────────────

    private void fetchWeather(double lat, double lng) {
        WeatherClient.fetch(lat, lng, new WeatherClient.Callback() {
            @Override
            public void onSuccess(String emoji, String description, String tip, int weatherCode) {
                if (!isAdded()) return;
                lastWeatherCode = weatherCode;
                AppContext.weatherDescription = description;
                AppContext.weatherCode = weatherCode;
                requireActivity().runOnUiThread(() -> {
                    tvWeatherIcon.setText(emoji);
                    tvWeatherDesc.setText(description);
                    tvWeatherTip.setText(tip);
                    updateWeatherAlert(weatherCode);
                    // Island: rain warning
                    if (weatherCode >= 51) {
                        SmartIsland.show(requireActivity(), new SmartIsland.Config()
                                .icon("☔").title("Rain expected today")
                                .subtitle("You may want to adjust outdoor plans")
                                .autoDismiss(7000));
                    }
                    // Re-run suggestion now that we have weather context
                    if (lastPreciseLat != 0) fetchContextSuggestion();
                });
            }

            @Override
            public void onError() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    tvWeatherDesc.setText("Weather unavailable");
                    tvWeatherTip.setText("Check your connection");
                    tvSugg2Emoji.setText("🌦️");
                    tvSugg2Title.setText("Weather unavailable");
                    tvSugg2Sub.setText("Check your internet connection");
                });
            }
        });
    }

    private void updateWeatherAlert(int code) {
        tvSugg2Emoji.setText(WeatherClient.alertEmoji(code));
        tvSugg2Title.setText(WeatherClient.alertTitle(code));
        tvSugg2Sub.setText(WeatherClient.alertSubtitle(code));
    }

    // ── Context-Aware Suggestion (card 1) ────────────────────────

    /** Set initial card state based on time + style (before GPS/weather arrive). */
    private void setCard1Initial() {
        int hour = ContextManager.getHour();
        String style = session != null ? session.getTravelStyle() : "";
        tvSugg1Emoji.setText(ContextManager.suggestionEmoji(hour, 0, style));
        tvSugg1Title.setText("Looking nearby...");
        tvSugg1Sub.setText(ContextManager.suggestionTitle(hour, 0, style) + "...");
    }

    private void fetchContextSuggestion() {
        if (lastPreciseLat == 0 && lastPreciseLng == 0) return;
        int hour = ContextManager.getHour();
        String style  = session.getTravelStyle();
        String filter = ContextManager.pickSuggestionFilter(hour, lastWeatherCode, style);
        String emoji  = ContextManager.suggestionEmoji(hour, lastWeatherCode, style);
        String label  = ContextManager.suggestionTitle(hour, lastWeatherCode, style);

        // Immediate placeholder with contextual emoji + label
        tvSugg1Emoji.setText(emoji);
        tvSugg1Title.setText("Looking nearby...");
        tvSugg1Sub.setText(label + "...");

        OverpassClient.fetchNearby(lastPreciseLat, lastPreciseLng, filter,
                new OverpassClient.Callback() {
                    @Override
                    public void onResult(List<NearbyPlace> places) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (places.isEmpty()) { setCard1Unavailable(); return; }
                            NearbyPlace nearest = places.get(0);
                            tvSugg1Emoji.setText(nearest.emoji);
                            tvSugg1Title.setText(nearest.name);
                            tvSugg1Sub.setText(label + " · " + nearest.getFormattedDistance() + " away");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> setCard1Unavailable());
                    }
                });
    }

    private void setCard1Unavailable() {
        int hour = ContextManager.getHour();
        String style = session.getTravelStyle();
        tvSugg1Emoji.setText(ContextManager.suggestionEmoji(hour, lastWeatherCode, style));
        tvSugg1Title.setText(ContextManager.suggestionTitle(hour, lastWeatherCode, style));
        tvSugg1Sub.setText("Tap to explore on the map");
    }

    // ── Reverse geocode ───────────────────────────────────────────

    private int daysUntil(String startDate) {
        if (startDate == null || startDate.isEmpty()) return -1;
        try {
            String[] p = startDate.split("-");
            Calendar trip = Calendar.getInstance();
            trip.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]), 0, 0, 0);
            trip.set(Calendar.MILLISECOND, 0);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long diff = trip.getTimeInMillis() - today.getTimeInMillis();
            return (int) (diff / (1000L * 60 * 60 * 24));
        } catch (Exception e) { return -1; }
    }

    private void reverseGeocode(double lat, double lng) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String result = "Location unavailable";
            try {
                if (Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        String city = addr.getLocality();
                        if (city == null) city = addr.getSubAdminArea();
                        if (city == null) city = addr.getAdminArea();
                        String country = addr.getCountryCode();
                        if (city != null && country != null) result = city + ", " + country;
                        else if (city != null)               result = city;
                    }
                }
            } catch (IOException | IllegalArgumentException ignored) {}
            // Share with AppContext so AI chat can use it
            AppContext.currentCity = result.equals("Location unavailable") ? "" : result;
            String finalResult = result;
            if (isAdded() && tvLocation != null) {
                requireActivity().runOnUiThread(() -> tvLocation.setText(finalResult));
            }
        });
    }
}
