package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hopestarter.wallet_test.R;

public class ConfirmPictureActivity extends AppCompatActivity {

    public static final String STATE_IMG_URI = "STATE_IMG_URI";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String TAG = "ConfirmPictureAct";
    private Uri mFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_picture);

        String mTitle;
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
        if (ab == null) {
            throw new IllegalStateException("getSupportActionBar() unexpectedly returned null");
        }

        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        ImageView mPicturePreview = (ImageView) findViewById(R.id.picture_preview);

        Picasso.with(this)
                .load(mFileUri)
                .resize(320, 240)
                .centerCrop()
                .into(mPicturePreview);

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

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.setData(mFileUri);
        setResult(RESULT_CANCELED, data);
        finish();
    }
}
