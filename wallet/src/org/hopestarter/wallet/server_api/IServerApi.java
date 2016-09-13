package org.hopestarter.wallet.server_api;

import org.hopestarter.wallet.Constants;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Server api interface definition for Retrofit
 */
public interface IServerApi {
    @FormUrlEncoded()
    @Headers({"Authorization: Basic " + Constants.BASIC_BASE64_CREDENTIALS})
    @POST("api/o/token/")
    Call<TokenResponse> getToken(@Field("grant_type") String grantType,
            @Field("username") String username, @Field("password") String password,
            @Field("refresh_token") String refreshToken, @Field("scope") String scope);

    @GET("api/user/profile/")
    Call<UserInfo> getUserInfo(@Header("Authorization") String authHeaderValue);

    @PUT("api/user/profile/")
    Call<UserInfo> setUserInfo(@Header("Authorization") String authHeaderValue,
            @Body UserInfo userInfo);

    @Multipart
    @PUT("api/user/profile/picture")
    Call<ResponseBody> uploadProfilePicture(@Header("Authorization") String authHeaderValue,
            @Part MultipartBody.Part picture);

    @Multipart
    @PUT("api/collector/image/{markid}/")
    Call<ResponseBody> uploadPictureForMark(
            @Header("Authorization") String authHeaderValue, @Path("markid") Long markId,
            @Part MultipartBody.Part picture);

    @POST("api/collector/mark/")
    Call<LocationMark> uploadLocationMark(
            @Header("Authorization") String authHeaderValue, @Body OutboundLocationMark locationMark);

    @GET("api/collector/marks/")
    Call<CollectorMarkResponse> getWorldLocationMarks(@Header("Authorization") String authHeaderValue,
            @Query("page_size") int size, @Query("page") int pageNum);

    @GET("api/user/mark/")
    Call<CollectorMarkResponse> getOwnLocationMarks(@Header("Authorization") String authHeaderValue,
            @Query("page_size") int size, @Query("page") int pageNum);
}
