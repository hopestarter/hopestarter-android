package org.hopestarter.wallet.data;

import android.app.Activity;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Adrian on 29/11/2016.
 */
public class AppConfig {
    @SerializedName("sentry_client_key")
    private String mSentryClientKey;

    public String getSentryClientKey() {
        return mSentryClientKey;
    }

    public static AppConfig getAppConfig(Activity activity) throws IOException {
        AppConfig appConfig;
        InputStream is = activity.getAssets().open("config.json");
        String json = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        Gson gson = new Gson();
        appConfig = gson.fromJson(json, AppConfig.class);
        is.close();
        return appConfig;
    }
}
