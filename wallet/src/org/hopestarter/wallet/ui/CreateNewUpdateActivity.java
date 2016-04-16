package org.hopestarter.wallet.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.hopestarter.wallet.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewUpdateActivity extends AppCompatActivity {
    private static final Logger log = LoggerFactory.getLogger(CreateNewUpdateActivity.class);
    private static final int PICTURE_REQ_CODE = 0;
    private static final String TAG = CreateNewUpdateActivity.class.getName();
    public static final String EXTRA_RESULT_MESSAGE = "EXTRA_RESULT_MESSAGE";
    private ImageView mImageView;
    private EditText mMessageView;
    private Uri mImageUri;
    private RequestListener<? super Uri, GlideDrawable> mImageLoaderListener = new RequestListener<Uri, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target,
                boolean isFirstResource) {
            log.error("Failed loading picture at " + model.toString(), e);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Uri model,
                Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_update);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_create_new_update);
        setSupportActionBar(toolbar);

        mImageView = (ImageView)findViewById(R.id.imageview);
        mMessageView = (EditText)findViewById(R.id.message);

        launchPictureSelectActivity();
    }

    public void launchPictureSelectActivity() {
        Intent activityIntent = new Intent(this, PictureSelectActivity.class);
        activityIntent.putExtra(PictureSelectActivity.EXTRA_TITLE, getString(R.string.post_a_photo_update_title));
        startActivityForResult(activityIntent, PICTURE_REQ_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                launchPictureSelectActivity();
                return true;
            case R.id.post:
                prepareResult();
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        launchPictureSelectActivity();
    }

    private void prepareResult() {
        String message = mMessageView.getText().toString();
        Intent data = new Intent();
        data.setData(mImageUri);
        data.putExtra(EXTRA_RESULT_MESSAGE, message);
        setResult(RESULT_OK, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_new_update_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICTURE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                setResult(RESULT_CANCELED);
                finish();
            } else {
                mImageUri = data.getData();
                Glide.with(this)
                        .load(data.getData())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .listener(mImageLoaderListener)
                        .into(mImageView);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
