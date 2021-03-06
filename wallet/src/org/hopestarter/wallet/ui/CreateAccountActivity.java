package org.hopestarter.wallet.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.bitcoinj.core.Address;
import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.server_api.AuthenticationFailed;
import org.hopestarter.wallet.server_api.ForbiddenResourceException;
import org.hopestarter.wallet.server_api.NoTokenException;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.StagingApi;
import org.hopestarter.wallet.server_api.TokenResponse;
import org.hopestarter.wallet.server_api.UnexpectedServerResponseException;
import org.hopestarter.wallet.server_api.UserInfo;
import org.hopestarter.wallet.util.ResourceUtils;
import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CreateAccountActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {

    private static final Logger log = LoggerFactory.getLogger(CreateAccountActivity.class);
    private static final int PROFILE_PIC_REQ_CODE = 0;
    private static final String TAG = "CreateAccountAct";
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 0;

    private ImageView mImageView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEthnicityView;
    private Uri mProfilePicture;

    private RequestListener<? super Uri, GlideDrawable> mImageLoaderListener = new RequestListener<Uri, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
            log.error("Failed loading image at " + model.toString());
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Toolbar tb = (Toolbar)findViewById(R.id.toolbar);
        tb.setTitle(R.string.activity_title_create_account);
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

        mProfilePicture = ResourceUtils.resIdToUri(this, R.drawable.avatar_placeholder);

        View.OnClickListener addPicClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent activityIntent = new Intent(CreateAccountActivity.this, PictureSelectActivity.class);
                activityIntent.putExtra(PictureSelectActivity.EXTRA_TITLE, getString(R.string.activity_title_add_profile_picture));
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
                safeCreateAccount();
        }
        return super.onOptionsItemSelected(item);
    }

    private void safeCreateAccount() {
        if (appHasPermissions()) {
            createAccount();
        } else {
            askForPermissions();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createAccount();
                } else {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_title_error)
                            .setIcon(R.drawable.ic_error_24dp)
                            .setMessage(R.string.error_msg_read_phone_state_permission_missing)
                            .create();
                    dialog.show();
                }
        }
    }

    private void askForPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                PERMISSION_REQUEST_READ_PHONE_STATE);
    }

    private boolean appHasPermissions() {
        int status = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        return (status == PackageManager.PERMISSION_GRANTED);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setProfilePicture(Uri pictureUri) {
        mProfilePicture = pictureUri;
        Glide.with(this)
                .load(pictureUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .listener(mImageLoaderListener)
                .into(mImageView);
    }

    private void createAccount() {
        final String firstName = mFirstNameView.getText().toString();
        final String lastName = mLastNameView.getText().toString();
        final String ethnicity = mEthnicityView.getText().toString();
        final String imei = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        Uri placeholderPicture = ResourceUtils.resIdToUri(this, R.drawable.avatar_placeholder);
        String pictureString = null;
        if (!placeholderPicture.equals(mProfilePicture)) {
            pictureString = mProfilePicture.toString();
        }

        final String profilePicture = pictureString;

        final Activity thisActivity = this;

        final ProgressDialog progressDialog =
                ProgressDialog.show(this, getString(R.string.creating_account_progress_dialog_title),
                        getString(R.string.please_wait_message), true, false);

        AsyncTask<String, Void, AccountCreationResult> createAccountTask = new AsyncTask<String, Void, AccountCreationResult>() {
            private String firstName;
            private String lastName;
            private String ethnicity;
            private String imei;
            private String profilePicture;

            @Override
            protected AccountCreationResult doInBackground(String... params) {
                firstName = params[0];
                lastName = params[1];
                ethnicity = params[2];
                imei = params[3];
                profilePicture = params[4];

                try {
                    StagingApi stagingApi = ((WalletApplication)getApplication()).getStagingApi();
                    ServerApi serverApi = ((WalletApplication)getApplication()).getServerApi();

                    // FIXME: Replace mock authentication before official release
                    int respCode = stagingApi.signUp(imei, "demopassword", firstName, lastName, ethnicity);

                    if (respCode == 200 || respCode == 302) {
                        TokenResponse tokenResp = serverApi.getToken(imei, "demopassword");
                        if (tokenResp != null) {
                            saveUserInformation(tokenResp.getAccessToken(), tokenResp.getRefreshToken(), null, null, null, null);
                            serverApi.updateAuthHeaderValue();

                            if (profilePicture != null && !profilePicture.isEmpty()) {
                                uploadProfilePicture(serverApi);
                            }

                            sendBitcoinAddress(serverApi);
                            return new AccountCreationResult(tokenResp);
                        } else {
                            String errorMsg = getString(R.string.account_creation_error_no_token);
                            return new AccountCreationResult(new NoTokenException(errorMsg));
                        }

                    } else {
                        String errorMsg = getString(R.string.error_unexpected_account_creation);
                        return new AccountCreationResult(new UnexpectedServerResponseException(respCode, errorMsg));
                    }

                } catch (Exception e) {
                    return new AccountCreationResult(e);
                }
            }

            private void sendBitcoinAddress(ServerApi serverApi) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException {
                Address receiveAddress = ((WalletApplication)getApplication()).getWallet().currentReceiveAddress();
                UserInfo info = new UserInfo.Builder().setBitcoinAddress(receiveAddress.toString()).create();
                serverApi.setUserInfo(info);
            }

            private void uploadProfilePicture(ServerApi serverApi) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, InterruptedException {
                serverApi.uploadProfileImage(new File(URI.create(profilePicture)));
            }

            @Override
            protected void onPostExecute(AccountCreationResult result) {
                progressDialog.dismiss();
                if (result.token != null) {
                    TokenResponse token = result.token;
                    saveUserInformation(token.getAccessToken(), token.getRefreshToken(), firstName, lastName, ethnicity, null);
                    setResult(RESULT_OK);
                    finish();

                } else {
                    if (result.error != null) {
                        String errorMsg;

                        if (result.error instanceof IOException) {
                            errorMsg = getString(R.string.error_connection_problem);
                        } else {
                            errorMsg = result.error.getMessage();
                        }

                        AlertDialog dialog = new AlertDialog.Builder(thisActivity)
                                .setTitle(getString(R.string.dialog_title_error))
                                .setMessage(errorMsg)
                                .setIcon(R.drawable.ic_error_24dp)
                                .create();
                        dialog.show();
                        log.error("Unable to create account", result.error);

                        clearUserInformation();
                    }
                }
            }
        };

        createAccountTask.execute(firstName, lastName, ethnicity, imei, profilePicture);
    }

    private void clearUserInformation() {
        saveUserInformation(null, null, null, null, null, null);
    }

    private void saveUserInformation(String token, String refreshToken, String firstName, String lastName, String ethnicity, String profilePicture) {
        SharedPreferences prefs = getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        boolean success = prefs.edit()
                .putString(UserInfoPrefs.TOKEN, token)
                .putString(UserInfoPrefs.REFRESH_TOKEN, refreshToken)
                .putString(UserInfoPrefs.FIRST_NAME, firstName)
                .putString(UserInfoPrefs.LAST_NAME, lastName)
                .putString(UserInfoPrefs.ETHNICITY, ethnicity)
                .putString(UserInfoPrefs.PROFILE_PIC, profilePicture)
                .commit();

        if (!success) {
            log.error("user information couldn't be saved");
        }
    }

    private static class AccountCreationResult {
        public AccountCreationResult(TokenResponse token) {
            this.token = token;
        }

        public AccountCreationResult(Exception error) {
            this.error = error;
        }

        public TokenResponse token;
        public Exception error;
    }
}
