package org.hopestarter.wallet.server_api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.hopestarter.wallet.Constants;
import org.hopestarter.wallet.data.UserInfoPrefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Server api client
 */
public class ServerApi {
    private static final String TAG = ServerApi.class.getSimpleName();
    private final Retrofit mApiRetrofit;
    private final IServerApi mApiImpl;
    private final Context mContext;
    private String mAuthHeaderValue;

    public ServerApi(Context context) {
        mContext = context;
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                okhttp3.Response response = chain.proceed(request);

                String lastPathSeg = request.url().pathSegments().get(request.url().pathSegments().size() - 1);
                if (response.code() == 403 && !lastPathSeg.equals("token")) {
                    synchronized (this) {
                        okhttp3.Response newResponse;
                        SharedPreferences prefs = mContext.getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
                        String authHeaderValue = request.header("Authorization");
                        boolean tokenInvalid = false;
                        int tokenStartPos = authHeaderValue.indexOf(" ") + 1;
                        if (tokenStartPos >= authHeaderValue.length()) {
                            tokenInvalid = true;
                        } else {
                            String currentToken = prefs.getString(UserInfoPrefs.TOKEN, "");
                            String requestToken = authHeaderValue.substring(tokenStartPos);
                            if (currentToken.equals(requestToken)) {
                                tokenInvalid = true;
                            }
                        }

                        if (tokenInvalid) {
                            Log.d(TAG, "Refreshing token...");
                            String refToken = prefs.getString(UserInfoPrefs.REFRESH_TOKEN, "");
                            if (!refToken.isEmpty()) {
                                Call<TokenResponse> tokenCall = mApiImpl.getToken("refresh_token", null, null, refToken, null);
                                Response<TokenResponse> tokenResponse = tokenCall.execute();
                                if (tokenResponse.isSuccessful()) {
                                    TokenResponse parsedTokenResp = tokenResponse.body();
                                    prefs.edit().putString(UserInfoPrefs.TOKEN, parsedTokenResp.getAccessToken())
                                            .putString(UserInfoPrefs.REFRESH_TOKEN, parsedTokenResp.getRefreshToken())
                                            .commit();
                                    updateAuthHeaderValue();

                                    Log.d(TAG, "Token refreshed, trying again last request...");
                                    Request newRequest = request.newBuilder().header("Authorization", mAuthHeaderValue).build();
                                    newResponse = chain.proceed(newRequest);
                                } else {
                                    newResponse = response;
                                }
                            } else {
                                newResponse = response;
                            }
                        } else {
                            Log.d(TAG, "Token was refreshed while we did last request, retrying...");
                            updateAuthHeaderValue();
                            Request newRequest = request.newBuilder().header("Authorization", mAuthHeaderValue).build();
                            newResponse = chain.proceed(newRequest);
                        }

                        response.body().close();
                        return newResponse;
                    }
                } else {
                    return response;
                }
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        mApiRetrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(new TokenResponseConverterFactory())
                .addConverterFactory(new UserInfoResponseConverterFactory())
                .addConverterFactory(new UserInfoRequestConverterFactory())
                .addConverterFactory(new OutboundLocationMarkConverterFactory())
                .addConverterFactory(new LocationMarkConverterFactory())
                .addConverterFactory(new CollectorMarkResponse.ConverterFactory())
                .build();

        mApiImpl = mApiRetrofit.create(IServerApi.class);

        updateAuthHeaderValue();

    }

    public void updateAuthHeaderValue() {
        mAuthHeaderValue = "Bearer " + mContext
                .getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE)
                .getString(UserInfoPrefs.TOKEN, "");
    }

    public TokenResponse getToken(String username, String password) throws IOException, AuthenticationFailed, UnexpectedServerResponseException {
        Call<TokenResponse> call = mApiImpl.getToken("password", username, password, null, "set-location update-profile");
        Response<TokenResponse> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public UserInfo getUserInfo() throws NoTokenException, IOException, ForbiddenResourceException,
            UnexpectedServerResponseException, AuthenticationFailed {

        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        Call<UserInfo> call = mApiImpl.getUserInfo(mAuthHeaderValue);
        Response<UserInfo> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public UserInfo setUserInfo(UserInfo info) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        Call<UserInfo> call = mApiImpl.setUserInfo(mAuthHeaderValue, info);
        Response<UserInfo> response = call.execute();
        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public void uploadProfileImage(File profilePicture) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), profilePicture);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("picture", profilePicture.getName(), requestFile);

        Call<ResponseBody> call = mApiImpl.uploadProfilePicture(mAuthHeaderValue, filePart);
        Response<ResponseBody> response = call.execute();

        if (response.isSuccessful()) {
            return; // I might need to change response/result type in the future. If not, change logic for a negative evaluation
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public void uploadPictureForMark(File picture, long markId) throws NoTokenException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, IOException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), picture);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part filePart =
                MultipartBody.Part.createFormData("picture", picture.getName(), requestFile);

        Call<ResponseBody> call = mApiImpl.uploadPictureForMark(mAuthHeaderValue, markId, filePart);
        Response<ResponseBody> response = call.execute();

        if (response.isSuccessful()) {
            return; // I might need to change response/result type in the future. If not, change logic for a negative evaluation
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public LocationMark uploadLocationMark(OutboundLocationMark locationMark) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        Call<LocationMark> call = mApiImpl.uploadLocationMark(mAuthHeaderValue, locationMark);
        Response<LocationMark> response = call.execute();

        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public CollectorMarkResponse getWorldLocationMarks(int page, int pageSize) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, ResourceNotFoundException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        Call<CollectorMarkResponse> call = mApiImpl.getWorldLocationMarks(mAuthHeaderValue, pageSize, page);
        Response<CollectorMarkResponse> response = call.execute();

        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                case 404:
                    throw new ResourceNotFoundException(call.request().url().toString());
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }

    public CollectorMarkResponse getOwnLocationMarks(int page, int pageSize) throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, ResourceNotFoundException {
        if (mAuthHeaderValue.isEmpty()) {
            throw new NoTokenException("No token has been retrieved before. Try authenticating with the server first.");
        }

        Call<CollectorMarkResponse> call = mApiImpl.getOwnLocationMarks(mAuthHeaderValue, pageSize, page);
        Response<CollectorMarkResponse> response = call.execute();

        if (response.isSuccessful()) {
            return response.body();
        } else {
            switch (response.code()) {
                case 401:
                    throw new AuthenticationFailed();
                case 403:
                    throw new ForbiddenResourceException();
                case 404:
                    throw new ResourceNotFoundException(call.request().url().toString());
                default:
                    throw new UnexpectedServerResponseException(response.code());
            }
        }
    }
}
