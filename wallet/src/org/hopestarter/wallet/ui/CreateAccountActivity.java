package org.hopestarter.wallet.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet_test.R;

public class CreateAccountActivity extends AppCompatActivity {

    private static final int PROFILE_PIC_REQ_CODE = 0;
    private static final String TAG = "CreateAccountAct";

    private ImageView mImageView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEthnicityView;

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

        mFirstNameView = (EditText)findViewById(R.id.first_name_input);
        mLastNameView = (EditText)findViewById(R.id.last_name_input);
        mEthnicityView = (EditText)findViewById(R.id.ethnicity_input);

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
                createAccount();
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

    private void createAccount() {
        final String firstName = mFirstNameView.getText().toString();
        final String lastName = mLastNameView.getText().toString();
        final String ethnicity = mEthnicityView.getText().toString();
        final String imei = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        AsyncTask<String, Void, AccountCreationResult> createAccountTask = new AsyncTask<String, Void, AccountCreationResult>() {
            @Override
            protected AccountCreationResult doInBackground(String... params) {
                // TODO: Replace this code with one communicating with the server to create an account
                //       It also must obtain a token associated with the new account
                return new AccountCreationResult("sometoken");
            }

            @Override
            protected void onPostExecute(AccountCreationResult result) {
                if (result.token != null) {
                    saveUserInformation(result.token, firstName, lastName, ethnicity);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        };

        createAccountTask.execute(firstName, lastName, ethnicity, imei);
    }

    private void saveUserInformation(String token, String firstName, String lastName, String ethnicity) {
        SharedPreferences prefs = getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(UserInfoPrefs.TOKEN, token)
                .putString(UserInfoPrefs.FIRST_NAME, firstName)
                .putString(UserInfoPrefs.LAST_NAME, lastName)
                .putString(UserInfoPrefs.ETHNICITY, ethnicity)
                .commit();

    }

    private class AccountCreationResult {
        public AccountCreationResult(String token) {
            this.token = token;
        }

        public AccountCreationResult(Exception error) {
            this.error = error;
        }

        public String token;
        public Exception error;
    }
}
