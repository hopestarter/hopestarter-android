package org.hopestarter.wallet.server_api;

/**
 * Created by Adrian on 12/02/2016.
 */
public class ResponseNotOkException extends Exception {
    public ResponseNotOkException(String s) {
        super(s);
    }
}
