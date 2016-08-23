package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Geometry implements Parcelable {
    @SerializedName("type")
    private String mType;

    @SerializedName("coordinates")
    private double[] mCoordinates;

    public String getType() {
        return mType;
    }

    public double[] getCoordinates() {
        return mCoordinates;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mType);
        dest.writeDoubleArray(this.mCoordinates);
    }

    public Geometry() {
    }

    protected Geometry(Parcel in) {
        this.mType = in.readString();
        this.mCoordinates = in.createDoubleArray();
    }

    public static final Parcelable.Creator<Geometry> CREATOR = new Parcelable.Creator<Geometry>() {
        @Override
        public Geometry createFromParcel(Parcel source) {
            return new Geometry(source);
        }

        @Override
        public Geometry[] newArray(int size) {
            return new Geometry[size];
        }
    };
}
