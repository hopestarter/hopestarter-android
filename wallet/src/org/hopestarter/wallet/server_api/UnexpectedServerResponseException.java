package org.hopestarter.wallet.server_api;

/**
 * Created by Adrian on 18/02/2016.
 */
public class UnexpectedServerResponseException extends Exception {
    public UnexpectedServerResponseException(int code) {
        this(code, null);
    }

    public UnexpectedServerResponseException(int code, String msg) {
        super("Unexpected server response. Server response code was " + Integer.toString(code) + "\n" + msg);
    }
}
