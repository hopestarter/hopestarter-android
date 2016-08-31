package org.hopestarter.wallet.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Adrian on 31/08/2016.
 */
public class AsyncLocationRequest {
    private final GoogleApiClient mGoogleApiClient;
    private LocationRequestListener mListener;
    private Handler mHandler;
    private long mRequestTime;
    private long mTimeout;
    private boolean mUsed;

    private LocationListener mLocationListener = new LocationListener() {
        private boolean used;

        @Override
        public void onLocationChanged(Location location) {
            if (!used) { // Location service runs in another thread, this avoids this callback to run more than once
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this); // Don't need the callback anymore
                used = true;
                mHandler.removeCallbacks(mTimeoutRunnable);
                if (mListener != null) {
                    mListener.onLocationFound(location);
                }
            }
        }
    };

    private Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = System.currentTimeMillis() - mRequestTime;
            if (elapsed >= mTimeout) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                if (mListener != null) {
                    mListener.onLocationRequestTimeout();
                }
            } else {
                mHandler.postDelayed(this, mTimeout - elapsed);
            }
        }
    };

    public interface LocationRequestListener {
        void onLocationFound(Location location);

        void onLocationRequestTimeout();
    }

    public AsyncLocationRequest(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
        mHandler = new Handler();
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void request() {
        if (mUsed) {
            throw new IllegalStateException("This AsyncLocationRequest instance has been already used");
        }
        mUsed = true;

        if (mTimeout != 0) {
            mHandler.postDelayed(mTimeoutRunnable, 500);
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(5000);

        mRequestTime = System.currentTimeMillis();

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, mLocationListener);
    }

    public void setLocationRequestListener(LocationRequestListener listener) {
        mListener = listener;
    }

    public void setTimeout (long timeoutMillis) {
        if (mUsed) {
            throw new IllegalStateException("This AsyncLocationRequest instance has been already started");
        }
        mTimeout = timeoutMillis;
    }
}
