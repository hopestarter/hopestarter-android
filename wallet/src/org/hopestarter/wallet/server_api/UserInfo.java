package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UserInfo implements Parcelable {
    @SerializedName("name")
    private String mFirstName;

    @SerializedName("surname")
    private String mLastName;

    @SerializedName("photo")
    private PhotoResources mPicture;

    @SerializedName("bitcoin")
    private String mBitcoinAddress;

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public PhotoResources getPictureResources() {
        return mPicture;
    }

    public String getBitcoinAddress() { return mBitcoinAddress; }

    public UserInfo(Builder builder) {
        mFirstName = builder.firstName;
        mLastName = builder.lastName;
        mPicture = builder.profilePictureResources;
        mBitcoinAddress = builder.bitcoinReceivingAddress;
    }

    public static class Builder {
        private String firstName;
        private String lastName;
        private PhotoResources profilePictureResources;
        private String bitcoinReceivingAddress;

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setProfilePicture(PhotoResources photoResources) {
            this.profilePictureResources = photoResources;
            return this;
        }

        public Builder setBitcoinAddress(String bitcoinReceivingAddress) {
            this.bitcoinReceivingAddress = bitcoinReceivingAddress;
            return this;
        }

        public UserInfo create() {
            return new UserInfo(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mFirstName);
        dest.writeString(this.mLastName);
        dest.writeParcelable(this.mPicture, flags);
        dest.writeString(this.mBitcoinAddress);
    }

    protected UserInfo(Parcel in) {
        this.mFirstName = in.readString();
        this.mLastName = in.readString();
        this.mPicture = in.readParcelable(PhotoResources.class.getClassLoader());
        this.mBitcoinAddress = in.readString();
    }

    public static final Parcelable.Creator<UserInfo> CREATOR = new Parcelable.Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}
