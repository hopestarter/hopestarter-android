package org.hopestarter.ui;

import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.hopestarter.ui.view.ImageViewHolder;
import org.hopestarter.wallet_test.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Adrian on 25/01/2016.
 */
public class GalleryFragment extends Fragment implements ImageViewHolder.OnClickListener {
    public interface Callback {
        void onImageSelected(Uri imageUri);
    }

    private static final String TAG = "GalleryFragment";
    private RecyclerView mRecyclerView;
    private ImageGridRecyclerViewAdapter mAdapter;
    private Picasso mImageLoader;
    private Callback mCallback;

    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!(getActivity() instanceof GalleryFragment.Callback)) {
            throw new RuntimeException("Host activity must implement GalleryFragment.Callback");
        }

        mCallback = (GalleryFragment.Callback)getActivity();

        mImageLoader = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e(TAG, "Failed loading image at: " + uri.toString(), exception);
            }
        }).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView)inflater.inflate(R.layout.gallery_fragment, viewGroup, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addItemDecoration(new ImageItemDecoration());

        ArrayList<Uri> mArrayList = new ArrayList<>();
        mAdapter = new ImageGridRecyclerViewAdapter(mArrayList, this);
        mRecyclerView.setAdapter(mAdapter);


        startImageRetrieval();

        return mRecyclerView;
    }

    @Override
    public void onImageClick(ImageViewHolder holder) {
        if (mCallback != null) {
            mCallback.onImageSelected(holder.imageUri);
        }
    }

    private void startImageRetrieval() {
        AsyncTask<Void, Uri, Void> asyncImageQuery = new AsyncTask<Void, Uri, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Cursor c = MediaStore.Images.Media.query(
                        getActivity().getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Images.Media.DATA}
                );
                if (!c.moveToFirst()) {
                    c.close();
                    return null;
                }
                do {
                    int columnIndex = c.getColumnIndex(MediaStore.Images.Media.DATA);
                    Uri uri = Uri.fromFile(new File(c.getString(columnIndex)));
                    publishProgress(uri);
                    c.moveToNext();
                } while (!c.isAfterLast());
                c.close();
                return null;
            }

            @Override
            protected void onProgressUpdate(Uri... values) {
                mAdapter.add(values[0]);
            }
        };

        asyncImageQuery.execute();
    }

    private class ImageGridRecyclerViewAdapter extends RecyclerView.Adapter<ImageViewHolder>{
        private final ArrayList<Uri> mImageUris;
        private final ImageViewHolder.OnClickListener mImageClickListener;

        public ImageGridRecyclerViewAdapter(@NonNull ArrayList<Uri> imageUris, ImageViewHolder.OnClickListener listener) {
            mImageUris = imageUris;
            mImageClickListener = listener;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView iv = (ImageView)getActivity().getLayoutInflater().inflate(R.layout.gallery_image_cell, parent, false);
            //iv.setLayoutParams(new GridLayoutManager.LayoutParams(GridLayoutManager.LayoutParams.MATCH_PARENT, 300));
            return new ImageViewHolder(iv, mImageClickListener);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            Uri imageUri = mImageUris.get(position);
            mImageLoader.load(imageUri).fit().centerCrop().into(holder.imageView);
            holder.imageUri = imageUri;
        }

        public void add(Uri imageUri) {
            mImageUris.add(imageUri);
            notifyItemInserted(mImageUris.size()-1);
        }

        public void add(int position, Uri imageUri) {
            mImageUris.add(position, imageUri);
            notifyItemInserted(position);
        }

        public void remove(int position) {
            mImageUris.remove(position);
            notifyItemRemoved(position);
        }

        public void remove(Uri imageUri) {
            int index = mImageUris.indexOf(imageUri);
            mImageUris.remove(index);
            notifyItemRemoved(index);
        }

        @Override
        public int getItemCount() {
            return mImageUris.size();
        }
    }



    private class ImageItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(5, 5, 5, 5);
        }
    }
}
