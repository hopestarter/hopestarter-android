package org.hopestarter.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TabHost;

import org.hopestarter.wallet_test.R;

public class PictureSelectActivity extends AppCompatActivity implements CameraFragment.CameraFragmentCallback {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String GALLERY_FRAGMENT_SPEC = "Gallery";
    public static final String TAKE_PHOTO_SPEC = "Take Photo";
    private static final int CONFIRMATION_REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_select);

        Toolbar tb = (Toolbar)findViewById(R.id.toolbar);

        String title;
        Intent intent = getIntent();
        if (savedInstanceState == null) {
            title = intent.getStringExtra(EXTRA_TITLE);
        } else {
            title = savedInstanceState.getString(EXTRA_TITLE);
        }

        if (title == null) {
            title = "Select a picture";
        }

        tb.setTitle(title);

        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);


        FragmentTabHost tabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.content_layout);
        tabHost.addTab(tabHost.newTabSpec(GALLERY_FRAGMENT_SPEC).setIndicator("Gallery"), GalleryFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec(TAKE_PHOTO_SPEC).setIndicator("Take a Photo"), CameraFragment.class, null);

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
        askUserConfirmation(pictureUri);
    }

    private void askUserConfirmation(Uri pictureUri) {
        Intent activityIntent = new Intent(this, ConfirmPictureActivity.class);
        activityIntent.putExtra(ConfirmPictureActivity.EXTRA_TITLE, getTitle());
        activityIntent.setData(pictureUri);

        startActivityForResult(activityIntent, CONFIRMATION_REQ_CODE);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case CONFIRMATION_REQ_CODE:
                if (resCode == RESULT_OK) {
                    setResult(resCode, data);
                    finish();
                }
                break;
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }
}
