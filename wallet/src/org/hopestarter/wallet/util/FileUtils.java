package org.hopestarter.wallet.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Adrian on 17/03/2016.
 */
public class FileUtils {
    public static boolean isSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");
        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public static void closeSilently(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // Exception ignored
        }
    }

    public static void closeSilently(InputStream inputStream) {
        closeSilently((Closeable)inputStream);
    }

    public static void closeSilently(OutputStream outputStream) {
        closeSilently((Closeable)outputStream);
    }
}
