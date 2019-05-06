package com.murphy.pokotalk.data.locationShare;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.server.PokoServer;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationMeasure {
    private static boolean started = false;
    private static FusedLocationProviderClient fusedLocationClient;
    private static LocationCallback locationCallback;
    private static LocationRequest locationRequest;
    private static ArrayList<LocationShareRoom> rooms;

    private static final int REQUEST_INTERVAL_FASTEST = 1000;
    private static final int REQUEST_INTERVAL_NORMAL = 2000;

    public synchronized static void startMeasure(Context context, final Activity activity) {
        if (!started) {
            // Start measure
            started = true;

            // Get fused location client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            // Create location request
            locationRequest = new LocationRequest();
            locationRequest.setInterval(REQUEST_INTERVAL_NORMAL);
            locationRequest.setFastestInterval(REQUEST_INTERVAL_FASTEST);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Make location setting request
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

            // Add location request
            builder.addLocationRequest(locationRequest);

            // Get setting client
            SettingsClient client = LocationServices.getSettingsClient(context);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            // Add success listener
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // Finish start measure
                    postStartMeasure();
                }
            });

            // Add fail listener
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed, need to restart after resolving
                    stopMeasure();

                    if (e instanceof ResolvableApiException) {
                        try {
                            // Resolve location setting failure
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity,
                                    Constants.RequestCode.LOCATION_SETTING.value);
                        } catch (IntentSender.SendIntentException sendEx) {
                            Log.e("Poko", "FAILED TO INITIALIZE LOCATION REQUEST");
                        }
                    }
                }
            });
        }
    }

    private synchronized static void postStartMeasure() {
        if (!started) {
            return;
        }

        // Make location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // Get locations
                List<Location> locationList = locationResult.getLocations();

                if (locationList.size() <= 0) {
                    return;
                }

                // Get coordinates
                double latitude = 0;
                double longitude = 0;
                long timestamp = -1;

                // Find latest location
                for (Location location : locationList) {
                    long curTimestamp = location.getTime();

                    // Compare timestamp
                    if (timestamp < curTimestamp) {
                        timestamp = curTimestamp;

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }

                // Get server
                PokoServer server = PokoServer.getInstance();
                Log.v("PKOK", "UPDATE Location "+ latitude + ", " + longitude);

                synchronized (LocationMeasure.class) {
                    // Update locations for all attached rooms
                    for (int i = 0; i < rooms.size(); i++) {
                        LocationShareRoom room = rooms.get(i);

                        // Update my location
                        LocationShareHelper.getInstance()
                                .updateMyLocation(room, new LatLng(latitude, longitude));

                        if (room.isJoined()) {
                            Log.v("PKOK", "UPDATE LOCATIOn "+ latitude + ", " + longitude);
                            // Send location to server
                            server.sendUpdateRealtimeLocation(room.getEventId(),
                                    latitude, longitude);
                        }
                    }
                }
            }
        };

        try {
            // Request location update
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void attachRoom(LocationShareRoom room) {
        if (rooms == null) {
            rooms = new ArrayList<>();
        }

        if (rooms.indexOf(room) < 0) {
            rooms.add(room);
        }
    }

    public synchronized static void detachRoom(LocationShareRoom room) {
        if (rooms == null) {
            return;
        }

        // Remove room
        rooms.remove(room);

        if (rooms.size() == 0) {
            // Remove rooms
            rooms = null;

            // Stop measure
            stopMeasure();
        }
    }

    private synchronized static void stopMeasure() {
        if (!started) {
            return;
        }

        // Finish measure
        started = false;

        if (fusedLocationClient != null && locationCallback != null) {
            // Stop measuring location
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // Remove location manager and listener
        fusedLocationClient = null;
        locationRequest = null;
        locationCallback = null;
    }
}
