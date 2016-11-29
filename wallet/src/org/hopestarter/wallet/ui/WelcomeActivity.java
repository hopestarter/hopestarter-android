package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.getsentry.raven.android.Raven;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet.data.AppConfig;
import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.server_api.AuthenticationFailed;
import org.hopestarter.wallet.server_api.ForbiddenResourceException;
import org.hopestarter.wallet.server_api.NoTokenException;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.UnexpectedServerResponseException;
import org.hopestarter.wallet.server_api.UserInfo;
import org.hopestarter.wallet.util.FileUtils;
import org.hopestarter.wallet.util.StreamDuplicationFailedException;
import org.hopestarter.wallet.util.StreamDuplicator;
import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WelcomeActivity extends Activity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final int ACCOUNT_CREATION_REQ_CODE = 0;
    private static final Logger log = LoggerFactory.getLogger(WelcomeActivity.class);
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        if (initRaven()){
            mBtn = (Button)findViewById(R.id.create_account_btn);
            mBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(WelcomeActivity.this, CreateAccountActivity.class), ACCOUNT_CREATION_REQ_CODE);
                }
            });

            if (accountExists()) {
                updateUserInfo();
            }
        } else {
            Toast.makeText(this, "A problem was detected. Please stay tuned for app updates.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean initRaven() {
        try {
            AppConfig appConfig = AppConfig.getAppConfig(this);
            Raven.init(this.getApplicationContext(), appConfig.getSentryClientKey());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't read raven/sentry client key", e);
            return false;
        }
    }

    private void updateUserInfo() {
        mBtn.setEnabled(false);

        final ProgressDialog progressDialog = ProgressDialog.show(
                this, getString(R.string.progress_dialog_title_connecting), getString(R.string.please_wait_message), true, false
        );

        final Context context = this;

        AsyncTask<Void, Void, Exception> updateUserInfo = new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                ServerApi serverApi = ((WalletApplication)getApplication()).getServerApi();
                try {
                    UserInfo info = serverApi.getUserInfo();

                    SharedPreferences prefs = context.getSharedPreferences(
                            UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE
                    );

                    String pictureUri = info.getPictureResources().getMedium();
                    String localPictureUri = null;
                    if (pictureUri != null && !pictureUri.isEmpty()) {
                        localPictureUri = downloadProfilePicture(info.getPictureResources().getMedium());
                    }

                    prefs.edit()
                            .putString(UserInfoPrefs.FIRST_NAME, info.getFirstName())
                            .putString(UserInfoPrefs.LAST_NAME, info.getLastName())
                            .putString(UserInfoPrefs.PROFILE_PIC, localPictureUri)
                            .commit();

                    return null;
                } catch (NoTokenException e) {
                    log.error("No token was stored previously to a call to getUserInfo", e);
                    return e;
                } catch (IOException e) {
                    log.error("Network error", e);
                    return e;
                } catch (ForbiddenResourceException e) {
                    log.error("Forbidden", e);
                    return e;
                } catch (UnexpectedServerResponseException e) {
                    log.error("Unexpected server response", e);
                    return e;
                } catch (AuthenticationFailed e) {
                    log.error("Authentication failed", e);
                    return e;
                }
            }

            private String downloadProfilePicture(String uri) {
                try {
                    OkHttpClient httpClient = new OkHttpClient();

                    Request pictureRequest = new Request.Builder()
                            .get()
                            .url(uri)
                            .build();

                    Call call =  httpClient.newCall(pictureRequest);
                    Response response = call.execute();

                    if (response.isSuccessful()) {
                        return saveProfilePicture(response.body());
                    } else if (response.body() != null) {
                        response.body().string();
                    }

                    return null;
                } catch (IOException e) {
                    log.error("Couldn't download profile picture due to connection error");
                    return null;
                }
            }

            private String saveProfilePicture(ResponseBody body) {
                InputStream is = body.byteStream();
                File outputFile = new File(getFilesDir(), "profile");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outputFile);
                    StreamDuplicator duplicator = new StreamDuplicator();
                    duplicator.duplicate(is, fos);
                    return outputFile.getPath();
                } catch (FileNotFoundException e) {
                    log.error("Couldn't open picture file for writing", e);
                    return null;
                } catch (StreamDuplicationFailedException e) {
                    log.error("Couldn't download profile picture", e);
                    return null;
                } finally {
                    FileUtils.closeSilently(fos);
                    FileUtils.closeSilently(is);
                }
            }

            @Override
            protected void onPostExecute(Exception error) {
                progressDialog.dismiss();
                mBtn.setEnabled(true);

                if (error == null) {
                    startMainActivity();
                    finish();
                } else {
                    String errorMessage;
                    if (error instanceof IOException) {
                        errorMessage = getString(R.string.error_connection_problem);
                    } else if (error instanceof ForbiddenResourceException) {
                        errorMessage = getString(R.string.error_authentication);
                    } else {
                        errorMessage = getString(R.string.error_unexpected);
                    }

                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.error_default_dialog_title)
                            .setMessage(errorMessage)
                            .setIcon(context.getResources().getDrawable(R.drawable.ic_error_24dp))
                            .create();

                    alertDialog.show();
                }
            }
        };
        updateUserInfo.execute();
    }

    private boolean accountExists() {
        SharedPreferences prefs = getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        String token = prefs.getString(UserInfoPrefs.TOKEN, null);
        return (token != null);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case ACCOUNT_CREATION_REQ_CODE:
                if (resCode == RESULT_OK) {
                    if (accountExists()) {
                        updateUserInfo();
                    }
                }
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainTabbedActivity.class));
    }

}
