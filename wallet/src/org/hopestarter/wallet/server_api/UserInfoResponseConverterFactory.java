package org.hopestarter.wallet.server_api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by Adrian on 18/02/2016.
 */
public class UserInfoResponseConverterFactory extends Converter.Factory {
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (type == UserInfo.class) {
            return new UserInfoResponseConverter();
        }
        return null;
    }
}
