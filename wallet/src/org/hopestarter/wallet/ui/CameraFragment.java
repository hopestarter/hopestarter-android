package org.hopestarter.wallet.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.hopestarter.wallet.ui.view.CameraPreview;
import org.hopestarter.wallet.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Adrian on 25/01/2016.
 */
@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements Camera.PictureCallback {
    public interface CameraFragmentCallback {
        void onPictureTaken(Uri pictureUri);
    }

    private static final Logger log = LoggerFactory.getLogger(CameraFragment.class);
    private static final String TAG = "CameraFragment";
    private boolean mFrontCameraSelected;
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private CameraPreview mCameraPreview;
    private List<String> mFlashModesSupported;
    private int mCurrentFlashMode;
    private ViewGroup mRootView;
    private CameraFragmentCallback mCallback;
    private OrientationEventListener mOrientationListener;
    private int mCurrentOrientation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(getActivity() instanceof CameraFragmentCallback)) {
            throw new RuntimeException("Host activity must implement CameraFragmentCallback interface");
        }

        mCallback = (CameraFragmentCallback)getActivity();

        mOrientationListener = new OrientationEventListener(getActivity()) {

            @Override
            public void onOrientationChanged(int orientation) {
                mCurrentOrientation = normalize(orientation);
            }

            private int normalize(int degrees) {
                if (degrees > 315 || degrees <= 45) {
                    return 0;
                }

                if (degrees > 45 && degrees <= 135) {
                    return 90;
                }

                if (degrees > 135 && degrees <= 225) {
                    return 180;
                }

                if (degrees > 225 && degrees <= 315) {
                    return 270;
                }

                throw new RuntimeException("Expected a value between 0 and 359");
            }

        };

    }

    private void openCamera() {
        int numCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && mFrontCameraSelected ||
                    info.facing == Camera.CameraInfo.CAMERA_FACING_BACK && !mFrontCameraSelected) {
                mCamera = Camera.open(i);
                mCameraInfo = info;
                log.debug("Camera opened");
                setCameraDisplayOrientation(mCameraInfo, mCamera);
                Camera.Parameters params = mCamera.getParameters();

                log.debug(":::Focus modes available:::");
                mFlashModesSupported = params.getSupportedFlashModes();

                if (mFlashModesSupported != null && mFlashModesSupported.size() > 0) {
                    setFlashMode();
                }

                setBestFocusMode();
                setBestResolution();
                break;
            }
        }
    }

    private void setBestResolution() {
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPictureSizes();

        int maxPixels = 0;
        int settingIndex = 0;
        int biggestSizeIndex = 0;
        for (Camera.Size size : sizes) {
            int pixels = size.width * size.height;
            if (maxPixels < pixels) {
                maxPixels = pixels;
                biggestSizeIndex = settingIndex;
            }
            settingIndex++;
        }

        Camera.Size biggestSize = sizes.get(biggestSizeIndex);
        params.setPictureSize(biggestSize.width, biggestSize.height);
        mCamera.setParameters(params);
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

    public void setCameraDisplayOrientation(Camera.CameraInfo info, android.hardware.Camera camera) {
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


    public void setCameraRotation() {
        int result;
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = (mCameraInfo.orientation + mCurrentOrientation) % 360;
            // result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCameraInfo.orientation - mCurrentOrientation + 360) % 360;
        }
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(result);
        mCamera.setParameters(params);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.camera_fragment, viewGroup, false);
        mCameraPreview = (CameraPreview)mRootView.findViewById(R.id.camera_preview);
        return mRootView;
    }

    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        } else {
            initCamera();
        }
        super.onResume();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        int permissionStatus = getActivity().checkSelfPermission(Manifest.permission.CAMERA);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    public void initCamera() {
        openCamera();

        if (mCamera != null) {
            onCameraOpen();
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        if (grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }
    }

    public void onCameraOpen() {
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
                mCamera = null;
                openCamera();
                mCameraPreview.setCamera(mCamera);
            }
        });

        mCameraPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCameraRotation();
                mCamera.takePicture(null, null, CameraFragment.this);
            }
        });
        log.debug("Camera preview camera instance set");

        mOrientationListener.enable();
    }

    @Override
    public void onPause() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        mOrientationListener.disable();
        log.debug("Camera released");
        super.onPause();
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            String pictureFileName = "temppicture.jpg";
            FileOutputStream fos = getActivity().openFileOutput(pictureFileName, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();

            File pictureFile = new File(getActivity().getFilesDir(), pictureFileName);
            log.debug("Picture saved at: " + pictureFile.toString());

            Uri pictureUri = Uri.fromFile(pictureFile);

            if (mCallback != null) {
                mCallback.onPictureTaken(pictureUri);
            }
        } catch (FileNotFoundException e) {
            log.error("Cannot create pic file", e);
        } catch (IOException e) {
            log.error("Cannot write pic file", e);
        }
    }

}
