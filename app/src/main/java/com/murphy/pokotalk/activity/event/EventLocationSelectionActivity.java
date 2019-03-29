package com.murphy.pokotalk.activity.event;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.murphy.pokotalk.R;
import com.murphy.pokotalk.adapter.LocationListAdapter;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Symbol;
import com.naver.maps.map.overlay.InfoWindow;

import java.util.ArrayList;

public class EventLocationSelectionActivity extends FragmentActivity
        implements OnMapReadyCallback,
        NaverLocationSearchService.Listener {
    private MapFragment fragment;
    private FrameLayout mapLayout;
    private Button backButton;
    private Button selectButton;
    private EditText searchEditText;
    private ListView searchResultList;
    private FrameLayout information;
    private LinearLayout bottomLayout;
    private NaverMap naverMap;
    private LocationListAdapter adapter;
    private InfoWindow infoWindow;
    private View locationInfoView;
    private LocationSearchResult selectedLocation;
    private EditTextObserver observer;

    protected NaverLocationSearchService searchService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_location_selection_layout);

        // Initialize selected location
        selectedLocation = null;

        // Get intent data
        Intent intent = getIntent();
        if (intent.hasExtra("selected")) {
            LatLng latLng = intent.getParcelableExtra("latLng");
            selectedLocation = new LocationSearchResult();
            selectedLocation.setTitle(intent.getStringExtra("title"));
            selectedLocation.setCategory(intent.getStringExtra("category"));
            selectedLocation.setAddress(intent.getStringExtra("address"));
            selectedLocation.setLatLng(latLng);
        }

        // Find views in the layout
        mapLayout = findViewById(R.id.eventLocationMapLayout);
        selectButton = findViewById(R.id.eventLocationSelectionButton);
        backButton = findViewById(R.id.eventLocationBackspaceButton);
        searchEditText = findViewById(R.id.eventLocationSearchKeyword);
        searchResultList = findViewById(R.id.eventLocationSearchList);
        information = findViewById(R.id.eventLocationSearchInformation);
        bottomLayout = findViewById(R.id.eventLocationSearchBottomLayout);

        // Create map fragment and add.
        fragment = MapFragment.newInstance(createMapOption());
        fragment.setArguments(new Bundle());
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.eventLocationMapLayout, fragment).
                commit();

        // Get NaverMap instance asynchronously
        fragment.getMapAsync(this);

        // Get search service
        searchService = NaverLocationSearchService.getInstance();
        searchService.setResultListener(this);

        // Create text change observer for location search TextEdit
        observer = new EditTextObserver()
                .setEditText(searchEditText)
                .setTextChangedCallback(new Runnable() {
                    @Override
                    public void run() {
                        // Get user input keyword
                        String keyword = searchEditText.getText().toString();

                        // Request search
                        searchService.request(keyword);
                    }
                })
                .setTextEmptyCallback(new Runnable() {
                    @Override
                    public void run() {
                        // Search list should disappear when empty text
                        searchResultList.setVisibility(View.GONE);

                        // Show selection button if selected
                        if (selectedLocation != null) {
                            showBottomLayout();
                        }
                    }
                });

        // Search result should disappear when lost focus
        searchResultList.setOnFocusChangeListener(new ListView.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchResultList.setVisibility(View.GONE);
                }
            }
        });

        // Set back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set select button click listener
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    // Put location information
                    Intent intent = new Intent();
                    intent.putExtra("title", selectedLocation.getTitle());
                    intent.putExtra("address", selectedLocation.getAddress());
                    intent.putExtra("category", selectedLocation.getCategory());
                    intent.putExtra("latLng", selectedLocation.getLatLng());

                    // Set result
                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        });
    }

    private ViewCreationCallback<LocationSearchResult> searchListItemCallback =
            new ViewCreationCallback<LocationSearchResult>() {
                @Override
                public void run(View view, final LocationSearchResult item) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(naverMap == null) {
                                return;
                            }

                            // Remove previous info window
                            if (infoWindow != null) {
                                infoWindow.setMap(null);
                                ((ViewGroup)locationInfoView.getParent()).removeAllViews();
                            }

                            // Get LatLng
                            LatLng latLng = item.getLatLng();

                            // Remove search result list
                            searchResultList.setVisibility(View.GONE);

                            // Close soft keypad
                            View view = getCurrentFocus();
                            if (view != null) {
                                InputMethodManager manager =
                                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            // Set selected location
                            selectedLocation = item;

                            // Update camera position
                            updateCamera();

                            // Make location information window
                            showInfoWindow();

                            // Show location select button
                            showBottomLayout();
                        }
                    });
                }
            };

    private void updateCamera() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (selectedLocation == null) {
                    return;
                }

                // Get camera update
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(selectedLocation.getLatLng());

                // Move to position
                naverMap.moveCamera(cameraUpdate);

                // Zoom in camera
                naverMap.moveCamera(CameraUpdate.zoomTo(18.0f));
            }
        });
    }

    private void showBottomLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (selectedLocation == null) {
                    return;
                }

                // Animation for selection location information
                Animation animation = AnimationUtils
                        .loadAnimation(getApplicationContext(), R.anim.event_location_button_action);
                animation.setDuration(300);

                // Add fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment oldFragment = fragmentManager.findFragmentById(R.id.eventLocationSearchInformation);

                if (oldFragment != null) {
                    // We recycle old fragment
                    EventLocationSelectionFragment locationFragment =
                            (EventLocationSelectionFragment) oldFragment;

                    // Set location
                    locationFragment.setLocation(selectedLocation);
                } else {
                    // Create selected location fragment
                    EventLocationSelectionFragment newFragment = new EventLocationSelectionFragment();

                    // Set location
                    newFragment.setLocation(selectedLocation);

                    // Add fragment
                    fragmentManager.beginTransaction()
                            .add(R.id.eventLocationSearchInformation, newFragment)
                            .commit();
                }

                // Show bottom layout and start animation
                bottomLayout.setVisibility(View.VISIBLE);
                bottomLayout.startAnimation(animation);
            }
        });
    }

    private void showInfoWindow() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (selectedLocation == null) {
                    return;
                }

                LatLng latLng = selectedLocation.getLatLng();

                // Make location information window
                infoWindow = new InfoWindow();
                infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(getApplicationContext()) {
                    @NonNull
                    @Override
                    protected View getContentView(@NonNull InfoWindow infoWindow) {
                        return getLocationInfoView(selectedLocation);
                    }
                });

                // Setup information window and show
                infoWindow.setPosition(latLng);
                infoWindow.setAnchor(new PointF(1,1));
                infoWindow.setAlpha(0.9f);
                infoWindow.setMap(naverMap);
            }
        });
    }

    private View getLocationInfoView(LocationSearchResult result) {
        if (locationInfoView == null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            locationInfoView = inflater.inflate(R.layout.event_location_information,
                    null, false);
        }

        TextView locationInfoTitle = locationInfoView.findViewById(R.id.locationInfoTitle);
        TextView locationInfoAddress = locationInfoView.findViewById(R.id.locationInfoAddress);
        TextView locationInfoCategory = locationInfoView.findViewById(R.id.locationInfoCategory);

        locationInfoTitle.setText(result.getTitle());
        locationInfoAddress.setText(result.getAddress());
        locationInfoCategory.setText("");

        return locationInfoView;
    }

    private NaverMap.OnSymbolClickListener symbolClickListener =
            new NaverMap.OnSymbolClickListener() {
        @Override
        public boolean onSymbolClick(@NonNull Symbol symbol) {
            String caption = symbol.getCaption();
            LatLng position = symbol.getPosition();

            // Scroll to selected symbol
            naverMap.moveCamera(CameraUpdate.scrollTo(position));

            // Popup location information

            return true;
        }
    };

    @UiThread
    @Override
    // This method is called once to get NaverMap instance
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        naverMap.setOnSymbolClickListener(symbolClickListener);

        if (selectedLocation != null) {
            // Update camera
            updateCamera();

            // Make location information window
            showInfoWindow();

            // Show location select button
            showBottomLayout();
        }
    }

    protected NaverMapOptions createMapOption() {
        NaverMapOptions options = new NaverMapOptions()
                .camera(new CameraPosition
                (new LatLng(37.621049, 126.839024), 8))
                .mapType(NaverMap.MapType.Basic)
                .enabledLayerGroups(NaverMap.LAYER_GROUP_TRANSIT)
                .symbolScale(1.5f)
                .indoorLevelPickerEnabled(true)
                .tiltGesturesEnabled(false)
                .rotateGesturesEnabled(false);

        return options;
    }

    // Location search callback
    public void onLocationSearchResult(ArrayList<LocationSearchResult> results) {
        // Check if result is null or has no item
        if (results == null || results.size() == 0) {
            // Set search list gone
            searchResultList.setVisibility(View.GONE);

            // Show select button if location is selected
            if (selectedLocation != null) {
                showBottomLayout();
            }

            return;
        }

        // Create list adapter
        adapter = new LocationListAdapter(this);

        // Set search result
        adapter.setLocations(results);

        // Set view creation callback
        adapter.setViewCreationCallback(searchListItemCallback);

        // Set search list visible
        searchResultList.setVisibility(View.VISIBLE);

        // Set adapter of ListView
        searchResultList.setAdapter(adapter);

        // Bring to front
        searchResultList.bringToFront();

        // Remove bottom layout
        bottomLayout.setVisibility(View.GONE);
    }
}
