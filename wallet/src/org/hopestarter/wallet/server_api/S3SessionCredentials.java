package org.hopestarter.wallet.server_api;

import com.amazonaws.auth.AWSSessionCredentials;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 23/02/2016.
 */

public class S3SessionCredentials implements AWSSessionCredentials {
    @SerializedName("SecretAccessKey")
    private String mSecretAccessKey;

    @SerializedName("SessionToken")
    private String mSessionToken;

    @SerializedName("AccessKeyId")
    private String mAccessKeyId;

    @Override
    public String getSessionToken() {
        return mSessionToken;
    }

    @Override
    public String getAWSAccessKeyId() {
        return mAccessKeyId;
    }

    @Override
    public String getAWSSecretKey() {
        return mSecretAccessKey;
    }
}
