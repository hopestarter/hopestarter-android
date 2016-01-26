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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hopestarter.wallet_test.R;

import java.io.FileNotFoundException;

import de.schildbach.wallet.util.BitmapFragment;

public class CreateAccountActivity extends AppCompatActivity {

    private static final int PROFILE_PIC_REQ_CODE = 0;
    private static final String TAG = "CreateAccountAct";

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Toolbar tb = (Toolbar)findViewById(R.id.toolbar);
        tb.setTitle("Create an account");
        tb.setNavigationIcon(R.drawable.close_icon);
        setSupportActionBar(tb);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mImageView = (ImageView)findViewById(R.id.profile_image_view);
        TextView addPicView = (TextView)findViewById(R.id.add_profile_picture_link);

        View.OnClickListener addPicClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(CreateAccountActivity.this, PictureSelectActivity.class);
                activityIntent.putExtra(PictureSelectActivity.EXTRA_TITLE, "Add a profile picture");
                startActivityForResult(activityIntent, PROFILE_PIC_REQ_CODE);
            }
        };

        mImageView.setOnClickListener(addPicClickListener);
        addPicView.setOnClickListener(addPicClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.done:
                setResult(RESULT_OK);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_account_menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case PROFILE_PIC_REQ_CODE:
                if (resCode == RESULT_OK) {
                    setProfilePicture(data.getData());
                }
                break;
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }

    private void setProfilePicture(Uri pictureUri) {
        Picasso.with(this).load(pictureUri).resize(94, 94).centerCrop().into(mImageView);
    }
}
