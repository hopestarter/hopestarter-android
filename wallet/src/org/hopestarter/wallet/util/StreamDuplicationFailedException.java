package org.hopestarter.wallet.util;

/**
 * Created by Adrian on 18/03/2016.
 */
public class StreamDuplicationFailedException extends Exception {
    public StreamDuplicationFailedException() {
    }

    public StreamDuplicationFailedException(String detailMessage) {
        super(detailMessage);
    }

    public StreamDuplicationFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public StreamDuplicationFailedException(Throwable throwable) {
        super(throwable);
    }
}
