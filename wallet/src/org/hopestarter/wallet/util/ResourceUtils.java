package org.hopestarter.wallet.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Created by Adrian on 09/02/2016.
 */
public class ResourceUtils {
    public static Uri resIdToUri(Context context, int resId) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getPackageName() + "/" + resId);
    }
}
