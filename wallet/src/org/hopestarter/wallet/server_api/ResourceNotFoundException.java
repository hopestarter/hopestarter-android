package org.hopestarter.wallet.server_api;

/**
 * Created by Adrian on 27/08/2016.
 */
public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String url) {
        super("Unable to get resource at: " + url);
    }
}
