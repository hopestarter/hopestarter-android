package org.hopestarter.wallet.server_api;

import org.hopestarter.wallet.Constants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Adrian on 11/02/2016.
 */
public interface IServerApi {
    @FormUrlEncoded()
    @Headers({"Authorization: Basic " + Constants.BASIC_BASE64_CREDENTIALS})
    @POST("api/o/token/")
    Call<TokenResponse> getToken(@Field("grant_type") String grantType,
            @Field("username") String username, @Field("password") String password,
            @Field("scope") String scope);

    @GET("api/user/profile/")
    Call<UserInfo> getUserInfo(@Header("Authorization") String token);

}
