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
 * Created by Adrian on 11/02/2016.
 */
public class ServerApi {
    private final Retrofit mApiRetrofit;
    private final IServerApi mApiImpl;

    public ServerApi() {
        Interceptor interceptor = new Interceptor() {
            private final Logger log = LoggerFactory.getLogger(ServerApi.class);
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

        mApiRetrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(new TokenResponseConverterFactory())
                .build();

        mApiImpl = mApiRetrofit.create(IServerApi.class);
    }

    public String getToken(String username, String password) throws IOException, ResponseNotOkException {
        Call<TokenResponse> call = mApiImpl.getToken("password", username, password, "set-location");
        Response<TokenResponse> response = call.execute();
        if (response.isSuccess()) {
            TokenResponse tokenResp = response.body();
            return tokenResp.getAccessToken();
        } else {
            throw new ResponseNotOkException("Failed to authenticate\nResponse code: " + Integer.toString(response.code()));
        }
    }
}
