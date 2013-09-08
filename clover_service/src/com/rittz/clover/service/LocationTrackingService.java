package com.rittz.clover.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class LocationTrackingService extends Service {

    private static final String EXTRA_TRACK = "com.rittz.clover.service.track";

    private static boolean trackingLocation = false;

    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final boolean track = intent.getBooleanExtra(EXTRA_TRACK, false);

        if (track) {
            startTracking();
        }
        else {
            stopTracking();
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startTracking() {
        final String provider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(provider, 1000, 10, locationListener);
        trackingLocation = true;
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(final String provider, final int status, final Bundle extras) {
            // Stub
        }

        @Override
        public void onProviderEnabled(final String provider) {
            // Stub
        }

        @Override
        public void onProviderDisabled(final String provider) {
            // Stub
        }

        @Override
        public void onLocationChanged(final Location location) {
            // Stub
        }
    };

    private void stopTracking() {
        locationManager.removeUpdates(locationListener);
        trackingLocation = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public static boolean isTrackingLocation() {
        return trackingLocation;
    }

    public static Intent makeIntent(final Context ctx, final boolean trackLocation) {
        final Intent intent = new Intent(ctx, LocationTrackingService.class);
        intent.putExtra(EXTRA_TRACK, trackLocation);
        return intent;
    }
}
