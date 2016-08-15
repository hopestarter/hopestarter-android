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

    @SerializedName("photo")
    private PhotoResource mPicture;

    @SerializedName("bitcoin")
    private String mBitcoinAddress;

    public String getFirstName() {
        return mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public PhotoResource getPictureResources() {
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
        private PhotoResource profilePictureResources;
        private String bitcoinReceivingAddress;

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setProfilePicture(PhotoResource photoResource) {
            this.profilePictureResources = photoResource;
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
}
