package org.hopestarter.wallet.server_api;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.hopestarter.wallet.ui.UpdateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Adrian on 16/04/2016.
 */
public class LocationMarkUploader extends AsyncTask<OutboundLocationMark, Integer, Exception> {
    private static final Logger log = LoggerFactory.getLogger(LocationMarkUploader.class);

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
        ServerApi serverApi = new ServerApi(mContext);

        for(OutboundLocationMark inputLocationMark : params) {
            try {
                URI pictureUri = null;

                if (inputLocationMark.getPictures() != null && inputLocationMark.getPictures().size() != 0) {
                    UploadImageResponse uploadInfo = serverApi.requestImageUpload();

                    AmazonS3 amazonClient = new AmazonS3Client(uploadInfo.getCredentials());

                    Region region = Region.getRegion(Regions.fromName(uploadInfo.getBucket().getRegion()));
                    amazonClient.setRegion(region);

                    TransferUtility transferUtility = new TransferUtility(amazonClient, mContext);

                    BucketInfo bucket = uploadInfo.getBucket();

                    File pictureFile = new File(inputLocationMark.getPictures().get(0).getPath());

                    StringBuilder uriBuilder = new StringBuilder();

                    StringBuilder keyBuilder = new StringBuilder();

                    UUID fileUUID = UUID.randomUUID();

                    keyBuilder.append(bucket.getPrefix()).append(fileUUID.toString()).append(pictureFile.getName());

                    uriBuilder.append("s3://")
                            .append(bucket.getName()).append("/")
                            .append(keyBuilder.toString());

                    URI s3PictureUri = URI.create(uriBuilder.toString());

                    final AtomicBoolean taskFinished = new AtomicBoolean(false);

                    TransferObserver observer = transferUtility.upload(bucket.getName(), keyBuilder.toString(), pictureFile);
                    TransferListener listener = new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (state.equals(TransferState.FAILED) ||
                                    state.equals(TransferState.CANCELED) ||
                                    state.equals(TransferState.COMPLETED)) {
                                synchronized (taskFinished) {
                                    taskFinished.set(true);
                                    taskFinished.notify();
                                }
                                log.debug("picture upload finished");
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            log.error("An error has occurred while uploading picture to S3", ex);
                        }
                    };
                    observer.setTransferListener(listener);

                    synchronized (taskFinished) {
                        while (!taskFinished.get()) {
                            taskFinished.wait();
                        }
                    }

                    observer.cleanTransferListener();

                    TransferState transferState = observer.getState();

                    if (transferState == TransferState.COMPLETED) {
                        pictureUri = s3PictureUri;
                    }
                }

                ArrayList<URI> pictures = null;

                if (pictureUri != null) {
                    pictures = new ArrayList<>();
                    pictures.add(pictureUri);
                }

                OutboundLocationMark locationMark = new OutboundLocationMark(
                        inputLocationMark.getCreated(),
                        inputLocationMark.getPoint(),
                        pictures,
                        inputLocationMark.getText()
                );

                serverApi.uploadLocationMark(locationMark);
            } catch (Exception e) {
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
