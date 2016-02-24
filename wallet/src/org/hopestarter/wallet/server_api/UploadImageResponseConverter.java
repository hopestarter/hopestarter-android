package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 23/02/2016.
 */
public class UploadImageResponseConverter implements Converter<ResponseBody, UploadImageResponse> {
    @Override
    public UploadImageResponse convert(ResponseBody value) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(value.string(), UploadImageResponse.class);
    }
}
