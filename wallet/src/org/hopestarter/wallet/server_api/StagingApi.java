package org.hopestarter.wallet.server_api;

import org.hopestarter.wallet.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Adrian on 10/02/2016.
 */
public class StagingApi {
    private static final Logger log = LoggerFactory.getLogger(StagingApi.class);
    private final Retrofit mStagingRetrofit;
    private final IStagingApi mApiImpl;

    public StagingApi() {
        Interceptor interceptor = new Interceptor() {
            private final Logger log = LoggerFactory.getLogger(StagingApi.class);
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                log.debug("Requesting " + request.method() + " " + request.url().toString());
                okhttp3.Response response = chain.proceed(request);
                log.debug("Response code " + Integer.toString(response.code()) + " from " +request.url().toString());
                return response;
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();

        mStagingRetrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Constants.STAGING_BASE_URL)
                .build();

        mApiImpl = mStagingRetrofit.create(IStagingApi.class);
    }

    public int signUp(String username, String password, String firstName, String lastName, String ethnicity) throws IOException {
        Call<Void> call = mApiImpl.signUp(username, password, firstName, lastName, ethnicity);
        Response response = call.execute();
        return response.code();
    }
}
