package org.hopestarter.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hopestarter.wallet_test.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ConfirmPictureActivity extends AppCompatActivity {

    public static final String STATE_IMG_URI = "EXTRA_IMG_URI";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String TAG = "ConfirmPictureAct";
    private String mTitle;
    private Uri mFileUri;
    private ImageView mPicturePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_picture);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra(EXTRA_TITLE);
            mFileUri = intent.getData();
        } else {
            mFileUri = savedInstanceState.getParcelable(STATE_IMG_URI);
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

        mPicturePreview = (ImageView)findViewById(R.id.picture_preview);

        Picasso.with(this)
                .load(mFileUri)
                .resize(320, 240)
                .centerCrop()
                .into(mPicturePreview);

//        InputStream fis;
//        Bitmap pictureBitmap = null;
//        try {
//            fis = getContentResolver().openInputStream(mFileUri);
//            pictureBitmap = BitmapFactory.decodeStream(fis);
//            fis.close();
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, "Cannot open picture file", e);
//        } catch (IOException e) {
//            Log.e(TAG, "Cannot read picture file", e);
//        }
//
//        mPicturePreview.setImageBitmap(pictureBitmap);

        final Activity activity = this;

        TextView retakeOption = (TextView)findViewById(R.id.retake_btn);
        retakeOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.setData(mFileUri);
                setResult(RESULT_CANCELED, data);
                finish();
            }
        });

        TextView addOption = (TextView)findViewById(R.id.add_btn);
        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.setData(mFileUri);
                setResult(RESULT_OK, data);
                finish();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.setData(mFileUri);
                setResult(RESULT_CANCELED, data);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
