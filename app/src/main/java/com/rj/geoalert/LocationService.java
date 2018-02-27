package com.rj.geoalert;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static com.rj.geoalert.Constants.DEFAULT_RADIUS;
import static com.rj.geoalert.Constants.INTENT_KEY_CENTER;
import static com.rj.geoalert.Constants.INTENT_KEY_RADIUS;

public class LocationService extends Service {

    private static final int FOREGROUND = 284;
    public static final String BROADCAST_ACTION = "GeoMapNewLocationBroadCast";
    public static final String LOCATION_KEY = "NewLocationKey";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 100 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5 ; // 5 minute
    private static final String TAG = LocationService.class.getSimpleName();

    private LocationManager locationManager;
    private MyLocationListener listener;
    private Location previousBestLocation = null;

    private Intent broadcastIntent;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastIntent = new Intent(BROADCAST_ACTION);
        Log.i(TAG, "Service onCreate");
        startForeground(FOREGROUND, new NotificationHelper(this).getOnGoingNotification("Location Service", "Running").build());
    }


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");


        broadcastIntent.putExtra(INTENT_KEY_RADIUS, intent.getIntExtra(INTENT_KEY_RADIUS, DEFAULT_RADIUS));
        broadcastIntent.putExtra(INTENT_KEY_CENTER, intent.getParcelableExtra(INTENT_KEY_CENTER));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location location) {
            if (isBetterLocation(location, previousBestLocation)) {
                broadcastIntent.putExtra(LOCATION_KEY, location);
                sendBroadcast(broadcastIntent);
                previousBestLocation = location;
            }
        }

        public void onProviderDisabled(String provider) {
            if(provider.equalsIgnoreCase(NETWORK_PROVIDER)){
                Toast.makeText(getApplicationContext(), "No Network", Toast.LENGTH_SHORT).show();
            }

            if(provider.equalsIgnoreCase(GPS_PROVIDER)){
                Toast.makeText(getApplicationContext(), "Please turn on GPS to work", Toast.LENGTH_SHORT).show();
            }
        }

        public void onProviderEnabled(String provider) {
            if(provider.equalsIgnoreCase(NETWORK_PROVIDER)){
                Toast.makeText(getApplicationContext(), "Network turned on", Toast.LENGTH_SHORT).show();
            }

            if(provider.equalsIgnoreCase(GPS_PROVIDER)){
                Toast.makeText(getApplicationContext(), "GPS turned on", Toast.LENGTH_SHORT).show();
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            locationManager.removeUpdates(listener);
        Log.i(TAG, "Service onDestroy");

    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new previousBestLocation is always better than no previousBestLocation
            return true;
        }

        // Check whether the new previousBestLocation fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current previousBestLocation, use the new previousBestLocation
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new previousBestLocation is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new previousBestLocation fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new previousBestLocation are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine previousBestLocation quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}