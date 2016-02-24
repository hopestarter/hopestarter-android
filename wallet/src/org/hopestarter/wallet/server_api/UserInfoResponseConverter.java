package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 18/02/2016.
 */
public class UserInfoResponseConverter implements Converter<ResponseBody, UserInfo> {
    @Override
    public UserInfo convert(ResponseBody value) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(value.string(), UserInfo.class);
    }
}
