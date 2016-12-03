package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Adrian on 25/08/2016.
 */
public class User implements Parcelable {
    @SerializedName("ethnicities")
    private ArrayList<String> mEthnicities;

    @SerializedName("mark")
    private String mUserMarksUri;

    @SerializedName("profile")
    private UserInfo mUserInfo;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("stats")
    private Stats mStats;

    public ArrayList<String> getEthnicities() {
        return mEthnicities;
    }

    public String getUserMarksUri() {
        return mUserMarksUri;
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public String getUsername() {
        return mUsername;
    }

    public Stats getStats() {
        return mStats;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mEthnicities);
        dest.writeString(this.mUserMarksUri);
        dest.writeParcelable(this.mUserInfo, flags);
        dest.writeString(this.mUsername);
        dest.writeParcelable(this.mStats, flags);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.mEthnicities = in.createStringArrayList();
        this.mUserMarksUri = in.readString();
        this.mUserInfo = in.readParcelable(UserInfo.class.getClassLoader());
        this.mUsername = in.readString();
        this.mStats = in.readParcelable(Stats.class.getClassLoader());
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
