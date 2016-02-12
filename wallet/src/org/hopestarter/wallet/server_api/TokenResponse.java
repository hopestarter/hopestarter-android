package org.hopestarter.wallet.server_api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 11/02/2016.
 */
public class TokenResponse {
    @SerializedName("access_token")
    private String mAccessToken;

    @SerializedName("expires_in")
    private int mExpiresIn;

    @SerializedName("refresh_token")
    private String mRefreshToken;

    @SerializedName("scope")
    private String mScope;

    @SerializedName("token_type")
    private String mTokenType;

    public String getAccessToken() {
        return mAccessToken;
    }

    public int getExpiresIn() {
        return mExpiresIn;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getScope() {
        return mScope;
    }

    public String getTokenType() {
        return mTokenType;
    }
}
