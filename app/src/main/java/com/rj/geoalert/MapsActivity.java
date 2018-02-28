package com.rj.geoalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.rj.geoalert.Constants.DEFAULT_RADIUS;
import static com.rj.geoalert.Constants.INTENT_KEY_CENTER;
import static com.rj.geoalert.Constants.INTENT_KEY_RADIUS;
import static com.rj.geoalert.Constants.KEY_IS_ALERT_SET;
import static com.rj.geoalert.Constants.KEY_LOCATION_LATITUDE;
import static com.rj.geoalert.Constants.KEY_LOCATION_LONGITUDE;
import static com.rj.geoalert.Constants.KEY_SELECTED_LATITUDE;
import static com.rj.geoalert.Constants.KEY_SELECTED_LONGITUDE;
import static com.rj.geoalert.Constants.RC_LOCATION_PERM;
import static com.rj.geoalert.LocationReceiver.NOTIFICATION_NUMBER;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks, Observer {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private LatLng currentLocation;
    private final int geoFenceColorArgb = Color.HSVToColor(70, new float[]{1, 1, 1});
    private Integer radiusInMeters; //default radius to see the previousBestLocation

    private static final String[] LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private boolean isAlertSet = false;
    private EditText radiusEditText;
    private Button alertButton;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ObservableObject.getInstance().addObserver(this);
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        isAlertSet = sp.getBoolean(KEY_IS_ALERT_SET, false);
        selectedLocation = new LatLng(sp.getFloat(KEY_SELECTED_LATITUDE, 17.3850f),
                sp.getFloat(KEY_SELECTED_LONGITUDE, 78.4867f));
        radiusInMeters = sp.getInt(INTENT_KEY_RADIUS, DEFAULT_RADIUS);

        currentLocation = new LatLng(sp.getFloat(KEY_LOCATION_LATITUDE, 17.3850f),
                sp.getFloat(KEY_LOCATION_LONGITUDE, 78.4867f));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        radiusEditText = findViewById(R.id.radiusEditText);
        alertButton = findViewById(R.id.alertButton);

        radiusEditText.setText("" + radiusInMeters);
        radiusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    radiusInMeters = Integer.parseInt(s.toString());
                    editor.putInt(INTENT_KEY_RADIUS, radiusInMeters).apply();
                }
                setPoints();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationService.class);
                intent.putExtra(INTENT_KEY_RADIUS, radiusInMeters);
                intent.putExtra(INTENT_KEY_CENTER, selectedLocation);
                if (!isAlertSet) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }

                    alertButton.setText(R.string.stop);
                    radiusEditText.setEnabled(false);
                    isAlertSet = true;

                } else {
                    stopService(intent);

                    alertButton.setText(R.string.set_alert);
                    radiusEditText.setEnabled(true);

                    isAlertSet = false;
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancelAll();
                    }

                }
                editor.putBoolean(KEY_IS_ALERT_SET, isAlertSet).apply();
            }
        });


        if (isAlertSet) {
            alertButton.setText(R.string.stop);
            radiusEditText.setEnabled(false);
        } else {
            alertButton.setText(R.string.set_alert);
            radiusEditText.setEnabled(true);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupLocationAccess();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        this.radiusInMeters = savedInstanceState.getInt(INTENT_KEY_RADIUS);
        this.selectedLocation = savedInstanceState.getParcelable(INTENT_KEY_CENTER);
        this.isAlertSet = savedInstanceState.getBoolean(KEY_IS_ALERT_SET);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(INTENT_KEY_RADIUS, radiusInMeters);
        outState.putParcelable(INTENT_KEY_CENTER, selectedLocation);
        outState.putBoolean(KEY_IS_ALERT_SET, isAlertSet);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Bangalore and move the camera
        setPoints();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                selectedLocation = point;
                setPoints();

                final SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putFloat(KEY_SELECTED_LATITUDE, (float) point.latitude).apply();
                editor.putFloat(KEY_SELECTED_LONGITUDE, (float) point.longitude).apply();

            }
        });
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION_PERM)
    private void setupLocationAccess() {

        if (hasLocationPermission()) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            setPoints();
            Toast.makeText(this, "Location permission granted. \n 1. Tap Map \n 2. Enter radius \n 3. Set Alert", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    LOCATION);
        }
    }


    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(this, LOCATION);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            String yes = getString(R.string.yes);
            String no = getString(R.string.no);

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                    this,
                    getString(R.string.returned_from_app_settings_to_activity,
                            hasLocationPermission() ? yes : no),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void setPoints() {
        if (mMap != null && selectedLocation != null) {
            MarkerOptions marker = new MarkerOptions().position(selectedLocation).title("Center Point");

            mMap.clear();
            mMap.addMarker(marker);

            mMap.addCircle(new CircleOptions()
                    .center(selectedLocation)
                    .radius(1000 * radiusInMeters)
                    .strokeColor(geoFenceColorArgb)
                    .fillColor(geoFenceColorArgb));
        }

        if (mMap != null && currentLocation != null) {
            MarkerOptions marker = new MarkerOptions()
                    .position(currentLocation)
                    .title("Your previousBestLocation")
                    .icon(BitmapDescriptorFactory.
                            defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            mMap.addMarker(marker);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 8));

        }

        float[] results = new float[1];

        if (selectedLocation != null && currentLocation != null) {
            Location.distanceBetween(selectedLocation.latitude, selectedLocation.longitude, currentLocation.latitude, currentLocation.longitude, results);
            float distanceInMeters = results[0];
            NotificationHelper notificationHelper = new NotificationHelper(this);
            if (distanceInMeters < radiusInMeters * 1000) {
                notificationHelper.notify(NOTIFICATION_NUMBER, notificationHelper.getAlertNotification("GeoAlert", "ENTERED"));
            } else {
                notificationHelper.notify(NOTIFICATION_NUMBER, notificationHelper.getAlertNotification("GeoAlert", "EXITED"));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void update(Observable o, Object arg) {
        currentLocation = (LatLng) arg;

        final SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putFloat(KEY_LOCATION_LATITUDE, (float) currentLocation.latitude).apply();
        editor.putFloat(KEY_LOCATION_LONGITUDE, (float) currentLocation.longitude).apply();

        setPoints();
    }
}
