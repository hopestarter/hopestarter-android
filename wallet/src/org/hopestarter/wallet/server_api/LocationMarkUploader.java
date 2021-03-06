package org.hopestarter.wallet.server_api;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.hopestarter.wallet.WalletApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LocationMarkUploader extends AsyncTask<OutboundLocationMark, Integer, Exception> {
    private static final Logger log = LoggerFactory.getLogger("LocationMarkUpl");
    private static final String TAG = "LocationMarkUpl";

    public interface UploaderListener {
        // If this method is called and ex == null everything went fine
        void onUploadCompleted(Exception ex);
    }

    private Activity mContext;
    private UploaderListener mListener;

    public LocationMarkUploader(@NonNull Activity context) {
        mContext = context;
    }

    @Override
    protected Exception doInBackground(OutboundLocationMark... params) {
        ServerApi serverApi = ((WalletApplication)mContext.getApplication()).getServerApi();

        for(OutboundLocationMark inputLocationMark : params) {
            try {
                OutboundLocationMark locationMark = new OutboundLocationMark(
                        inputLocationMark.getCreated(),
                        inputLocationMark.getPoint(),
                        null,
                        inputLocationMark.getText()
                );
                Log.i(TAG, "Sending location mark...");
                LocationMark result = serverApi.uploadLocationMark(locationMark);
                Log.i(TAG, "Location mark sent!");

                if (inputLocationMark.getPictures() != null && inputLocationMark.getPictures().size() != 0) {
                    Log.i(TAG, "Sending location mark picture...");
                    File pictureFile = new File(inputLocationMark.getPictures().get(0).getPath());
                    serverApi.uploadPictureForMark(pictureFile, result.getMarkId());
                    Log.i(TAG, "Location mark picture sent!");
                }
            } catch (Exception e) {
                Log.e(TAG, "error sending location mark", e);
                return e;
            }
        }

        return null; // Everything went well :)
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

    }

    @Override
    protected void onPostExecute(Exception e) {
        if (mListener != null) {
            mListener.onUploadCompleted(e);
        }
    }

    public void setListener(@NonNull UploaderListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }
}
