package org.hopestarter.wallet.server_api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 14/04/2016.
 */

public class Point {
    @SerializedName("type")
    private String mType;
    @SerializedName("coordinates")
    private float[] mCoordinates;

    public Point(String type, float[] coordinates) {
        mType = type;
        mCoordinates = coordinates;
    }

    public String getType() {
        return mType;
    }

    public float[] getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        return "Point {" + mCoordinates.toString() + "}";
    }
}
