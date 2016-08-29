package org.hopestarter.wallet.ui;

import android.content.Context;
import android.os.AsyncTask;

import org.hopestarter.wallet.server_api.CollectorMarkResponse;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.util.FetchResult;

/**
 * AsyncTask for fetching location marks from server
 */

public class LocationMarksFetcher extends AsyncTask<Integer, Void, FetchResult<CollectorMarkResponse>> {
    public enum Author {
        CUR_USER, // Current user
        ALL_USERS
    }

    public interface OnPostExecuteListener {
        void onPostExecute(FetchResult<CollectorMarkResponse> response);
    }

    private ServerApi mServerApi;
    private Author mAuthor;
    private OnPostExecuteListener mListener;

    public LocationMarksFetcher(Context context, Author author) {
        mServerApi = new ServerApi(context);
        mAuthor = author;
    }

    public void setListener(OnPostExecuteListener listener) {
        mListener = listener;
    }

    @Override
    protected FetchResult<CollectorMarkResponse> doInBackground(Integer... params) {
        int page = params[0];
        int pageSize = params[1];

        try {
            CollectorMarkResponse response = null;
            switch(mAuthor) {
                case CUR_USER:
                    response = mServerApi.getOwnLocationMarks(page, pageSize);
                    break;
                case ALL_USERS:
                    response = mServerApi.getWorldLocationMarks(page, pageSize);
                    break;
            }
            return new FetchResult<>(response);
        } catch (Exception e) {
            return new FetchResult<>(e);
        }
    }

    @Override
    public void onPostExecute(FetchResult<CollectorMarkResponse> response) {
        if (mListener != null) {
            mListener.onPostExecute(response);
        }
    }

    public void fetch(int page, int pageSize) {
        execute(page, pageSize);
    }
}
