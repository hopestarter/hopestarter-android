package org.hopestarter.ui.view;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Adrian on 27/01/2016.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {
    public interface OnClickListener {
        void onImageClick(ImageViewHolder view);
    }

    public ImageView imageView;
    public Uri imageUri;

    private OnClickListener listener;

    public ImageViewHolder(View itemView, final ImageViewHolder.OnClickListener listener) {
        super(itemView);
        imageView = (ImageView)itemView;
        this.listener = listener;
        final ImageViewHolder thisInstance = this;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onImageClick(thisInstance);
                }
            }
        });
    }
}
