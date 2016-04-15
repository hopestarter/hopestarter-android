package org.hopestarter.wallet.server_api;

import org.hopestarter.wallet.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

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
    Call<UserInfo> getUserInfo(@Header("Authorization") String authHeaderValue);

    @PUT("api/user/profile/")
    Call<UserInfo> setUserInfo(@Header("Authorization") String authHeaderValue, @Body UserInfo userInfo);

    @POST("api/collector/uploadimage/")
    Call<UploadImageResponse> requestImageUpload(@Header("Authorization") String authHeaderValue);

    @POST("api/collector/mark/")
    Call<ResponseBody> uploadLocationMark(@Header("Authorization") String authHeaderValue, @Body OutboundLocationMark locationMark);
}
