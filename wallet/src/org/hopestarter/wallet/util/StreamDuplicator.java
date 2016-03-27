package org.hopestarter.wallet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Adrian on 08/03/2016.
 */
public class StreamDuplicator {
    // Used to notify copy progress to caller
    public interface OnDuplicationProgressListener {
        void onDuplicationProgress(int bytesCopied);
    }

    // Default buffer size
    public static final int BUFFER_SIZE = 4096;

    public void duplicate(InputStream is, OutputStream os) throws StreamDuplicationFailedException {
        duplicate(is, os, BUFFER_SIZE, null);
    }

    public void duplicate(InputStream is, OutputStream os, int bufferSize) throws StreamDuplicationFailedException {
        duplicate(is, os , bufferSize, null);
    }

    public void duplicate(InputStream is, OutputStream os, OnDuplicationProgressListener listener) throws StreamDuplicationFailedException {
        duplicate(is, os, BUFFER_SIZE, listener);
    }

    public void duplicate(InputStream is, OutputStream os, int bufferSize, OnDuplicationProgressListener listener) throws StreamDuplicationFailedException {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize cannot be smaller than 1");
        }

        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        try {
            while((bytesRead = read(is, bufferSize, buffer)) != -1) {
                write(os, buffer, bytesRead);

                if (listener != null) {
                    listener.onDuplicationProgress(bytesRead);
                }
            }
        } catch (IOException e) {
             throw new StreamDuplicationFailedException(e);
        }

    }

    private int read(InputStream is, int bufferSize, byte[] buffer) throws StreamReadFailureException {
        try {
            return is.read(buffer, 0, bufferSize);
        } catch (IOException e) {
            throw new StreamReadFailureException(is);
        }
    }

    private void write(OutputStream os, byte[] buffer, int bytesRead) throws StreamWriteFailureException {
        try {
            os.write(buffer, 0, bytesRead);
        } catch (IOException e) {
            throw new StreamWriteFailureException(os);
        }

    }
}
