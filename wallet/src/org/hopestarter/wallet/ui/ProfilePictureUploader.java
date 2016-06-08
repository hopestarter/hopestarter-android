package org.hopestarter.wallet.ui;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import org.hopestarter.wallet.server_api.AuthenticationFailed;
import org.hopestarter.wallet.server_api.BucketInfo;
import org.hopestarter.wallet.server_api.ForbiddenResourceException;
import org.hopestarter.wallet.server_api.NoTokenException;
import org.hopestarter.wallet.server_api.ServerApi;
import org.hopestarter.wallet.server_api.UnexpectedServerResponseException;
import org.hopestarter.wallet.server_api.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Updates the profile picture with the server
 */
public class ProfilePictureUploader {
    private static final Logger log = LoggerFactory.getLogger(ProfilePictureUploader.class);

    private String mProfilePicture;
    private ServerApi mServerApi;
    private BucketInfo mBucketInfo;
    private TransferUtility mTransferUtility;


    public ProfilePictureUploader setServerApiInstance(@NonNull ServerApi serverApi) {
        mServerApi = serverApi;
        return this;
    }

    public ProfilePictureUploader setProfilePictureUri(@NonNull String pictureUri) {
        mProfilePicture = pictureUri;
        return this;
    }

    public ProfilePictureUploader setTransferUtility(@NonNull TransferUtility transferUtility) {
        mTransferUtility = transferUtility;
        return this;
    }

    public ProfilePictureUploader setBucketInfo(@NonNull BucketInfo bucket) {
        mBucketInfo = bucket;
        return this;
    }

    public void upload() throws InterruptedException, IOException, NoTokenException, ForbiddenResourceException, UnexpectedServerResponseException, AuthenticationFailed {
        if (mServerApi == null || mProfilePicture == null || mBucketInfo == null || mTransferUtility == null) {
            throw new IllegalStateException("Make sure to call every set method with a non-null argument, before calling upload");
        }
        new ProfilePictureUploaderImpl(this).invoke();
    }

    private static class ProfilePictureUploaderImpl {
        private static final Logger log = LoggerFactory.getLogger(ProfilePictureUploaderImpl.class);

        private String mProfilePicture;
        private ServerApi mServerApi;
        private BucketInfo mBucketInfo;
        private TransferUtility mTransferUtility;

        public ProfilePictureUploaderImpl(ProfilePictureUploader builder) {
            mProfilePicture = builder.mProfilePicture;
            mServerApi = builder.mServerApi;
            mTransferUtility = builder.mTransferUtility;
            mBucketInfo = builder.mBucketInfo;
        }

        public void invoke() throws NoTokenException, IOException, AuthenticationFailed, ForbiddenResourceException, UnexpectedServerResponseException, InterruptedException {
            File pictureFile = new File(Uri.parse(mProfilePicture).getPath());

            StringBuilder uriBuilder = new StringBuilder();

            StringBuilder keyBuilder = new StringBuilder();

            keyBuilder.append(mBucketInfo.getPrefix()).append(pictureFile.getName());

            uriBuilder.append("s3://")
                    .append(mBucketInfo.getName()).append("/")
                    .append(keyBuilder.toString());

            Uri s3PictureUri = Uri.parse(uriBuilder.toString());

            final AtomicBoolean taskFinished = new AtomicBoolean(false);

            TransferObserver observer = mTransferUtility.upload(mBucketInfo.getName(), keyBuilder.toString(), pictureFile);
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
                UserInfo info = new UserInfo.Builder()
                        .setProfilePicture(s3PictureUri.toString())
                        .create();
                mServerApi.setUserInfo(info);
            }
        }

    }
}
