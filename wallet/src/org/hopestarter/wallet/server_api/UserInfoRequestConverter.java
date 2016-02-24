package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 23/02/2016.
 */
public class UserInfoRequestConverter implements Converter<UserInfo, RequestBody> {
    @Override
    public RequestBody convert(UserInfo value) throws IOException {
        Gson gson = new Gson();
        String jsonString = gson.toJson(value, UserInfo.class);
        return RequestBody.create(MediaType.parse("application/json"), jsonString);
    }
}
