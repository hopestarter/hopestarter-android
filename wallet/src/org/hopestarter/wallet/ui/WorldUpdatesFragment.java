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
import org.hopestarter.wallet.util.ResourceUtils;
import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldUpdatesFragment extends Fragment {

    private static final String TAG = WorldUpdatesFragment.class.getName();
    private static final Logger log = LoggerFactory.getLogger(WorldUpdatesFragment.class);
    private static final int POST_UPDATE_REQ_CODE = 0;
    private UpdatesFragment mUpdatesFragment;
    private Picasso mImageLoader;
    private TextView mNumUpdates;
    private Button mPostPictureUpdate;
    private String mFirstName;
    private String mLastName;
    private String mFullName;
    private String mEthnicity;
    private Uri mProfilePicture;

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

        mImageLoader = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                log.error("Failed loading picture at " + uri.toString(), exception);
            }
        }).build();

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_worldupdates, container, false);
        mUpdatesFragment = UpdatesFragment.newInstance();

        mPostPictureUpdate = (Button)rootView.findViewById(R.id.post_photo_update);

        mPostPictureUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCreateNewUpdateActivity();
            }
        });

        getChildFragmentManager().beginTransaction()
                .add(R.id.updates_fragment_container, mUpdatesFragment).commit();

        feedFakeData();
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

        UpdateInfo info3 = new UpdateInfo.Builder()
                .setUserName("Dawud Wasamet")
                .setEthnicity("Iraqi")
                .setLocation("hegyeshalom, Hungary")
                .setUpdateDateMillis(System.currentTimeMillis() - 6 * 60 * 60 * 1000)
                .setPictureUri(path2)
                .setMessage("Syrian refugees are freezing to death as snow covers the region.")
                .setUpdateViews(230)
                .build();

        mUpdatesFragment.addAll(new UpdateInfo[] {info, info2, info3});
    }

    private void launchCreateNewUpdateActivity() {
        Intent activityIntent = new Intent(getActivity(), CreateNewUpdateActivity.class);
        startActivityForResult(activityIntent, POST_UPDATE_REQ_CODE);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch(reqCode) {
            case POST_UPDATE_REQ_CODE:
                if (resCode == Activity.RESULT_OK) {
                    UpdateInfo update = new UpdateInfo.Builder()
                            .setUserName(mFullName)
                            .setPictureUri(data.getData())
                            .setProfilePictureUri(mProfilePicture)
                            .setMessage(data.getStringExtra(CreateNewUpdateActivity.EXTRA_RESULT_MESSAGE))
                            .setUpdateDateMillis(System.currentTimeMillis())
                            .setEthnicity(mEthnicity)
                            .setLocation("Unknown")
                            .build();

                    mUpdatesFragment.add(update);
                }
                break;
            default:
                super.onActivityResult(reqCode, resCode, data);
        }
    }


}
