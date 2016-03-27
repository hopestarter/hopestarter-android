package org.hopestarter.wallet.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Adrian on 18/03/2016.
 */
public class StreamWriteFailureException extends IOException {
    public StreamWriteFailureException(OutputStream os) {
        this(os, null);
    }

    public StreamWriteFailureException(OutputStream os, Throwable e) {
        super("Problem occurred trying to write to stream " +os.toString(), e);
    }
}
