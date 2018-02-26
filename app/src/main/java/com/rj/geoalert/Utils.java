package com.rj.geoalert;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.rj.geoalert.callbacks.PermissionGrantedCallback;

import static com.rj.geoalert.Constants.MULTIPLE_PERMISSION_REQUEST_CODE;

/**
 * Created by rahul.janagouda on 26/02/18.
 */

public class Utils {

    public static void checkPermissionsState(Context mContext, PermissionGrantedCallback permissionGrantedCallback) {
        int internetPermissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET);

        int networkStatePermissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_NETWORK_STATE);

        int coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int wifiStatePermissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_WIFI_STATE);

        if (internetPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                networkStatePermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                wifiStatePermissionCheck == PackageManager.PERMISSION_GRANTED) {

            permissionGrantedCallback.setup();

        } else {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE},
                    MULTIPLE_PERMISSION_REQUEST_CODE);
        }
    }
}
