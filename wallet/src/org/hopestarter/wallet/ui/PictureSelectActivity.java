package org.hopestarter.wallet.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.hopestarter.wallet.R;

public class PictureSelectActivity extends AppCompatActivity implements CameraFragment.CameraFragmentCallback, GalleryFragment.Callback {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String GALLERY_FRAGMENT_SPEC = "Gallery";
    public static final String TAKE_PHOTO_SPEC = "Take Photo";
    private static final int FROM_CAMERA_CONFIRMATION_REQ_CODE = 0;
    private static final int FROM_GALLERY_CONFIRMATION_REQ_CODE = 1;

    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_select);

        Toolbar tb = (Toolbar)findViewById(R.id.toolbar);

        Intent intent = getIntent();
        if (savedInstanceState == null) {
            mTitle = intent.getStringExtra(EXTRA_TITLE);
        } else {
            mTitle = savedInstanceState.getString(EXTRA_TITLE);
        }

        if (mTitle == null) {
            mTitle = "Select a picture";
        }

        tb.setTitle(mTitle);

        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        setupTabs();
    }

    private void setupTabs() {
        FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.content_layout);
        tabHost.addTab(tabHost.newTabSpec(GALLERY_FRAGMENT_SPEC).setIndicator("Gallery"), GalleryFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAKE_PHOTO_SPEC).setIndicator("Take a Photo"), CameraFragment.class, null);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putString(EXTRA_TITLE, mTitle);
        super.onSaveInstanceState(out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureTaken(Uri pictureUri) {
        askUserConfirmation(pictureUri, true);
    }

    @Override
    public void onImageSelected(Uri imageUri) {
        askUserConfirmation(imageUri, false);
    }

    private void askUserConfirmation(Uri pictureUri, boolean fromCamera) {
        Intent activityIntent = new Intent(this, ConfirmPictureActivity.class);
        activityIntent.putExtra(ConfirmPictureActivity.EXTRA_TITLE, mTitle);
        activityIntent.setData(pictureUri);
        if (fromCamera) {
            startActivityForResult(activityIntent, FROM_CAMERA_CONFIRMATION_REQ_CODE);
        } else {
            startActivityForResult(activityIntent, FROM_GALLERY_CONFIRMATION_REQ_CODE);
        }

    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case FROM_CAMERA_CONFIRMATION_REQ_CODE:
                if (resCode == RESULT_OK) {
                    setResult(resCode, data);
                    finish();
                } else {
                    deleteFile(data.getData().getLastPathSegment());
                }
                break;
            case FROM_GALLERY_CONFIRMATION_REQ_CODE:
                if (resCode == RESULT_OK) {
                    setResult(resCode, data);
                    finish();
                }
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }


}
