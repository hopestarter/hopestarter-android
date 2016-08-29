package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.makeramen.roundedimageview.RoundedImageView;

import org.hopestarter.wallet.WalletApplication;
import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet.server_api.CollectorMarkResponse;
import org.hopestarter.wallet.server_api.LocationMark;
import org.hopestarter.wallet.server_api.LocationMarkUploader;
import org.hopestarter.wallet.util.FetchResult;
import org.hopestarter.wallet.util.ResourceUtils;
import org.hopestarter.wallet.ui.LocationMarksFetcher.Author;
import org.hopestarter.wallet_test.R;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, UpdatesFragment.OnRequestDataListener, LocationMarkUploader.UploaderListener {
    private static final String TAG = ProfileFragment.class.getName();
    private static final Logger log = LoggerFactory.getLogger(ProfileFragment.class);

    // Activity result request codes
    private static final int PHOTO_UPDATE_DATA_REQUEST = 0;

    // Permission request codes
    private static final int PHOTO_UPDATE_PERMISSION_REQUEST_LOCATION = 0;
    public static final int POSTS_PER_PAGE = 20;

    private UpdatesFragment mUpdatesFragment;
    private TextView mNumUpdates;
    private Button mPostBtn;
    private TextView mUserNameView;
    private TextView mUserEthnicityView;
    private TextView mProfileDonations;
    private String mFirstName;
    private String mLastName;
    private String mEthnicity;
    private String mFullName;
    private RelativeLayout mProfileLayout;
    private Uri mProfilePicture;
    private RoundedImageView mProfilePictureView;
    private PhotoUpdateCreator mPhotoUpdateCreator;
    private GoogleApiClient mGoogleApiClient;
    private boolean mPostingEnabled;
    private int mPage;
    private int mPageSize;

    private RequestListener mImageLoaderListener = new RequestListener() {
        @Override
        public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
            log.error("Failed loading image", e);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    private LocationMarksFetcher.OnPostExecuteListener mOnPostFetch = new LocationMarksFetcher.OnPostExecuteListener() {
        @Override
        public void onPostExecute(FetchResult<CollectorMarkResponse> result) {
            if (result.isSuccessful()) {
                CollectorMarkResponse response = result.getResult();

                ArrayList<UpdateInfo> updates = new ArrayList<>();

                List<LocationMark> locationMarks = response.getResults().getLocationMarks();
                for(LocationMark mark : locationMarks) {
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

                    UpdateInfo update = new UpdateInfo.Builder()
                            .setUserName(mFullName)
                            .setUpdateViews(updateViews)
                            .setEthnicity(mEthnicity)
                            .setUpdateDateMillis(updateDateMillis)
                            .setMessage(message)
                            .setPictureUri(pictureUri)
                            .setProfilePictureUri(mProfilePicture)
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

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        SharedPreferences prefs = getActivity().getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        mFirstName = prefs.getString(UserInfoPrefs.FIRST_NAME, getString(R.string.unnamed_first_name));
        mLastName = prefs.getString(UserInfoPrefs.LAST_NAME, getString(R.string.unnamed_last_name));
        mEthnicity = prefs.getString(UserInfoPrefs.ETHNICITY, getString(R.string.no_ethnicity_ethnicity));

        Uri profilePictureSource = Uri.parse(
                prefs.getString(
                    UserInfoPrefs.PROFILE_PIC,
                    ResourceUtils.resIdToUri(
                            getActivity(), R.drawable.avatar_placeholder
                    ).toString()
                )
        );

        if (profilePictureSource.getScheme() == null || profilePictureSource.getScheme().isEmpty()) {
            mProfilePicture = Uri.fromFile(new File(profilePictureSource.getPath()));
        } else {
            mProfilePicture = profilePictureSource;
        }

        mFullName = mFirstName + " " + mLastName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mNumUpdates = (TextView)rootView.findViewById(R.id.profile_updates);

        mUpdatesFragment = UpdatesFragment.newInstance();
        mUpdatesFragment.setOnRequestDataListener(this);

        mPostBtn = (Button)rootView.findViewById(R.id.post_photo_update);
        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPostingEnabled && mPhotoUpdateCreator != null) {
                    mPhotoUpdateCreator.create();
                }
            }
        });

        getChildFragmentManager().beginTransaction()
                .add(R.id.updates_fragment_container, mUpdatesFragment).commit();

        mUserNameView = (TextView)rootView.findViewById(R.id.profile_full_name);
        mUserEthnicityView = (TextView)rootView.findViewById(R.id.profile_ethnicity);
        mProfileDonations = (TextView)rootView.findViewById(R.id.profile_received_donations);
        mProfilePictureView = (RoundedImageView)rootView.findViewById(R.id.profile_image_view);

        mProfileLayout = (RelativeLayout)rootView.findViewById(R.id.profile_layout);
        mProfileLayout.setClickable(true);
        mProfileLayout.setOnTouchListener(new View.OnTouchListener() {
            private float lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                log.debug("Profile layout onTouchListener.onTouch called");
                final int action = event.getAction() & MotionEvent.ACTION_MASK;
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        log.debug("ACTION DOWN event received");
                        lastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        log.debug("ACTION DOWN event received");
                        return moveView(event);
                }
                return false;
            }

            private boolean moveView(MotionEvent event) {
                float dy = event.getRawY() - lastY;
                float newTop = mProfileLayout.getTop() + dy;

                lastY = event.getRawY();

                if (newTop < -mProfileLayout.getHeight()) {
                    dy = dy - (newTop + mProfileLayout.getHeight());
                }

                if (newTop > 0) {
                    dy = dy - newTop;
                }

                if (dy != 0) {
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mProfileLayout.getLayoutParams();
                    layoutParams.topMargin = Double.valueOf(Math.floor(mProfileLayout.getTop() + dy)).intValue();
                    mProfileLayout.setLayoutParams(layoutParams);
                    mProfileLayout.getParent().requestLayout();
                    return true;
                }

                return false;
            }
        });

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            private int taps;

            private Runnable resetRunnable = new Runnable() {
                @Override
                public void run() {
                    taps = 0;
                }
            };

            @Override
            public void onClick(View v) {
                taps++;
                if (taps == 10) {
                    taps = 0;
                    String receiveAddress = ((WalletApplication)getActivity().getApplication()).getWallet().currentReceiveAddress().toString();

                    ClipboardManager clipboardManager = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = ClipData.newPlainText("address", receiveAddress);
                    clipboardManager.setPrimaryClip(data);
                    Toast.makeText(getActivity(), "Address copied to the clipboard", Toast.LENGTH_SHORT).show();

                    AlertDialog addressDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Bitcoin address")
                            .setMessage(receiveAddress)
                            .create();
                    addressDialog.show();
                }
                mProfileLayout.removeCallbacks(resetRunnable);
                mProfileLayout.postDelayed(resetRunnable, 1000);
            }
        });

        mPhotoUpdateCreator = new PhotoUpdateCreator(this, mGoogleApiClient, PHOTO_UPDATE_DATA_REQUEST, PHOTO_UPDATE_PERMISSION_REQUEST_LOCATION);
        mPhotoUpdateCreator.setUploadListener(this);

        feedData();
        updateNumberOfUpdates();
        return rootView;
    }

    private void feedData() {
        feedProfileData();
        mPage = 1;
        mPageSize = POSTS_PER_PAGE;
        feedUpdates();
    }

    private void feedUpdates() {
        LocationMarksFetcher fetcher = new LocationMarksFetcher(getActivity(), Author.CUR_USER);
        fetcher.setListener(mOnPostFetch);
        fetcher.fetch(mPage, mPageSize);
    }

    private void feedProfileData() {
        mUserNameView.setText(mFullName);
        mUserEthnicityView.setText(mEthnicity);
        Glide.with(this).load(mProfilePicture).centerCrop().listener(mImageLoaderListener).into(mProfilePictureView);
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

    private void feedFakeData() {
        Uri path = Uri.parse("android.resource://org.hopestarter.wallet_test/" + R.drawable.test_image);
        Uri path2 = Uri.parse("android.resource://org.hopestarter.wallet_test/" + R.drawable.test_image2);

        UpdateInfo info = new UpdateInfo.Builder()
                .setUserName("Muhammad Erbil")
                .setEthnicity("Syrian")
                .setLocation("Samothrace, Greece")
                .setUpdateDateMillis(System.currentTimeMillis() - 4 * 60 * 1000)
                .setPictureUri(path)
                .setMessage("The island we landed on was called Samothrace. We were so thankful to be there. We thought we'd reached safety.")
                .setUpdateViews(344)
                .build();

        UpdateInfo info2 = new UpdateInfo.Builder()
                .setUserName("Muhammad Erbil")
                .setEthnicity("Syrian")
                .setLocation("Samothrace, Greece")
                .setUpdateDateMillis(System.currentTimeMillis() - 16 * 60 * 1000)
                .setPictureUri(path2)
                .setMessage("Syrian refugees are freezing to death as snow covers the region.")
                .setUpdateViews(688)
                .build();

        mUpdatesFragment.addAll(new UpdateInfo[]{info, info2});

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        mProfileDonations.setText(currencyFormat.format(46.30));
    }

    private void updateNumberOfUpdates() {
        mNumUpdates.setText(String.format(Locale.US, "%d", mUpdatesFragment.getNumberOfUpdates()));
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
    public void onActivityResult(int reqCode, int resCode, final Intent data) {
        switch(reqCode) {
            case PHOTO_UPDATE_DATA_REQUEST:
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
    public void onConnected(@Nullable Bundle bundle) {
        mPostingEnabled = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPostingEnabled = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestData() {
        mPage++;
        feedUpdates();
    }

    @Override
    public void onUploadCompleted(Exception ex) {
        mUpdatesFragment.clear();
        mPage = 1;
        feedUpdates();
    }
}
