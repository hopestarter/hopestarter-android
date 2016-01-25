package org.hopestarter.ui;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.hopestarter.wallet_test.R;

public class PictureSelectActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";

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
        tabHost.addTab(tabHost.newTabSpec("Gallery").setIndicator("Gallery"), GalleryFragment.class, null);
        Bundle cameraArgs = new Bundle();
        cameraArgs.putString(CameraFragment.ARG_TITLE, title);
        tabHost.addTab(tabHost.newTabSpec("Take Photo").setIndicator("Take a Photo"), CameraFragment.class, cameraArgs);

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
}
