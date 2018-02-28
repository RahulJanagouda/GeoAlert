# GeoAlert
A map is shown with the help of Play Services, however, Play services are not used for Location Fixes or GeoFencing.

## MapsActivity :

1. Helps user to select a point on the map, define a radius and set an Alert to monitor the entry/exit of the device within the defined Area. This also helps in stopping the set alert.
2. This also requests GPS permission and handles granting and denial using EasyPermission
3. Important state of the application like Location, Radius, and Center points are cached in SharedPreferances to give the user a better experience
4. This also implements saving the state in the bundle in case the application is destroyed.
5. Once the user sets the alert this triggers the LocationService to monitor the device location.
6. Finally, this will observe any location updates from LocationReceiver using an observable.

## LocationService :
1. Once created this runs as a foreground service and monitors the device location with the help of android.location.LocationManager and android.location.LocationListener.
2. Location fixes are requested based on the enabled providers.
    2.1 As the use of GPS consumes more battery power this is only requested once in five minutes.
    2.2 Also network location fixes cheap compared to GPS this is requested every minute.
    2.3 Finally location updates provided by other apps requests, does not have any effects on the battery. So this is requested every time new fix is available.
3. This service runs with the notification to indicate the user that location is being tracked.  If tapped on the notification, MapsActivity will be opened and the user can stop the alert he had set.  The service continues to monitor the location even after the app is killed.
4. Once any location fix is received from the above listeners BROADCAST_ACTION will be broadcasted.

## LocationReceiver :
1. This broadcast receiver is registered to listen for BROADCAST_ACTION. This will receive the latest location fix from LocationService.
2. This will calculate whether the current location is inside the preset Geofence or outside using
Location.distanceBetween(selectedLocation.latitude, selectedLocation.longitude, currentLocation.latitude, currentLocation.longitude, results);

if the distance is greater than the radius then the device is outside the geofence or inside.
3. Finally, it will send the INSIDE/OUTSIDE notification with the help of NotificationHelper.

## NotificationHelper :
1. This is a helper class which helps with Notification Channels, Sending notifications and Cancelling all notifications.

## ObservableObject :
Its a custom Singleton Observable object which helps in observing location changes from MapsActivity.

## GeoApplication :
Application object used to register/unregister LocationReceiver .
