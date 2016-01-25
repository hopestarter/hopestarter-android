package org.hopestarter.ui;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.hopestarter.ui.view.CameraPreview;
import org.hopestarter.wallet_test.R;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Adrian on 25/01/2016.
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements Camera.PictureCallback {
    private static final String TAG = "CameraFragment";
    private static final int CONFIRM_REQ_CODE = 0;
    public static final String ARG_TITLE = "ARG_TITLE";
    private boolean mSurfaceAvailable;
    private boolean mFrontCameraSelected;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private CameraPreview mCameraPreview;
    private boolean mFlashAvailable;
    private List<String> mFlashModesSupported;
    private int mCurrentFlashMode;
    private ViewGroup mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void openCamera() {
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mFrontCameraSelected ||
                    info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && !mFrontCameraSelected) {
                mCamera = Camera.open(i);
                Log.d(TAG, "Camera opened");
                setCameraDisplayOrientation(i, mCamera);
                Camera.Parameters params = mCamera.getParameters();

                Log.d(TAG, ":::Focus modes available:::");
                mFlashModesSupported = params.getSupportedFlashModes();

                if (mFlashModesSupported != null && mFlashModesSupported.size() > 0) {
                    setFlashMode();
                }

                setBestFocusMode();

                break;
            }
        }
    }

    private void setBestFocusMode() {
        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(params);
    }

    private void setFlashMode() {
        if (mFlashModesSupported.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
            changeFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        } else if (mFlashModesSupported.contains(Camera.Parameters.FLASH_MODE_ON)) {
            changeFlashMode(Camera.Parameters.FLASH_MODE_ON);
        } else {
            changeFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
    }

    private void changeFlashMode(String flashMode) {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(flashMode);
        Toast.makeText(getActivity(), "Flash set to " + flashMode, Toast.LENGTH_LONG).show();
        mCamera.setParameters(params);
    }

    private void switchFlashMode() {
        if (mFlashModesSupported == null || mFlashModesSupported.size() == 0) {
            return;
        }

        mCurrentFlashMode++;
        if (mCurrentFlashMode > mFlashModesSupported.size() - 1) mCurrentFlashMode = 0;
        changeFlashMode(mFlashModesSupported.get(mCurrentFlashMode));
    }

    public void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.camera_fragment, viewGroup, false);
        mCameraPreview = (CameraPreview)mRootView.findViewById(R.id.camera_preview);
        return mRootView;
    }

    @Override
    public void onResume() {
        openCamera();

        if (mCamera != null) {
            mCameraPreview.setCamera(mCamera);

            ImageButton flashBtn = (ImageButton)mRootView.findViewById(R.id.flash_btn);
            flashBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchFlashMode();
                }
            });

            ImageButton flipCameraBtn = (ImageButton)mRootView.findViewById(R.id.flip_btn);
            flipCameraBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFrontCameraSelected = !mFrontCameraSelected;
                    mCamera.stopPreview();
                    mCamera.release();
                    openCamera();
                    mCameraPreview.setCamera(mCamera);
                }
            });

            mCameraPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCamera.takePicture(null, null, CameraFragment.this);
                }
            });
            Log.d(TAG, "Camera preview camera instance set");
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        Log.d(TAG, "Camera released");
        super.onPause();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            String pictureFileName = Long.toString(System.currentTimeMillis()) + "-profile.jpg";
            FileOutputStream fos = getActivity().openFileOutput(pictureFileName, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();
            Intent activityIntent = new Intent(getActivity(), ConfirmPictureActivity.class);
            activityIntent.putExtra(ConfirmPictureActivity.EXTRA_FILENAME, pictureFileName);
            activityIntent.putExtra(ConfirmPictureActivity.EXTRA_TITLE, getArguments().getString(ARG_TITLE));
            startActivityForResult(activityIntent, CONFIRM_REQ_CODE);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot create profile pic file", e);
        } catch (IOException e) {
            Log.e(TAG, "Cannot write profile pic file", e);
        }
    }
}
