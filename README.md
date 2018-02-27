# GeoAlert
Map is shown with the help of Play Services, however Play services are neither used for Location Fixes nor GeoFencing.

## MapsActivity :

1. Helps user to select a point on map, defining a radius and setting an Alert to monitor the Entry/Exit of device within the defined Area.
2. This also requests GPS permission and handles granting and denial using EasyPermission
3. Important state of the applicaion like Location, Radius and Center points are cached in shared preferances to give the user a better experience 

## LocationService :

## LocationReceiver :

## NotificationHelper :

## ObservableObject :

## GeoApplication :
