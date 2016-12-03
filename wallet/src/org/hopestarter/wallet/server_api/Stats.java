package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 03/12/2016.
 */
public class Stats implements Parcelable {
    @SerializedName("created")
    private String mCreationDate;

    @SerializedName("modified")
    private String mModificationDate;

    @SerializedName("post_count")
    private String mPostCount;

    public String getCreationDate() {
        return mCreationDate;
    }

    public String getModificationDate() {
        return mModificationDate;
    }

    public String getPostCount() {
        return mPostCount;
    }

    public int getNumberOfPosts() {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mCreationDate);
        dest.writeString(this.mModificationDate);
        dest.writeString(this.mPostCount);
    }

    public Stats() {
    }

    protected Stats(Parcel in) {
        this.mCreationDate = in.readString();
        this.mModificationDate = in.readString();
        this.mPostCount = in.readString();
    }

    public static final Parcelable.Creator<Stats> CREATOR = new Parcelable.Creator<Stats>() {
        @Override
        public Stats createFromParcel(Parcel source) {
            return new Stats(source);
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };
}
