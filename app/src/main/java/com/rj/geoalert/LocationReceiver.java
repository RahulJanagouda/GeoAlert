package com.rj.geoalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import static com.rj.geoalert.Constants.DEFAULT_RADIUS;
import static com.rj.geoalert.Constants.INTENT_KEY_CENTER;
import static com.rj.geoalert.Constants.INTENT_KEY_RADIUS;
import static com.rj.geoalert.LocationService.LOCATION_KEY;

public class LocationReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_NUMBER = 415;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        Log.i("LocationReceiver", "onreceived");
        if (bundle != null) {

            LatLng selectedLocation = intent.getParcelableExtra(INTENT_KEY_CENTER);
            Integer radiusInMeters = intent.getIntExtra(INTENT_KEY_RADIUS, DEFAULT_RADIUS);

            Location userLocation = bundle.getParcelable(LOCATION_KEY);
            LatLng currentLocation = null;
            if (userLocation != null) {
                currentLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            }

            float[] results = new float[1];

            if (selectedLocation != null) {
                Location.distanceBetween(selectedLocation.latitude, selectedLocation.longitude, currentLocation.latitude, currentLocation.longitude, results);
                float distanceInMeters = results[0];
                NotificationHelper notificationHelper = new NotificationHelper(context);
                if (distanceInMeters < radiusInMeters * 1000) {
                    notificationHelper.notify(NOTIFICATION_NUMBER, notificationHelper.getAlertNotification("GeoAlert", "ENTERED"));
                } else {
                    notificationHelper.notify(NOTIFICATION_NUMBER, notificationHelper.getAlertNotification("GeoAlert", "EXITED"));
                }
            }

            ObservableObject.getInstance().updateValue(currentLocation);
        }
    }
}