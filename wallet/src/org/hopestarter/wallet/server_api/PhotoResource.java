package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 15/08/2016.
 */
public class PhotoResource implements Parcelable {
    @SerializedName("large")
    private String mLarge;

    @SerializedName("medium")
    private String mMedium;

    @SerializedName("small")
    private String mSmall;

    @SerializedName("thumbnail")
    private String mThumbnail;

    public String getLarge() {
        return mLarge;
    }

    public String getMedium() {
        return mMedium;
    }

    public String getSmall() {
        return mSmall;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mLarge);
        dest.writeString(this.mMedium);
        dest.writeString(this.mSmall);
        dest.writeString(this.mThumbnail);
    }

    public PhotoResource() {
    }

    protected PhotoResource(Parcel in) {
        this.mLarge = in.readString();
        this.mMedium = in.readString();
        this.mSmall = in.readString();
        this.mThumbnail = in.readString();
    }

    public static final Parcelable.Creator<PhotoResource> CREATOR = new Parcelable.Creator<PhotoResource>() {
        @Override
        public PhotoResource createFromParcel(Parcel source) {
            return new PhotoResource(source);
        }

        @Override
        public PhotoResource[] newArray(int size) {
            return new PhotoResource[size];
        }
    };
}
