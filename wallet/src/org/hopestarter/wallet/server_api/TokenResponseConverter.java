package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 11/02/2016.
 */
public class TokenResponseConverter implements Converter<ResponseBody, TokenResponse> {
    @Override
    public TokenResponse convert(ResponseBody value) throws IOException {
        Gson gson = new Gson();
        return gson.fromJson(value.string(), TokenResponse.class);
    }
}
