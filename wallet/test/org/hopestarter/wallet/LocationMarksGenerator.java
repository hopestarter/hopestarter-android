package org.hopestarter.wallet;

import org.hopestarter.wallet.server_api.CollectorMarkResponse;
import org.hopestarter.wallet.server_api.IServerApi;
import org.hopestarter.wallet.server_api.LocationMark;
import org.hopestarter.wallet.server_api.LocationMarkConverterFactory;
import org.hopestarter.wallet.server_api.OutboundLocationMark;
import org.hopestarter.wallet.server_api.OutboundLocationMarkConverterFactory;
import org.hopestarter.wallet.server_api.Point;
import org.hopestarter.wallet.server_api.TokenResponse;
import org.hopestarter.wallet.server_api.TokenResponseConverterFactory;
import org.hopestarter.wallet.server_api.UploadImageResponseConverterFactory;
import org.hopestarter.wallet.server_api.UserInfoRequestConverterFactory;
import org.hopestarter.wallet.server_api.UserInfoResponseConverterFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Part;

/**
 * Created by Adrian on 29/08/2016.
 */
public class LocationMarksGenerator {
    @Test
    public void generate() throws IOException {
        IServerApi api = new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(new TokenResponseConverterFactory())
                .addConverterFactory(new UserInfoResponseConverterFactory())
                .addConverterFactory(new UserInfoRequestConverterFactory())
                .addConverterFactory(new UploadImageResponseConverterFactory())
                .addConverterFactory(new OutboundLocationMarkConverterFactory())
                .addConverterFactory(new LocationMarkConverterFactory())
                .addConverterFactory(new CollectorMarkResponse.ConverterFactory())
                .build().create(IServerApi.class);

        Call<TokenResponse> loginCall = api.getToken("password", "864587024232887", "demopassword", "set-location update-profile");
        Response<TokenResponse> loginResponse = loginCall.execute();
        Assert.assertTrue(loginResponse.isSuccessful());
        String token = loginResponse.body().getAccessToken();

        for(int i = 0; i < 100; i++) {
            OutboundLocationMark mark = new OutboundLocationMark(new Date(System.currentTimeMillis()), new Point("point", new float[]{1.0f, 1.0f}), null, "Location mark " + i);
            Call<LocationMark> markCall =  api.uploadLocationMark("Bearer " + token, mark);
            Response<LocationMark> markResponse = markCall.execute();
            Assert.assertTrue(markResponse.isSuccessful());
            LocationMark locationMark = markResponse.body();

            File picture = new File("F:\\dev\\hopestarter-wallet\\wallet\\res\\drawable\\test_image.jpg");

            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), picture);

            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData("picture", picture.getName(), requestFile);

            Call<ResponseBody> markImageCall = api.uploadPictureForMark("Bearer " + token, locationMark.getMarkId(), filePart);
            Response<ResponseBody> markImageResponse = markImageCall.execute();
            Assert.assertTrue(markImageResponse.isSuccessful());
            System.out.println("Sent location mark num " + i);
        }
    }
}
