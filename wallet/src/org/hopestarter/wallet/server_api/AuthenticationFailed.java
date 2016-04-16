package org.hopestarter.wallet.server_api;

import org.hopestarter.wallet.R;

/**
 * Created by Adrian on 23/02/2016.
 */
public class AuthenticationFailed extends Exception {
    public AuthenticationFailed() {
        this(null);
    }

    public AuthenticationFailed(String msg) {
        super("Failed to authenticate. " + msg);
    }
}
