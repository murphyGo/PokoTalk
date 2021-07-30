package com.murphy.pokotalk.activity.event;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.murphy.pokotalk.R;

// Selected location fragment
public class EventLocationSelectionFragment extends Fragment {
    private LocationSearchResult location;
    private View view;
    private TextView titleView;
    private TextView categoryView;
    private TextView addressView;

    public LocationSearchResult getLocation() {
        return location;
    }

    public void setLocation(LocationSearchResult location) {
        this.location = location;

        if (view != null) {
            displayLocationData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.event_location_information, container, false);

        // Find views
        titleView = view.findViewById(R.id.locationInfoTitle);
        categoryView = view.findViewById(R.id.locationInfoCategory);
        addressView = view.findViewById(R.id.locationInfoAddress);

        if (location != null) {
            displayLocationData();
        }

        return view;
    }

    private void displayLocationData() {
        // Get contents
        String title = location.getTitle();
        String category = location.getCategory();
        String address = location.getAddress();

        // View contents
        titleView.setText(title);
        categoryView.setText(category);
        addressView.setText(address);
    }
}
