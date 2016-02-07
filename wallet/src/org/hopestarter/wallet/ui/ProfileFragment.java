package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.hopestarter.wallet.data.UserInfoPrefs;
import org.hopestarter.wallet_test.R;

import java.text.NumberFormat;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getName();
    private static final int POST_UPDATE_REQ_CODE = 0;
    private UpdatesFragment mUpdatesFragment;
    private Picasso mImageLoader;
    private TextView mNumUpdates;
    private Button mPostBtn;
    private TextView mUserNameView;
    private TextView mUserEthnicityView;
    private TextView mProfileDonations;
    private String mFirstName;
    private String mLastName;
    private String mEthnicity;
    private String mFullName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e(TAG, "Failed loading picture at " + uri.toString(), exception);
            }
        }).build();

        SharedPreferences prefs = getActivity().getSharedPreferences(UserInfoPrefs.PREF_FILE, Context.MODE_PRIVATE);
        mFirstName = prefs.getString(UserInfoPrefs.FIRST_NAME, "Unnamed");
        mLastName = prefs.getString(UserInfoPrefs.LAST_NAME, "User");
        mEthnicity = prefs.getString(UserInfoPrefs.ETHNICITY, "No ethnicity");
        mFullName = mFirstName + " " + mLastName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mNumUpdates = (TextView)rootView.findViewById(R.id.profile_updates);

        mUpdatesFragment = UpdatesFragment.newInstance();

        mPostBtn = (Button)rootView.findViewById(R.id.post_photo_update);

        mPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCreateNewUpdateActivity();
            }
        });

        getChildFragmentManager().beginTransaction()
                .add(R.id.updates_fragment_container, mUpdatesFragment).commit();

        mUserNameView = (TextView)rootView.findViewById(R.id.profile_full_name);
        mUserEthnicityView = (TextView)rootView.findViewById(R.id.profile_ethnicity);
        mProfileDonations = (TextView)rootView.findViewById(R.id.profile_received_donations);

        feedFakeData();
        updateNumberOfUpdates();
        return rootView;
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



        mUserNameView.setText(mFullName);
        mUserEthnicityView.setText(mEthnicity);
    }

    private void launchCreateNewUpdateActivity() {
        Intent activityIntent = new Intent(getActivity(), CreateNewUpdateActivity.class);
        startActivityForResult(activityIntent, POST_UPDATE_REQ_CODE);
    }

    private void updateNumberOfUpdates() {
        mNumUpdates.setText(Integer.toString(mUpdatesFragment.getNumberOfUpdates()));
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case POST_UPDATE_REQ_CODE:
                if (resCode == Activity.RESULT_OK) {
                    UpdateInfo update = new UpdateInfo.Builder()
                            .setUserName(mFullName)
                            .setPictureUri(data.getData())
                            .setMessage(data.getStringExtra(CreateNewUpdateActivity.EXTRA_RESULT_MESSAGE))
                            .setUpdateDateMillis(System.currentTimeMillis())
                            .setEthnicity(mEthnicity)
                            .setLocation("Unknown")
                            .build();

                    mUpdatesFragment.add(update);
                    updateNumberOfUpdates();
                }
                break;
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }


}
