package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Adrian on 25/08/2016.
 */
public class Results implements Parcelable {
    @SerializedName("type")
    private String mType;

    @SerializedName("features")
    private ArrayList<LocationMark> mLocationMarks;

    public String getType() {
        return mType;
    }

    public ArrayList<LocationMark> getLocationMarks() {
        return mLocationMarks;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mType);
        dest.writeTypedList(this.mLocationMarks);
    }

    public Results() {
    }

    protected Results(Parcel in) {
        this.mType = in.readString();
        this.mLocationMarks = in.createTypedArrayList(LocationMark.CREATOR);
    }

    public static final Parcelable.Creator<Results> CREATOR = new Parcelable.Creator<Results>() {
        @Override
        public Results createFromParcel(Parcel source) {
            return new Results(source);
        }

        @Override
        public Results[] newArray(int size) {
            return new Results[size];
        }
    };
}
