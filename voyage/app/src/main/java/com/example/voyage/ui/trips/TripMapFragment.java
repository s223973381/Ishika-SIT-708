package com.example.voyage.ui.trips;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.voyage.R;

public class TripMapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(requireContext().getColor(R.color.voyage_cream));

        TextView emoji = new TextView(requireContext());
        emoji.setText("🗺️");
        emoji.setTextSize(64);
        emoji.setGravity(Gravity.CENTER);

        TextView title = new TextView(requireContext());
        title.setText("Map Coming Soon");
        title.setTextSize(18);
        title.setTextColor(requireContext().getColor(R.color.voyage_text_primary));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 32, 0, 0);

        TextView sub = new TextView(requireContext());
        sub.setText("Itinerary locations will appear here");
        sub.setTextSize(13);
        sub.setTextColor(requireContext().getColor(R.color.voyage_text_secondary));
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 12, 0, 0);

        layout.addView(emoji);
        layout.addView(title);
        layout.addView(sub);

        return layout;
    }
}
