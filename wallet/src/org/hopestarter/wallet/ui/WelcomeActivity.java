package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet_test.R;

public class WelcomeActivity extends Activity {

    private static final int ACCOUNT_CREATION_REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button btn = (Button)findViewById(R.id.create_account_btn);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(WelcomeActivity.this, CreateAccountActivity.class), ACCOUNT_CREATION_REQ_CODE);
            }
        });

        if (accountExists()) {
            startMainActivity();
            finish();
        }
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
