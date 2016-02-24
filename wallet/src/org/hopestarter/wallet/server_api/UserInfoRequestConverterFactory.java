package org.hopestarter.wallet.server_api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by Adrian on 23/02/2016.
 */
public class UserInfoRequestConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (type == UserInfo.class) {
            return new UserInfoRequestConverter();
        }
        return null;
    }
}
