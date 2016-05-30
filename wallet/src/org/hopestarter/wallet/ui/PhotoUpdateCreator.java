package org.hopestarter.wallet.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.hopestarter.wallet.server_api.LocationMarkUploader;

import org.hopestarter.wallet.server_api.OutboundLocationMark;
import org.hopestarter.wallet.server_api.Point;
import org.hopestarter.wallet.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Adrian on 19/04/2016.
 */
public class PhotoUpdateCreator {
    private static final Logger log = LoggerFactory.getLogger(PhotoUpdateCreator.class);
    private static final String REQUEST_DIALOG_TAG = "requestdialog";
    public static final int LOCATION_REQUEST_TIMEOUT = 10000;
    private final ProgressDialog mProgressDialog;
    private Fragment mFragment;
    private int mReqCode;
    private int mPermissionReqCode;
    private LocationMarkUploader.UploaderListener mListener;
    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;

    public PhotoUpdateCreator(Fragment fragment, GoogleApiClient googleApiClient, int dataReqCode, int permissionReqCode) {
        mFragment = fragment;
        mReqCode = dataReqCode;
        mPermissionReqCode = permissionReqCode;
        mGoogleApiClient = googleApiClient;

        mProgressDialog = new ProgressDialog(fragment.getActivity());
        mProgressDialog.setTitle(fragment.getActivity().getString(R.string.post_update_waiting_dialog_title));
        mProgressDialog.setMessage(fragment.getActivity().getString(R.string.post_update_waiting_dialog_message));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mHandler = new Handler();
    }

    public void setUploadListener(LocationMarkUploader.UploaderListener listener) {
        mListener = listener;
    }

    public void create() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermissions();
        } else {
            launchCreateActivityIfGPSEnabled();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermissions() {
        int locationPermission = mFragment.getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            launchCreateActivityIfGPSEnabled();
        } else {
            PermissionRequestDialog requestDialog = new PermissionRequestDialog();

            requestDialog.setPositiveButton(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission();
                }
            });

            requestDialog.show(mFragment.getFragmentManager(), REQUEST_DIALOG_TAG);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        mFragment.requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, mPermissionReqCode);
    }

    private void launchCreateActivityIfGPSEnabled() {
        if (!isGPSActive()) {
            showEnableGPSDialog();
            return;
        }

        launchCreateNewUpdateActivity();
    }

    private void launchCreateNewUpdateActivity() {
        Intent activityIntent = new Intent(mFragment.getActivity(), CreateNewUpdateActivity.class);
        mFragment.startActivityForResult(activityIntent, mReqCode);
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResult) {
        if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            launchCreateActivityIfGPSEnabled();
        }
    }

    private boolean isGPSActive() {
        LocationManager manager = (LocationManager) mFragment.getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showEnableGPSDialog() {
        new AlertDialog.Builder(mFragment.getActivity())
                .setTitle(R.string.enable_gps_dialog_title)
                .setMessage(R.string.enable_gps_dialog_message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    public void onActivityResult(final Intent data) {
        try {
            if (!isGPSActive()) {
                showEnableGPSDialog();
                return;
            }

            mProgressDialog.show();

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location == null) {
                AsyncLocationRequest locationRequest = new AsyncLocationRequest(mGoogleApiClient);
                locationRequest.setTimeout(LOCATION_REQUEST_TIMEOUT);
                locationRequest.setLocationRequestListener(new AsyncLocationRequest.LocationRequestListener() {
                    @Override
                    public void onLocationFound(Location location) {
                        sendLocationUpdate(location, data);
                    }

                    @Override
                    public void onLocationRequestTimeout() {
                        PhotoUpdateCreator.this.onLocationRequestTimeout();
                    }
                });
                locationRequest.request();
            } else {
                sendLocationUpdate(location, data);
            }
        } catch(SecurityException e) {
            // Permissions have changed while uploading location
        }
    }

    private void onLocationRequestTimeout() {
        mProgressDialog.dismiss();
        new AlertDialog.Builder(mFragment.getActivity())
                .setTitle(R.string.dialog_title_error_gps_location_retrieval)
                .setMessage(R.string.error_msg_gps_location_retrieval)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    private void sendLocationUpdate(Location location, Intent data) {
        Point point = new Point("point", new float[]{(float)location.getLongitude(), (float)location.getLatitude()});

        log.debug("Geolocation coordinates are " + point.toString());

        OutboundLocationMark locationMark = new OutboundLocationMark(
                new Date(System.currentTimeMillis()),
                point,
                Arrays.asList(URI.create(data.getData().toString())),
                data.getStringExtra(CreateNewUpdateActivity.EXTRA_RESULT_MESSAGE)
        );

        LocationMarkUploader uploader = new LocationMarkUploader(mFragment.getActivity());
        uploader.setListener(new LocationMarkUploader.UploaderListener() {
            @Override
            public void onUploadCompleted(Exception ex) {
                mProgressDialog.dismiss();
                if (mListener != null) {
                    mListener.onUploadCompleted(ex);
                }
            }
        });

        uploader.execute(locationMark);
    }

    public static class PermissionRequestDialog extends DialogFragment {
        private DialogInterface.OnClickListener mPositiveButtonListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_information)
                    .setMessage(R.string.dialog_permission_request_message)
                    .setPositiveButton(android.R.string.ok, mPositiveButtonListener)
                    .create();

        }

        public void setPositiveButton(DialogInterface.OnClickListener listener) {
            mPositiveButtonListener = listener;
        }
    }
}
