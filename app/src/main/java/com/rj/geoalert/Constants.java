package com.rj.geoalert;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rahul.janagouda on 26/02/18.
 */

class Constants {
    static final int RC_LOCATION_PERM = 100;

    public static final String INTENT_KEY_RADIUS = "radiusKey";
    public static final String INTENT_KEY_CENTER = "centerKey";
    public static final String KEY_IS_ALERT_SET = "isAlertSet";
    public static final String KEY_SELECTED_LATITUDE = "selectedLatitude";
    public static final String KEY_SELECTED_LONGITUDE = "selectedLongitude";
    public static final String KEY_LOCATION_LATITUDE = "locationLatitude";
    public static final String KEY_LOCATION_LONGITUDE = "locationLongitude";

    public static final Integer DEFAULT_RADIUS = 50;
    public static final LatLng DEFAULT_CENTER = new LatLng(17.3850, 78.4867);


}
