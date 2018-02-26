package com.rj.geoalert;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import static com.rj.geoalert.Constants.RC_LOCATION_PERM;
import static com.rj.geoalert.LocationService.LOCATION_KEY;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks, Observer {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private LatLng currentLocation;
    private int mFillColorArgb = Color.HSVToColor(70, new float[]{1, 1, 1});

    private Integer radiusInMeters = 50; //default radius to see the previousBestLocation

    private static final String[] LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ObservableObject.getInstance().addObserver(this);

        EditText radiusEditText = findViewById(R.id.radiusEditText);
        Button alertButton = findViewById(R.id.alertButton);

        radiusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0)
                    radiusInMeters = Integer.parseInt(s.toString());
                setPoints();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupLocationAccess();

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
        selectedLocation = new LatLng(17.3850, 78.4867);
        setPoints();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                selectedLocation = point;
                setPoints();
            }
        });
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    private void setupLocationAccess() {

        if (hasLocationPermission()) {
            Intent intent = new Intent(this, LocationService.class);
            startService(intent);
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

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
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

    void setPoints() {
        if (mMap != null && selectedLocation != null) {
            MarkerOptions marker = new MarkerOptions().position(selectedLocation).title("Center Point");

            mMap.clear();
            mMap.addMarker(marker);

            mMap.addCircle(new CircleOptions()
                    .center(selectedLocation)
                    .radius(1000 * radiusInMeters)
                    .strokeColor(mFillColorArgb)
                    .fillColor(mFillColorArgb));
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
        Toast.makeText(this, String.valueOf("activity observer " + arg), Toast.LENGTH_SHORT).show();
        currentLocation = (LatLng) arg;

        float[] results = new float[1];
        Location.distanceBetween(selectedLocation.latitude, selectedLocation.longitude, currentLocation.latitude, currentLocation.longitude, results);
        float distanceInMeters = results[0];
        boolean isWithin = distanceInMeters < radiusInMeters * 1000;

        if (distanceInMeters < radiusInMeters * 1000){
            Toast.makeText(this, "IN", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "OUT", Toast.LENGTH_SHORT).show();
        }

        setPoints();
    }
}
