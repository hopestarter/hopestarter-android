package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 03/12/2016.
 */
public class UserResponseConverter implements Converter<ResponseBody, User> {
    @Override
    public User convert(ResponseBody value) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(value.string(), User.class);
    }
}
