package org.hopestarter.wallet.ui;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.hopestarter.wallet.server_api.AuthenticationFailed;
import org.hopestarter.wallet.server_api.BucketInfo;
import org.hopestarter.wallet.server_api.ForbiddenResourceException;
import org.hopestarter.wallet.server_api.NoTokenException;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.UnexpectedServerResponseException;
import org.hopestarter.wallet.server_api.UploadImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Adrian on 08/06/2016.
 */
public class ProfilePictureUpdater {
    private static final Logger log = LoggerFactory.getLogger(ProfilePictureUpdater.class);

    private String mProfilePicture;
    private ServerApi mServerApi;
    private Activity mActivity;
    private TransferUtility mTransferUtility;

    public ProfilePictureUpdater(@NonNull Activity activity, @NonNull ServerApi serverApi, @NonNull String profilePicture) {
        mServerApi = serverApi;
        mActivity = activity;
        mProfilePicture = profilePicture;
    }

    public void invoke() throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, InterruptedException {
        UploadImageResponse uploadInfo = mServerApi.requestImageUpload();

        AmazonS3 amazonClient = new AmazonS3Client(uploadInfo.getCredentials());
        Region region = Region.getRegion(Regions.fromName(uploadInfo.getBucket().getRegion()));
        amazonClient.setRegion(region);
        mTransferUtility = new TransferUtility(amazonClient, mActivity);

        BucketInfo bucket = uploadInfo.getBucket();

        File pictureFile = new File(Uri.parse(mProfilePicture).getPath());

        StringBuilder uriBuilder = new StringBuilder();

        StringBuilder keyBuilder = new StringBuilder();

        keyBuilder.append(bucket.getPrefix()).append(pictureFile.getName());

        uriBuilder.append("s3://")
                .append(bucket.getName()).append("/")
                .append(keyBuilder.toString());

        Uri s3PictureUri = Uri.parse(uriBuilder.toString());

        final AtomicBoolean taskFinished = new AtomicBoolean(false);

        TransferObserver observer = mTransferUtility.upload(bucket.getName(), keyBuilder.toString(), pictureFile);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.equals(TransferState.FAILED) ||
                        state.equals(TransferState.CANCELED) ||
                        state.equals(TransferState.COMPLETED)) {
                    synchronized (taskFinished) {
                        taskFinished.set(true);
                        taskFinished.notify();
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {
                log.error("An error has occurred while uploading picture to S3", ex);
            }
        });

        synchronized (taskFinished) {
            while (!taskFinished.get()) {
                taskFinished.wait();
            }
        }

        observer.cleanTransferListener();
        TransferState transferState = observer.getState();
        if (transferState == TransferState.COMPLETED) {
            mServerApi.setUserInfo(null, null, s3PictureUri.toString());
        }
    }
}
