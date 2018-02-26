package com.rj.geoalert;

import android.app.Application;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by rahul.janagouda on 27/02/18.
 */

public class GeoApplication extends Application {
    LocationReceiver locationReceiver = new LocationReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(locationReceiver, new IntentFilter(LocationService.BROADCAST_ACTION));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(locationReceiver);
    }
}
