package org.hopestarter.wallet.server_api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 23/02/2016.
 */
public class BucketInfo {
    @SerializedName("region")
    private String mRegion;

    @SerializedName("prefix")
    private String mPrefix;

    @SerializedName("name")
    private String mName;

    public String getName() {
        return mName;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public String getRegion() {
        return mRegion;
    }
}
