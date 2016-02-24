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

    @SerializedName("picture")
    private String mPicture;

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public String getPictureUri() {
        return mPicture;
    }

    public UserInfo(String firstName, String lastName, String pictureUri) {
        mFirstName = firstName;
        mLastName = lastName;
        mPicture = pictureUri;
    }
}
