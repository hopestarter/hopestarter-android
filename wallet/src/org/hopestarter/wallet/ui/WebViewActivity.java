package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import org.hopestarter.wallet.R;

/**
 * Created by Adrian on 29/11/2016.
 */
public class WebViewActivity extends AppCompatActivity {
    public static final String EXTRA_WEBVIEW_TITLE = "EXTRA_WEBVIEW_TITLE";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.close_icon);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Intent callingIntent = getIntent();
        String webViewTitle = callingIntent.getStringExtra(EXTRA_WEBVIEW_TITLE);

        if (webViewTitle == null) {
            webViewTitle = "";
        }

        ab.setTitle(webViewTitle);

        Uri uri = callingIntent.getData();
        WebView wv = (WebView) findViewById(R.id.webview);
        wv.loadUrl(uri.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
