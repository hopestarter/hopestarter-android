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
public class LocationMarkUploader extends AsyncTask<UpdateInfo, Integer, Exception> {
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
    protected Exception doInBackground(UpdateInfo... params) {
        ServerApi serverApi = new ServerApi(mContext);

        for(UpdateInfo updateInfo : params) {
            try {
                URI pictureUri = null;

                if (updateInfo.getPictureUri() != null) {
                    UploadImageResponse uploadInfo = serverApi.requestImageUpload();

                    AmazonS3 amazonClient = new AmazonS3Client(uploadInfo.getCredentials());

                    Region region = Region.getRegion(Regions.fromName(uploadInfo.getBucket().getRegion()));
                    amazonClient.setRegion(region);

                    TransferUtility transferUtility = new TransferUtility(amazonClient, mContext);

                    BucketInfo bucket = uploadInfo.getBucket();

                    File pictureFile = new File(updateInfo.getPictureUri().getPath());

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

                Date updateDate = new Date(updateInfo.getUpdateDateMillis());
                Point point = new Point("point", new float[] {1000, 1000});

                ArrayList<URI> pictures = null;

                if (pictureUri != null) {
                    pictures = new ArrayList<>();
                    pictures.add(pictureUri);
                }

                OutboundLocationMark locationMark = new OutboundLocationMark(updateDate, point, pictures, updateInfo.getMessage());
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
