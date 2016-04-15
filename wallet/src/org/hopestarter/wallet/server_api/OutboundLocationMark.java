package org.hopestarter.wallet.server_api;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * Created by Adrian on 14/04/2016.
 */
public class OutboundLocationMark {
    @SerializedName("created")
    private Date mCreated;

    @SerializedName("point")
    private Point mPoint;

    @SerializedName("picture")
    private List<URI> mPictures;

    @SerializedName("text")
    private String mText;

    public OutboundLocationMark(Date created, Point point, List<URI> pictures, String text) {
        mCreated = created;
        mPoint = point;
        mPictures = pictures;
        mText = text;
    }

    public Date getCreated() {
        return mCreated;
    }

    public Point getPoint() {
        return mPoint;
    }

    public List<URI> getPictures() {
        return mPictures;
    }

    public String getText() {
        return mText;
    }
}
