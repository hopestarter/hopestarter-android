package org.hopestarter.wallet.server_api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by Adrian on 25/08/2016.
 */
public class CollectorMarkResponse implements Parcelable {
    @SerializedName("count")
    private int mCount;

    @SerializedName("next")
    private String mNext;

    @SerializedName("previous")
    private String mPrevious;

    @SerializedName("results")
    private Results mResults;

    public int getCount() {
        return mCount;
    }

    public String getNext() {
        return mNext;
    }

    public String getPrevious() {
        return mPrevious;
    }

    public Results getResults() {
        return mResults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCount);
        dest.writeString(this.mNext);
        dest.writeString(this.mPrevious);
        dest.writeParcelable(this.mResults, flags);
    }

    public CollectorMarkResponse() {
    }

    protected CollectorMarkResponse(Parcel in) {
        this.mCount = in.readInt();
        this.mNext = in.readString();
        this.mPrevious = in.readString();
        this.mResults = in.readParcelable(Results.class.getClassLoader());
    }

    public static final Parcelable.Creator<CollectorMarkResponse> CREATOR = new Parcelable.Creator<CollectorMarkResponse>() {
        @Override
        public CollectorMarkResponse createFromParcel(Parcel source) {
            return new CollectorMarkResponse(source);
        }

        @Override
        public CollectorMarkResponse[] newArray(int size) {
            return new CollectorMarkResponse[size];
        }
    };

    public static class Converter implements retrofit2.Converter<ResponseBody, CollectorMarkResponse> {
        @Override
        public CollectorMarkResponse convert(ResponseBody value) throws IOException {
            Gson gson = new Gson();
            return gson.fromJson(value.string(), CollectorMarkResponse.class);
        }
    }

    public static class ConverterFactory extends retrofit2.Converter.Factory {
        @Override
        public retrofit2.Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (type == CollectorMarkResponse.class) {
                return new Converter();
            }
            return null;
        }
    }
}
