package org.hopestarter.wallet.server_api;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Adrian on 15/04/2016.
 */
public final class ISODateFormatFactory {
    public static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    }
}
