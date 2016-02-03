package org.hopestarter.wallet.ui;

import android.net.Uri;

import java.util.Comparator;

/**
 * Created by Adrian on 03/02/2016.
 */
public class UpdateInfo {
    protected final String userName;
    protected final String ethnicity;
    protected final String location;
    protected final String message;
    protected final Uri pictureUri;
    protected final Uri profilePictureUri;
    protected final int updateViews;
    protected final long updateDateMillis;

    public UpdateInfo(UpdateInfo.Builder builder) {
        this.profilePictureUri = builder.getProfilePictureUri();
        this.message = builder.getMessage();
        this.updateDateMillis = builder.getUpdateDateMillis();
        this.location = builder.getLocation();
        this.ethnicity = builder.getEthnicity();
        this.updateViews = builder.getUpdateViews();
        this.pictureUri = builder.getPictureUri();
        this.userName = builder.getUserName();
    }

    public String getUserName() {
        return userName;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public String getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    public Uri getPictureUri() {
        return pictureUri;
    }

    public int getUpdateViews() {
        return updateViews;
    }

    public long getUpdateDateMillis() {
        return updateDateMillis;
    }

    public Uri getProfilePictureUri() {
        return profilePictureUri;
    }

    public static class Builder {
        private String userName;
        private String ethnicity;
        private String location;
        private String message;
        private Uri pictureUri;
        private Uri profilePictureUri;
        private int updateViews;
        private long updateDateMillis;

        public UpdateInfo build() {
            return new UpdateInfo(this);
        }

        public String getUserName() {
            return userName;
        }

        public String getEthnicity() {
            return ethnicity;
        }

        public String getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }

        public Uri getPictureUri() {
            return pictureUri;
        }

        public int getUpdateViews() {
            return updateViews;
        }

        public long getUpdateDateMillis() {
            return updateDateMillis;
        }

        public Uri getProfilePictureUri() {
            return profilePictureUri;
        }

        public UpdateInfo.Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public UpdateInfo.Builder setEthnicity(String ethnicity) {
            this.ethnicity = ethnicity;
            return this;
        }

        public UpdateInfo.Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public UpdateInfo.Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public UpdateInfo.Builder setPictureUri(Uri pictureUri) {
            this.pictureUri = pictureUri;
            return this;
        }

        public UpdateInfo.Builder setUpdateViews(int updateViews) {
            this.updateViews = updateViews;
            return this;
        }

        public UpdateInfo.Builder setUpdateDateMillis(long updateDateMillis) {
            this.updateDateMillis = updateDateMillis;
            return this;
        }

        public UpdateInfo.Builder setProfilePictureUri(Uri profilePictureUri) {
            this.profilePictureUri = profilePictureUri;
            return this;
        }
    }

    public static class UpdateInfoInverseDateComparator implements Comparator<UpdateInfo> {

        @Override
        public int compare(UpdateInfo lhs, UpdateInfo rhs) {
            return -Long.valueOf(lhs.getUpdateDateMillis()).compareTo(rhs.getUpdateDateMillis());
        }
    }
}
