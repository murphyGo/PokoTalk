package com.murphy.pokotalk.activity.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.Symbol;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationShareFragment extends Fragment
        implements OnMapReadyCallback,
        LocationShareSelectUserDialog.Listener {
    private PokoServer server;
    private int eventId = -1;
    private Handler handler;
    private Button myPositionButton;
    private Button userPositionButton;
    private Button meetingLocationButton;
    private MapView mapLayout;
    private LocationShareRoom room;
    private boolean locationFinePermission;
    private boolean locationCoarsePermission;
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

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate view
        View view = inflater.inflate(R.layout.location_share_fragment, null, false);

        FragmentActivity activity = getActivity();
        Context context = getContext();

        // Event id should exist
        if (activity == null || context == null || eventId < 0) {
            return null;
        }

        // Initialize data structures
        markers = new HashMap<>();

        // Find views
        myPositionButton = view.findViewById(R.id.locationShareMyPositionButton);
        userPositionButton = view.findViewById(R.id.locationShareUserPositionButton);
        meetingLocationButton = view.findViewById(R.id.locationShareMeetingPositionButton);
        mapLayout = view.findViewById(R.id.locationShareMapLayout);

        // Add event listeners
        myPositionButton.setOnClickListener(myLocationButtonListener);
        userPositionButton.setOnClickListener(userLocationButtonListener);
        meetingLocationButton.setOnClickListener(meetingLocationButtonListener);

        // Get location share helper
        LocationShareHelper helper = LocationShareHelper.getInstance();

        // Get location share room
        room = helper.getRoomOrCreateIfNotExists(eventId);

        // Check for permissions
        locationFinePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        locationCoarsePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (!locationFinePermission || !locationCoarsePermission) {
            // Request for permission
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.LOCATION_PERMISSION);
        } else {
            // Start measure location
            room.startMeasure(context, activity);
        }

        // Get server
        server = PokoServer.getInstance();

        if (!room.isJoined()) {
            // Join location share
            server.sendJoinRealtimeLocationShare(eventId, null);
        }

        // Add server listeners
        server.attachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.attachActivityCallback(Constants.joinRealtimeLocationShareName, joinListener);
        server.attachActivityCallback(Constants.exitRealtimeLocationShareName, exitListener);
        server.attachActivityCallback(Constants.updateRealtimeLocationName, updateListener);
        server.attachActivityCallback(Constants.realtimeLocationShareBroadcastName, broadcastListener);
        
        // Get NaverMap instance asynchronously
        mapLayout.getMapAsync(this);
        
        // Make handler
        handler = new Handler(Looper.getMainLooper());
        
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Detach listeners
        server.detachActivityCallback(Constants.sessionLoginName, sessionLoginListener);
        server.detachActivityCallback(Constants.joinRealtimeLocationShareName, joinListener);
        server.detachActivityCallback(Constants.exitRealtimeLocationShareName, exitListener);
        server.detachActivityCallback(Constants.updateRealtimeLocationName, updateListener);
        server.detachActivityCallback(Constants.realtimeLocationShareBroadcastName, broadcastListener);
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
        
        // Get context
        Context context = getContext();
        
        if (context == null) {
            return;
        }

        // Make location information window
        meetingLocationWindow = new InfoWindow();
        meetingLocationWindow.setAdapter(new InfoWindow.DefaultViewAdapter(context) {
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
        // Get context
        Context context = getContext();

        if (context == null) {
            return null;
        }
        
        if (locationInfoView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
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
            // Get my location
            LocationShare myShare = room.getMyLocation();

            if (naverMap != null && myShare != null) {
                // Move to my location
                naverMap.moveCamera(CameraUpdate.scrollTo(myShare.getLatLng()));
            }
        }
    };

    @Override
    public void onSelectUserPosition(String key) {
        UserPosition position = markers.get(key);

        if (naverMap != null && position != null) {
            // Get marker
            Marker marker = position.getMarker();

            if (marker != null) {
                // Move to marker position
                naverMap.moveCamera(CameraUpdate.scrollTo(marker.getPosition()));
            }
        }
    }

    private View.OnClickListener userLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get context
            FragmentActivity activity = getActivity();

            if (activity == null) {
                return;
            }
            
            //Start user selection dialog
            LocationShareSelectUserDialog dialog = new LocationShareSelectUserDialog();
            dialog.setMarkers((HashMap<String, UserPosition>) markers.clone());
            dialog.setListener(LocationShareFragment.this);
            dialog.show(activity.getSupportFragmentManager(), "user selection");
        }
    };

    private View.OnClickListener meetingLocationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Get meeting location
            MeetingLocation meetingLocation = room.getMeetingLocation();

            if (naverMap != null && meetingLocation != null) {
                // Move to meeting location
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
            // Get context
            final Context context = getContext();
            final FragmentActivity activity = getActivity();

            if (activity == null || context == null) {
                return;
            }
            
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Connection is lost and reconnected,
                    // join location share again
                    Toast.makeText(context, R.string.location_share_reconnect,
                            Toast.LENGTH_SHORT).show();

                    if (room != null) {
                        // Stop measure
                        room.stopMeasure();

                        // Get my location
                        LocationShare myLocation = room.getMyLocation();

                        if (myLocation != null) {
                            // Send join message
                            server.sendJoinRealtimeLocationShare(eventId, myLocation.getNumber());
                        } else {
                            // Send join message
                            server.sendJoinRealtimeLocationShare(eventId, null);
                        }

                        // Measure again
                        room.startMeasure(context, activity);
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

            // Get context
            final Context context = getContext();

            if (context == null) {
                return;
            }

            if (id != null && id == eventId) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
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

            // Get context
            final Context context = getContext();

            if (context == null) {
                return;
            }

            if (id != null && id == eventId) {

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

                handler.post(new Runnable() {
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Get context
        final Context context = getContext();
        final FragmentActivity activity = getActivity();

        if (activity == null || context == null) {
            return;
        }

        if (requestCode == Constants.RequestCode.LOCATION_SETTING.value) {
            if (requestCode == Activity.RESULT_OK) {
                room.startMeasure(context, activity);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Get context
        final Context context = getContext();
        final FragmentActivity activity = getActivity();

        if (activity == null || context == null) {
            return;
        }

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
                    room.startMeasure(context, activity);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapLayout.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapLayout.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapLayout.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapLayout.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapLayout.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapLayout.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapLayout.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapLayout.onLowMemory();
    }
}
