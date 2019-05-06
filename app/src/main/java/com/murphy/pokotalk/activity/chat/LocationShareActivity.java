package com.murphy.pokotalk.activity.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.data.locationShare.LocationShare;
import com.murphy.pokotalk.data.locationShare.LocationShareData;
import com.murphy.pokotalk.data.locationShare.LocationShareHelper;
import com.murphy.pokotalk.data.locationShare.LocationShareRoom;
import com.murphy.pokotalk.data.locationShare.MeetingLocation;
import com.murphy.pokotalk.data.user.User;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Symbol;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationShareActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private AppCompatActivity activity;
    private PokoServer server;
    private int eventId;
    private Button backspaceButton;
    private Button myPositionButton;
    private Button meetingLocationButton;
    private LocationShareRoom room;
    private boolean locationFinePermission;
    private boolean locationCoarsePermission;
    private MapFragment fragment;
    private NaverMap naverMap;
    private InfoWindow meetingLocationWindow;
    private View locationInfoView;
    private HashMap<String, UserPosition> markers;
    private OverlayImage[] icons = {MarkerIcons.BLUE,
                                    MarkerIcons.GRAY,
                                    MarkerIcons.GREEN,
                                    MarkerIcons.LIGHTBLUE,
                                    MarkerIcons.PINK,
                                    MarkerIcons.RED,
                                    MarkerIcons.YELLOW,};

    static class UserPosition {
        private String key;
        private boolean updated = false;
        private Marker marker;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }

        public Marker getMarker() {
            return marker;
        }

        public void setMarker(Marker marker) {
            this.marker = marker;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_share_activity);

        activity = this;

        // Get intent
        Intent intent = getIntent();

        // Get event id
        eventId = intent.getIntExtra("eventId", -1);

        // Event id should exist
        if (eventId < 0) {
            finish();
            return;
        }

        // Initialize data structures
        markers = new HashMap<>();

        // Find views
        backspaceButton = findViewById(R.id.locationShareBackspaceButton);
        myPositionButton = findViewById(R.id.locationShareMyPositionButton);
        meetingLocationButton = findViewById(R.id.locationShareMeetingPositionButton);

        // Add event listeners
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        myPositionButton.setOnClickListener(myLocationButtonListener);
        meetingLocationButton.setOnClickListener(meetingLocationButtonListener);

        // Get location share helper
        LocationShareHelper helper = LocationShareHelper.getInstance();

        // Get location share room
        room = helper.getRoom(eventId);

        // Check for permissions
        locationFinePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        locationCoarsePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!locationFinePermission || locationCoarsePermission) {
            // Request for permission
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION);
        } else {
            // Start measure location
            room.startMeasure(getApplicationContext(), this);
        }

        // Get server
        server = PokoServer.getInstance();

        if (!room.isJoined()) {
            // Join location share
            server.sendJoinRealtimeLocationShare(eventId);
        }

        // Add server listeners
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.attachActivityCallback(Constants.joinRealtimeLocationShareName, joinListener);
        server.attachActivityCallback(Constants.exitRealtimeLocationShareName, exitListener);
        server.attachActivityCallback(Constants.updateRealtimeLocationName, updateListener);
        server.attachActivityCallback(Constants.realtimeLocationShareBroadcastName, broadcastListener);

        // Create map fragment and add
        fragment = MapFragment.newInstance(createMapOption());
        fragment.setArguments(new Bundle());
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.locationShareMapLayout, fragment).
                commit();

        // Get NaverMap instance asynchronously
        fragment.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Detach listeners
        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.detachActivityCallback(Constants.joinRealtimeLocationShareName, joinListener);
        server.detachActivityCallback(Constants.exitRealtimeLocationShareName, exitListener);
        server.detachActivityCallback(Constants.updateRealtimeLocationName, updateListener);
        server.detachActivityCallback(Constants.realtimeLocationShareBroadcastName, broadcastListener);
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    private void finishActivity() {
        // Exit location share
        server.sendExitRealtimeLocationShare(eventId);

        // Finish activity
        finish();
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

    @UiThread
    @Override
    // This method is called once to get NaverMap instance
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        Log.v("POKO", "NAVER MAP READY");
        // Set click listener
        naverMap.setOnSymbolClickListener(symbolClickListener);

        // Show meeting location
        showMeetingPositionOnMap();

        // Show users positions
        showUserPositionsOnMap();
    }

    private synchronized void showMeetingPositionOnMap() {
        // Get meeting location
        final MeetingLocation location = room.getMeetingLocation();

        if (naverMap == null || location == null) {
            return;
        }

        // Get coordinate
        LatLng latLng = location.getLatLng();

        if (meetingLocationWindow != null) {
            return;
        }

        // Make location information window
        meetingLocationWindow = new InfoWindow();
        meetingLocationWindow.setAdapter(new InfoWindow.DefaultViewAdapter(getApplicationContext()) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                return getLocationInfoView(location);
            }
        });

        // Setup information window and show
        meetingLocationWindow.setPosition(latLng);
        meetingLocationWindow.setAnchor(new PointF(1,1));
        meetingLocationWindow.setAlpha(0.9f);
        meetingLocationWindow.setMap(naverMap);
    }

    private synchronized void showUserPositionsOnMap() {
        LocationShareData data = room.getLocationData();

        if (naverMap == null || data != null) {
            ArrayList<LocationShare> locationShares = data.getList();

            for (int i = 0; i < locationShares.size(); i++) {
                LocationShare share = locationShares.get(i);

                // Parse location share data
                User user = share.getUser();
                LatLng latLng = share.getLatLng();
                int number = share.getNumber();

                // Data should exist
                if (user == null || latLng == null || number < 0) {
                    continue;
                }

                // Show location data on map
                String key = data.getKey(share);

                // Get user position
                UserPosition position = markers.get(key);

                if (position == null) {
                    // Create position
                    position = new UserPosition();

                    // Create marker
                    Marker marker = new Marker();
                    marker.setPosition(latLng);
                    marker.setMap(naverMap);
                    marker.setIconPerspectiveEnabled(true);
                    marker.setCaptionText(user.getNickname());

                    // Randomly make marker icon
                    int index = (int) Math.floor(7 * Math.random());
                    marker.setIcon(icons[index]);

                    // Set marker
                    position.setMarker(marker);

                    // Set key
                    position.setKey(key);

                    // Add to list
                    markers.put(key, position);
                }

                // Set updated
                position.setUpdated(true);

                // Get marker
                Marker marker = position.getMarker();

                // Update position
                marker.setPosition(latLng);
            }

            // Remove markers that are not visited
            ArrayList<UserPosition> positions = new ArrayList<>(markers.values());

            // Loop through all position
            for (int i = 0; i < positions.size(); i++) {
                UserPosition position = positions.get(i);

                if (!position.isUpdated()) {
                    // Remove marker from map
                    position.getMarker().setMap(null);

                    // Remove position
                    markers.remove(position.getKey());
                } else {
                    // Set not updated
                    position.setUpdated(false);
                }
            }
        }
    }

    private View getLocationInfoView(MeetingLocation location) {
        if (locationInfoView == null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            locationInfoView = inflater.inflate(R.layout.event_location_information,
                    null, false);
        }

        TextView locationInfoTitle = locationInfoView.findViewById(R.id.locationInfoTitle);
        TextView locationInfoAddress = locationInfoView.findViewById(R.id.locationInfoAddress);
        TextView locationInfoCategory = locationInfoView.findViewById(R.id.locationInfoCategory);

        locationInfoTitle.setText(location.getLocationName());
        locationInfoAddress.setText(location.getDescription());
        locationInfoCategory.setText("");

        return locationInfoView;
    }

    private View.OnClickListener myLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LocationShare myShare = room.getMyLocation();

            if (naverMap != null && myShare != null) {
                naverMap.moveCamera(CameraUpdate.scrollTo(myShare.getLatLng()));
            }
        }
    };

    private View.OnClickListener meetingLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MeetingLocation meetingLocation = room.getMeetingLocation();

            if (naverMap != null && meetingLocation != null) {
                naverMap.moveCamera(CameraUpdate.scrollTo(meetingLocation.getLatLng()));
            }
        }
    };

    private NaverMap.OnSymbolClickListener symbolClickListener =
            new NaverMap.OnSymbolClickListener() {
                @Override
                public boolean onSymbolClick(@NonNull Symbol symbol) {
                    String caption = symbol.getCaption();
                    LatLng position = symbol.getPosition();

                    // Scroll to selected symbol
                    naverMap.moveCamera(CameraUpdate.scrollTo(position));

                    return true;
                }
            };


    private ActivityCallback sessionLoginListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Connection is lost and reconnected,
                    // join location share again
                    Toast.makeText(getApplicationContext(), R.string.location_share_reconnect,
                            Toast.LENGTH_SHORT).show();

                    if (room != null) {
                        // Stop measure
                        room.stopMeasure();

                        // Send join message
                        server.sendJoinRealtimeLocationShare(eventId);

                        // Measure again
                        room.startMeasure(getApplicationContext(), activity);
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback joinListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer id = (Integer) getData("eventId");

            if (id != null && id == eventId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.location_share_joined,
                                Toast.LENGTH_SHORT).show();

                        if (naverMap != null) {
                            // Show meeting position
                            showMeetingPositionOnMap();

                            // Show users position
                            showUserPositionsOnMap();
                        }
                    }
                });
            }

        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback exitListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer id = (Integer) getData("eventId");

            if (id != null && id == eventId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.location_share_exited,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback updateListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer id = (Integer) getData("eventId");

            if (id != null && id == eventId) {
                Log.v("POKO", "USER LOCATION DATA UPDATED");
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback broadcastListener = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            Integer id = (Integer) getData("eventId");

            if (id != null && id == eventId) {
                Log.v("POKO", "USER LOCATION DATA UPDATED");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show users position
                        showUserPositionsOnMap();
                    }
                });
            }
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.RequestCode.LOCATION_SETTING.value) {
            if (requestCode == RESULT_OK) {
                room.startMeasure(getApplicationContext(), this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.LOCATION_PERMISSION) {
            // Check permission granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        continue;
                    }

                    switch (permissions[i]) {
                        case Manifest.permission.ACCESS_FINE_LOCATION: {
                            locationFinePermission = true;
                            break;
                        }
                        case Manifest.permission.ACCESS_COARSE_LOCATION: {
                            locationCoarsePermission = true;
                            break;
                        }
                    }
                }

                if (locationFinePermission && locationCoarsePermission) {
                    // Start measure
                    room.startMeasure(getApplicationContext(), this);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
