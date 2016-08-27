package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Adrian on 16/08/2016.
 */
public class LocationMark implements Parcelable {
    public static class Properties implements Parcelable {
        @SerializedName("photo")
        private PhotoResources mPhotoResources;

        @SerializedName("text")
        private String mText;

        @SerializedName("created")
        private String mCreatedDate;

        @SerializedName("modified")
        private String mModifiedDate;

        @SerializedName("user")
        private User mUser;

        public PhotoResources getPhotoResources() {
            return mPhotoResources;
        }

        public String getText() {
            return mText;
        }

        public String getCreatedDate() {
            return mCreatedDate;
        }

        public String getModifiedDate() {
            return mModifiedDate;
        }

        public User getUser() {
            return mUser;
        }

        public Properties() {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mPhotoResources, flags);
            dest.writeString(this.mText);
            dest.writeString(this.mCreatedDate);
            dest.writeString(this.mModifiedDate);
            dest.writeParcelable(this.mUser, flags);
        }

        protected Properties(Parcel in) {
            this.mPhotoResources = in.readParcelable(PhotoResources.class.getClassLoader());
            this.mText = in.readString();
            this.mCreatedDate = in.readString();
            this.mModifiedDate = in.readString();
            this.mUser = in.readParcelable(User.class.getClassLoader());
        }

        public static final Creator<Properties> CREATOR = new Creator<Properties>() {
            @Override
            public Properties createFromParcel(Parcel source) {
                return new Properties(source);
            }

            @Override
            public Properties[] newArray(int size) {
                return new Properties[size];
            }
        };
    }

    @SerializedName("id")
    private long mMarkId;

    @SerializedName("type")
    private String mType;

    @SerializedName("geometry")
    private Geometry mGeometry;

    @SerializedName("properties")
    private Properties mProperties;

    public long getMarkId() {
        return mMarkId;
    }

    public String getType() {
        return mType;
    }

    public Geometry getGeometry() {
        return mGeometry;
    }

    public Properties getProperties() {
        return mProperties;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mMarkId);
        dest.writeString(this.mType);
        dest.writeParcelable(this.mGeometry, flags);
        dest.writeParcelable(this.mProperties, flags);
    }

    public LocationMark() {
    }

    protected LocationMark(Parcel in) {
        this.mMarkId = in.readLong();
        this.mType = in.readString();
        this.mGeometry = in.readParcelable(Geometry.class.getClassLoader());
        this.mProperties = in.readParcelable(Properties.class.getClassLoader());
    }

    public static final Parcelable.Creator<LocationMark> CREATOR = new Parcelable.Creator<LocationMark>() {
        @Override
        public LocationMark createFromParcel(Parcel source) {
            return new LocationMark(source);
        }

        @Override
        public LocationMark[] newArray(int size) {
            return new LocationMark[size];
        }
    };
}
