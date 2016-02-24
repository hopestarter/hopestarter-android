package org.hopestarter.wallet.server_api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 23/02/2016.
 */

public class UploadImageResponse {
    @SerializedName("credentials")
    private S3SessionCredentials mCredentials;

    @SerializedName("bucket")
    private BucketInfo mBucket;

    public S3SessionCredentials getCredentials() {
        return mCredentials;
    }

    public BucketInfo getBucket() {
        return mBucket;
    }
}
