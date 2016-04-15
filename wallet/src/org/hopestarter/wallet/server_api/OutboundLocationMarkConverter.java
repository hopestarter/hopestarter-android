package org.hopestarter.wallet.server_api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * Created by Adrian on 14/04/2016.
 */
public class OutboundLocationMarkConverter implements Converter<OutboundLocationMark, RequestBody> {
    @Override
    public RequestBody convert(OutboundLocationMark value) throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(OutboundLocationMark.class, new OutboundLocationMarkSerializer())
                .create();
        return RequestBody.create(MediaType.parse("application/json"), gson.toJson(value));
    }
}
