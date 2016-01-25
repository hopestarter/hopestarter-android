package org.hopestarter.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import org.hopestarter.wallet_test.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ConfirmPictureActivity extends AppCompatActivity {

    public static final String EXTRA_FILENAME = "EXTRA_FILENAME";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String TAG = "ConfirmPictureAct";
    private String mTitle;
    private String mFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_picture);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra(EXTRA_TITLE);
            mFilename = intent.getStringExtra(EXTRA_FILENAME);
        } else {
            mFilename = savedInstanceState.getString(EXTRA_FILENAME);
            mTitle = savedInstanceState.getString(EXTRA_TITLE);
        }

        if (mTitle == null) {
            mTitle = "Confirm picture";
        }

        Toolbar topToolbar = (Toolbar)findViewById(R.id.toolbar);
        topToolbar.setTitle(mTitle);
        setSupportActionBar(topToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Toolbar bottomToolbar = (Toolbar)findViewById(R.id.toolbar_bottom);
        bottomToolbar.inflateMenu(R.menu.bottom_menu_confirm_picture);

        ImageView picturePreview = (ImageView)findViewById(R.id.picture_preview);
        FileInputStream fis;
        Bitmap pictureBitmap = null;
        try {
            fis = openFileInput(mFilename);
            pictureBitmap = BitmapFactory.decodeFileDescriptor(fis.getFD());
            fis.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot open picture file", e);
        } catch (IOException e) {
            Log.e(TAG, "Cannot read picture file", e);
        }

        picturePreview.setImageBitmap(pictureBitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
