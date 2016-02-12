package org.hopestarter.wallet.server_api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Adrian on 10/02/2016.
 */
public interface IStagingApi {
    @FormUrlEncoded
    @POST("accounts/demo_signup/")
    Call<Void> signUp(@Field("username")String username, @Field("password1")String password,
            @Field("name")String firstName, @Field("surname")String lastName,
            @Field("ethnicity") String ethnicity);
}
