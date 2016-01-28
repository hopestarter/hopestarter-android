package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case ACCOUNT_CREATION_REQ_CODE:
                if (resCode == RESULT_OK) {
                    startActivity(new Intent(this, MainTabbedActivity.class));
                    finish();
                }
        }
    }
}
