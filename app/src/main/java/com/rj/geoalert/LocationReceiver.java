package com.rj.geoalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import static com.rj.geoalert.LocationService.LOCATION_KEY;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        Log.i("LocationReceiver", "onreceived");
        if (bundle != null) {
            Location userLocation = bundle.getParcelable(LOCATION_KEY);
            LatLng currentLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            ObservableObject.getInstance().updateValue(currentLocation);

        }
    }
}