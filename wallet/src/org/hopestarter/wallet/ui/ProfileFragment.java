package org.hopestarter.wallet.ui;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import org.hopestarter.wallet_test.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private Picasso mImageLoader;
    private ProfileUpdatesAdapter mAdapter;
    private TextView mNumUpdates;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment ProfileFragment.
     */
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

        mAdapter = new ProfileUpdatesAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new UpdateItemDecoration(getActivity()));
        mNumUpdates = (TextView)rootView.findViewById(R.id.profile_updates);

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

        addAllUpdates(new UpdateInfo[] {info, info2});

        return rootView;
    }

    public void addUpdate(UpdateInfo info) {
        mAdapter.add(info);
        updateItemCount();
    }

    public void addAllUpdates(List<UpdateInfo> updates) {
        mAdapter.addAll(updates);
        updateItemCount();
    }

    public void addAllUpdates(UpdateInfo[] updates) {
        mAdapter.addAll(updates);
        updateItemCount();
    }

    public void clearUpdates() {
        mAdapter.clear();
        updateItemCount();
    }

    public void updateItemCount() {
        mNumUpdates.setText(Integer.toString(mAdapter.getItemCount()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public class UpdateItemDecoration extends RecyclerView.ItemDecoration {
        private Context context;

        public UpdateItemDecoration(Context context) {
            this.context = context;
        }

        @Override
        public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            float density = context.getResources().getDisplayMetrics().density;
            outRect.set(0, Double.valueOf(Math.floor(10 * density)).intValue(), 0, 0);
        }
    }

    public class ProfileUpdatesAdapter extends RecyclerView.Adapter<ProfileUpdatesViewHolder> {
        public final ArrayList<UpdateInfo> mUpdates = new ArrayList<>();

        public void add(UpdateInfo item) {
            mUpdates.add(item);
            notifyItemInserted(mUpdates.size()-1);
        }

        public void addAll(List<UpdateInfo> updateList) {
            for(UpdateInfo update : updateList) {
                add(update);
            }
        }

        public void addAll(UpdateInfo[] updateList) {
            addAll(Arrays.asList(updateList));
        }

        public void add(int index, UpdateInfo item) {
            mUpdates.add(index, item);
            notifyItemInserted(index);
        }

        public void remove(UpdateInfo item) {
            int index = mUpdates.indexOf(item);
            mUpdates.remove(index);
            notifyItemRemoved(index);
        }

        public void remove(int index) {
            mUpdates.remove(index);
            notifyItemRemoved(index);
        }

        public void clear() {
            mUpdates.clear();
            notifyDataSetChanged();
        }

        @Override
        public ProfileUpdatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rootView = inflater.inflate(R.layout.recyclerview_update_item_layout, parent, false);
            return new ProfileUpdatesViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(ProfileUpdatesViewHolder holder, int position) {
            UpdateInfo data = mUpdates.get(position);

            holder.data = data;
            holder.userNameView.setText(data.getUserName());
            holder.messageView.setText(data.getMessage());

            if (data.getProfilePictureUri() != null) {
                mImageLoader.load(data.getProfilePictureUri()).fit().centerCrop().into(holder.userPictureView);
            } else {
                mImageLoader.load(R.drawable.avatar_placeholder).fit().centerCrop().into(holder.userPictureView);
            }

            if (data.getPictureUri() != null) {

                mImageLoader.load(data.getPictureUri()).fit().centerCrop().into(holder.attachedImageView);
            }

            String additionalInfo = String.format(
                    "%s · %s · %s",
                    data.getEthnicity(),
                    data.getLocation(),
                    getFormattedUpdateTimeDifference(data.getUpdateDateMillis())
            );

            holder.additionalInfoView.setText(additionalInfo);
            String viewCount = String.format("%1$d %2$s", data.getUpdateViews(), getString(R.string.views));
            holder.viewCountView.setText(viewCount);
        }

        @Override
        public int getItemCount() {
            return mUpdates.size();
        }

        private String getFormattedUpdateTimeDifference(long timeMillis) {
            String[] units = {"second", "minute", "hour", "day", "week", "month", "year", "decade"};
            String[] unitsPlural = {"seconds", "minutes", "hours", "days", "weeks", "months", "years", "decades"};
            double[] unitDifferences = {60, 60, 24, 7, 4.5, 12, 10};

            long now = System.currentTimeMillis();

            double difference;
            String tense;
            if(now > timeMillis) {
                difference = now - timeMillis;
                tense = "ago";
            } else {
                difference = timeMillis - now;
                tense = "from now";
            }

            difference /= 1000; // Millis to seconds conversion

            int i;
            for (i = 0; i < unitDifferences.length && difference >= unitDifferences[i]; i++) {
                difference = difference / unitDifferences[i];
            }

            difference = Math.floor(difference);

            String unit;
            if (difference > 1) {
                unit = unitsPlural[i];
            } else if(difference == 1) {
                unit = units[i];
            } else {
                return getString(R.string.now);
            }

            String timeDiffFormat = "%1$d %2$s %3$s";
            return String.format(timeDiffFormat, Double.valueOf(difference).longValue(), unit, tense);
        }
    }

    public static class UpdateInfo {
        private final String userName;
        private final String ethnicity;
        private final String location;
        private final String message;
        private final Uri pictureUri;
        private final Uri profilePictureUri;
        private final int updateViews;
        private final long updateDateMillis;

        public String getUserName() {
            return userName;
        }

        public String getEthnicity() {
            return ethnicity;
        }

        public String getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }

        public Uri getPictureUri() {
            return pictureUri;
        }

        public int getUpdateViews() {
            return updateViews;
        }

        public long getUpdateDateMillis() {
            return updateDateMillis;
        }

        public Uri getProfilePictureUri() {
            return profilePictureUri;
        }

        public UpdateInfo(UpdateInfo.Builder builder) {
            this.userName = builder.getUserName();
            this.ethnicity = builder.getEthnicity();
            this.location = builder.getLocation();
            this.message = builder.getMessage();
            this.pictureUri = builder.getPictureUri();
            this.profilePictureUri = builder.getProfilePictureUri();
            this.updateViews = builder.getUpdateViews();
            this.updateDateMillis = builder.getUpdateDateMillis();
        }

        public static class Builder {
            private String userName;
            private String ethnicity;
            private String location;
            private String message;
            private Uri pictureUri;
            private Uri profilePictureUri;
            private int updateViews;
            private long updateDateMillis;

            public UpdateInfo build() {
                return new UpdateInfo(this);
            }

            public String getUserName() {
                return userName;
            }

            public String getEthnicity() {
                return ethnicity;
            }

            public String getLocation() {
                return location;
            }

            public String getMessage() {
                return message;
            }

            public Uri getPictureUri() {
                return pictureUri;
            }

            public int getUpdateViews() {
                return updateViews;
            }

            public long getUpdateDateMillis() {
                return updateDateMillis;
            }

            public Uri getProfilePictureUri() {
                return profilePictureUri;
            }

            public Builder setUserName(String userName) {
                this.userName = userName;
                return this;
            }

            public Builder setEthnicity(String ethnicity) {
                this.ethnicity = ethnicity;
                return this;
            }

            public Builder setLocation(String location) {
                this.location = location;
                return this;
            }

            public Builder setMessage(String message) {
                this.message = message;
                return this;
            }

            public Builder setPictureUri(Uri pictureUri) {
                this.pictureUri = pictureUri;
                return this;
            }

            public Builder setUpdateViews(int updateViews) {
                this.updateViews = updateViews;
                return this;
            }

            public Builder setUpdateDateMillis(long updateDateMillis) {
                this.updateDateMillis = updateDateMillis;
                return this;
            }

            public Builder setProfilePictureUri(Uri profilePictureUri) {
                this.profilePictureUri = profilePictureUri;
                return this;
            }
        }
    }

    public class ProfileUpdatesViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public TextView viewCountView;
        public ImageView attachedImageView;
        public RoundedImageView userPictureView;
        public TextView messageView;
        public TextView additionalInfoView;
        public TextView userNameView;
        public UpdateInfo data;

        public ProfileUpdatesViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            userPictureView = (RoundedImageView)rootView.findViewById(R.id.user_profile_picture);
            userNameView = (TextView)rootView.findViewById(R.id.user_name);
            additionalInfoView = (TextView)rootView.findViewById(R.id.additional_info);
            messageView = (TextView)rootView.findViewById(R.id.message);
            attachedImageView = (ImageView)rootView.findViewById(R.id.attached_picture);
            viewCountView = (TextView)rootView.findViewById(R.id.view_count);
        }
    }
}
