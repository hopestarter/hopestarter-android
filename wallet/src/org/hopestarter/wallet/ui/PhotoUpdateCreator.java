package org.hopestarter.wallet.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.hopestarter.wallet.server_api.LocationMarkUploader;

import org.hopestarter.wallet.server_api.OutboundLocationMark;
import org.hopestarter.wallet.server_api.Point;
import org.hopestarter.wallet_test.R;
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
    private final ProgressDialog mProgressDialog;
    private Fragment mFragment;
    private int mReqCode;
    private int mPermissionReqCode;
    private LocationMarkUploader.UploaderListener mListener;
    private GoogleApiClient mGoogleApiClient;

    public PhotoUpdateCreator(Fragment fragment, GoogleApiClient googleApiClient, int dataReqCode, int permissionReqCode) {
        mFragment = fragment;
        mReqCode = dataReqCode;
        mPermissionReqCode = permissionReqCode;
        mGoogleApiClient = googleApiClient;

        mProgressDialog = new ProgressDialog(fragment.getActivity());
        mProgressDialog.setTitle("Uploading update");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    public void setUploadListener(LocationMarkUploader.UploaderListener listener) {
        mListener = listener;
    }

    public void create() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermissions();
        } else {
            launchCreateNewUpdateActivity();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkLocationPermissions() {
        int locationPermission = mFragment.getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            launchCreateNewUpdateActivity();
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

    private void launchCreateNewUpdateActivity() {
        Intent activityIntent = new Intent(mFragment.getActivity(), CreateNewUpdateActivity.class);
        mFragment.startActivityForResult(activityIntent, mReqCode);
    }

    public void onRequestPermissionsResult(String[] permissions, int[] grantResult) {
        if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            launchCreateNewUpdateActivity();
        }
    }

    public void onActivityResult(final Intent data) {
        try {
            mProgressDialog.show();

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            Point point = new Point("point", new float[]{(float)location.getLatitude(), (float)location.getLongitude()});

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
        } catch(SecurityException e) {
            // Permissions were changed while uploading location
        }

    }

    public static class PermissionRequestDialog extends DialogFragment {
        private DialogInterface.OnClickListener mPositiveButtonListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Information")
                    .setMessage("Please allow Hopestarter to access your location for it to be associated with your status update")
                    .setPositiveButton(android.R.string.ok, mPositiveButtonListener)
                    .create();

        }

        public void setPositiveButton(DialogInterface.OnClickListener listener) {
            mPositiveButtonListener = listener;
        }
    }
}
