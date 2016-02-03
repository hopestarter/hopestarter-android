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


public class UpdatesFragment extends Fragment {

    private static final String TAG = UpdatesFragment.class.getName();
    private RecyclerView mRecyclerView;
    private Picasso mImageLoader;
    private ProfileUpdatesAdapter mAdapter = new ProfileUpdatesAdapter();

    public UpdatesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment ProfileFragment.
     */
    public static UpdatesFragment newInstance() {
        UpdatesFragment fragment = new UpdatesFragment();
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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new UpdateItemDecoration(getActivity()));
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 10);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public class UpdateItemDecoration extends RecyclerView.ItemDecoration {
        private Context context;

        public UpdateItemDecoration(Context context) {
            this.context = context;
        }

        @Override
        public void getItemOffsets (Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            float density = context.getResources().getDisplayMetrics().density;
            int margin = Double.valueOf(Math.floor(10 * density)).intValue();
            outRect.set(margin, margin, margin, 0);
        }
    }

    public void add(UpdateInfo item) {
        mAdapter.add(item);
    }

    public void addAll(List<UpdateInfo> updateList) {
        mAdapter.addAll(updateList);
    }

    public void addAll(UpdateInfo[] updateList) {
        mAdapter.addAll(updateList);
    }

    public void add(int index, UpdateInfo item) {
        mAdapter.add(index, item);
    }

    public void remove(UpdateInfo item) {
        mAdapter.remove(item);
    }

    public void remove(int index) {
        mAdapter.remove(index);
    }

    public void clear() {
        mAdapter.clear();
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
