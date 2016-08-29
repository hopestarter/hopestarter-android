package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.server_api.CollectorMarkResponse;
import org.hopestarter.wallet.server_api.LocationMark;
import org.hopestarter.wallet.server_api.LocationMarkUploader;
import org.hopestarter.wallet.server_api.User;
import org.hopestarter.wallet.server_api.UserInfo;
import org.hopestarter.wallet.ui.LocationMarksFetcher.Author;
import org.hopestarter.wallet.util.FetchResult;
import org.hopestarter.wallet.util.ResourceUtils;
import org.hopestarter.wallet_test.R;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WorldUpdatesFragment extends Fragment implements UpdatesFragment.OnRequestDataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationMarkUploader.UploaderListener {
    private static final String TAG = WorldUpdatesFragment.class.getName();
    private static final Logger log = LoggerFactory.getLogger(WorldUpdatesFragment.class);
    private static final int POST_UPDATE_REQ_CODE = 0;
    private static final int PHOTO_UPDATE_DATA_REQUEST = 0;
    private static final int PHOTO_UPDATE_PERMISSION_REQUEST_LOCATION = 1;

    private GoogleApiClient mGoogleApiClient;
    private PhotoUpdateCreator mPhotoUpdateCreator;
    private UpdatesFragment mUpdatesFragment;
    private Button mPostPictureUpdate;
    private String mFirstName;
    private String mLastName;
    private String mFullName;
    private String mEthnicity;
    private Uri mProfilePicture;
    private int mPage;
    private int mPageSize;

    private LocationMarksFetcher.OnPostExecuteListener mOnPostFetch = new LocationMarksFetcher.OnPostExecuteListener() {
        @Override
        public void onPostExecute(FetchResult<CollectorMarkResponse> result) {
            if (result.isSuccessful()) {
                CollectorMarkResponse response = result.getResult();

                ArrayList<UpdateInfo> updates = new ArrayList<>();

                List<LocationMark> locationMarks = response.getResults().getLocationMarks();
                for(LocationMark mark : locationMarks) {
                    User user = mark.getProperties().getUser();
                    UserInfo userInfo = user.getUserInfo();
                    String userName = userInfo.getFirstName() + " " + userInfo.getLastName();
                    String ethnicity = user.getEthnicities().get(0);
                    String message = mark.getProperties().getText();
                    String location = "unknown";

                    int updateViews = 0;

                    DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
                    DateTime updateDate = dateTimeFormatter.parseDateTime(mark.getProperties().getCreatedDate());
                    long updateDateMillis = updateDate.getMillis();

                    Uri pictureUri = null;
                    if (mark.getProperties().getPhotoResources().getMedium() != null) {
                        pictureUri = Uri.parse(mark.getProperties().getPhotoResources().getLarge());
                    }

                    Uri profilePictureUri = null;
                    if (userInfo.getPictureResources().getThumbnail() != null) {
                        profilePictureUri = Uri.parse(userInfo.getPictureResources().getThumbnail());
                    }

                    UpdateInfo update = new UpdateInfo.Builder()
                            .setUserName(userName)
                            .setUpdateViews(updateViews)
                            .setEthnicity(ethnicity)
                            .setUpdateDateMillis(updateDateMillis)
                            .setMessage(message)
                            .setPictureUri(pictureUri)
                            .setProfilePictureUri(profilePictureUri)
                            .setLocation(location)
                            .build();

                    updates.add(update);
                }
                //mUpdatesFragment.clear();
                mUpdatesFragment.addAll(updates);

                if (response.getNext() != null) {
                    mUpdatesFragment.askForData(true);
                }
            } else {
                Throwable t = result.getException();
                Log.e(TAG, "Couldn't fetch location marks", t);
            }
        }
    };
    private boolean mPostingEnabled;

    public WorldUpdatesFragment() {
        // Required empty public constructor
    }

    public static WorldUpdatesFragment newInstance() {
        WorldUpdatesFragment fragment = new WorldUpdatesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getActivity().getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        mFirstName = prefs.getString(UserInfoPrefs.FIRST_NAME, getString(R.string.unnamed_first_name));
        mLastName = prefs.getString(UserInfoPrefs.LAST_NAME, getString(R.string.unnamed_last_name));
        mEthnicity = prefs.getString(UserInfoPrefs.ETHNICITY, getString(R.string.no_ethnicity_ethnicity));
        mProfilePicture = Uri.parse(
                prefs.getString(
                        UserInfoPrefs.PROFILE_PIC,
                        ResourceUtils.resIdToUri(
                                getActivity(), R.drawable.avatar_placeholder
                        ).toString()
                )
        );
        mFullName = mFirstName + " " + mLastName;

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_worldupdates, container, false);
        mUpdatesFragment = UpdatesFragment.newInstance();
        mUpdatesFragment.setOnRequestDataListener(this);
        mPostPictureUpdate = (Button)rootView.findViewById(R.id.post_photo_update);

        mPostPictureUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostingEnabled) {
                    mPhotoUpdateCreator.create();
                }
            }
        });

        getChildFragmentManager().beginTransaction()
                .add(R.id.updates_fragment_container, mUpdatesFragment).commit();

        mPhotoUpdateCreator = new PhotoUpdateCreator(this, mGoogleApiClient, PHOTO_UPDATE_DATA_REQUEST, PHOTO_UPDATE_PERMISSION_REQUEST_LOCATION);
        mPhotoUpdateCreator.setUploadListener(this);
        mPage = 1;
        mPageSize = 20;

        feedData();
        return rootView;
    }

    private void feedData() {
        LocationMarksFetcher fetcher = new LocationMarksFetcher(getActivity(), Author.ALL_USERS);
        fetcher.setListener(mOnPostFetch);
        fetcher.fetch(mPage, mPageSize);
    }

    private void launchCreateNewUpdateActivity() {
        Intent activityIntent = new Intent(getActivity(), CreateNewUpdateActivity.class);
        startActivityForResult(activityIntent, POST_UPDATE_REQ_CODE);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, final Intent data) {
        switch(reqCode) {
            case POST_UPDATE_REQ_CODE:
                if (resCode == Activity.RESULT_OK) {
                    if (!mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(@Nullable Bundle bundle) {
                                mPhotoUpdateCreator.onActivityResult(data);
                                mGoogleApiClient.unregisterConnectionCallbacks(this);
                            }

                            @Override
                            public void onConnectionSuspended(int i) {

                            }
                        });
                    } else {
                        mPhotoUpdateCreator.onActivityResult(data);
                    }
                }
                break;
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResult) {
        switch(requestCode) {
            case PHOTO_UPDATE_PERMISSION_REQUEST_LOCATION:
                mPhotoUpdateCreator.onRequestPermissionsResult(permissions, grantResult);
                break;
        }
    }

    @Override
    public void onRequestData() {
        mPage++;
        feedData();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPostingEnabled = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPostingEnabled = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mPostingEnabled = false;
        Log.e(TAG, "Error connecting to google api: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onUploadCompleted(Exception ex) {
        if (ex == null) {
            mPage = 1;
            mUpdatesFragment.clear();
            feedData();
        }
    }
}
