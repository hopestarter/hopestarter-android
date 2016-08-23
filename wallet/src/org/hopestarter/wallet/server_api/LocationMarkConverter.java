package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 16/08/2016.
 */
public class LocationMarkConverter implements Converter<ResponseBody, LocationMark> {

    @Override
    public LocationMark convert(ResponseBody value) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(value.string(), LocationMark.class);
    }
}
