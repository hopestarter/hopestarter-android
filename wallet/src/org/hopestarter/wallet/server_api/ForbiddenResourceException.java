package org.hopestarter.wallet.server_api;

/**
 * Created by Adrian on 18/02/2016.
 */
public class ForbiddenResourceException extends Exception {
    public ForbiddenResourceException() {
        super("Token is either not valid or the user is not authorized to access the resource");
    }
}
