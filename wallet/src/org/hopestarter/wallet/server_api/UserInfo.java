package org.hopestarter.wallet.server_api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 18/02/2016.
 */
public class UserInfo {
    @SerializedName("name")
    private String mFirstName;

    @SerializedName("surname")
    private String mLastName;

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }
}
