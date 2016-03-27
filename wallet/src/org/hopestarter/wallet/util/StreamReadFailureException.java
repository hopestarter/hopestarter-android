package org.hopestarter.wallet.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Adrian on 18/03/2016.
 */
public class StreamReadFailureException extends IOException {
    public StreamReadFailureException(InputStream is) {
        this(is, null);
    }

    public StreamReadFailureException(InputStream is, Throwable e) {
        super("Problem ocurred trying to read from stream " + is.toString(), e);
    }
}
