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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.makeramen.roundedimageview.RoundedImageView;

import org.hopestarter.wallet_test.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class UpdatesFragment extends Fragment {
    public interface OnRequestDataListener {
        void onRequestData();
    }

    private static final String TAG = UpdatesFragment.class.getName();
    private static final Logger log = LoggerFactory.getLogger(UpdatesFragment.class);
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private boolean mLoading;
    private OnRequestDataListener mRequestDataListener;
    private ProfileUpdatesAdapter mAdapter = new ProfileUpdatesAdapter();

    private RequestListener<? super Uri, GlideDrawable> mImageLoaderListener = new RequestListener<Uri, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
            log.error("Failed loading picture at " + model.toString(), e);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

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
        mLoading = true;
    }

    public void setOnRequestDataListener(OnRequestDataListener listener) {
        mRequestDataListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_updates, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new UpdateItemDecoration(getActivity()));
        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 10);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    long visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!mLoading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            mLoading = true;
                            if (mRequestDataListener != null) {
                                mRequestDataListener.onRequestData();
                            }
                        }
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (mRecyclerView != null) {
            mRecyclerView.clearOnScrollListeners();
        }
        super.onDestroy();
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

    public void askForData(boolean ask) {
        mLoading = !ask;
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

    public int getNumberOfUpdates() { return mAdapter.getItemCount(); }

    public class ProfileUpdatesAdapter extends RecyclerView.Adapter<ProfileUpdatesViewHolder> {
        public final ArrayList<UpdateInfo> mUpdates = new ArrayList<>();

        private void sortData() {
            Collections.sort(mUpdates, new UpdateInfo.UpdateInfoInverseDateComparator());
        }

        public void add(UpdateInfo item) {
            mUpdates.add(item);
            sortData();
            int index = mUpdates.indexOf(item);
            notifyItemInserted(index);
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
            remove(index);
        }

        public void remove(int index) {
            mUpdates.remove(index);
            sortData();
            notifyDataSetChanged();
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

            RequestManager glide = Glide.with(UpdatesFragment.this);

            if (data.getProfilePictureUri() != null) {
                glide.load(data.getProfilePictureUri())
                        .centerCrop()
                        .listener(mImageLoaderListener)
                        .into(holder.userPictureView);

            } else {
                glide.load(R.drawable.avatar_placeholder)
                        .centerCrop()
                        .into(holder.userPictureView);
            }

            if (data.getPictureUri() != null) {
                glide.load(data.getPictureUri())
                        .centerCrop()
                        .listener(mImageLoaderListener)
                        .into(holder.attachedImageView);
            } else {
                Glide.clear(holder.attachedImageView);
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
