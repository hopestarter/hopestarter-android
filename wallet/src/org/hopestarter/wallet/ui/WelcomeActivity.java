package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.server_api.AuthenticationFailed;
import org.hopestarter.wallet.server_api.ForbiddenResourceException;
import org.hopestarter.wallet.server_api.NoTokenException;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.UnexpectedServerResponseException;
import org.hopestarter.wallet.server_api.UserInfo;
import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WelcomeActivity extends Activity {

    private static final int ACCOUNT_CREATION_REQ_CODE = 0;
    private static final Logger log = LoggerFactory.getLogger(WelcomeActivity.class);
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
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
    }

    private void updateUserInfo() {
        mBtn.setEnabled(false);

        final ProgressDialog progressDialog = ProgressDialog.show(
                this, "Connecting ...", getString(R.string.please_wait_message), true, false
        );

        final Context context = this;

        AsyncTask<Void, Void, Exception> updateUserInfo = new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                ServerApi serverApi = new ServerApi(context);
                try {
                    UserInfo info = serverApi.getUserInfo();

                    SharedPreferences prefs = context.getSharedPreferences(
                            UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE
                    );

                    prefs.edit()
                            .putString(UserInfoPrefs.FIRST_NAME, info.getFirstName())
                            .putString(UserInfoPrefs.LAST_NAME, info.getLastName())
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
                    startMainActivity();
                    finish();
                }
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainTabbedActivity.class));
    }

}
