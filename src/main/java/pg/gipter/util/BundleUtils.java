package pg.gipter.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**Created by Pawel Gawedzki on 05-Mar-2019.*/
public final class BundleUtils {

    public static final String[] SUPPORTED_LANGUAGES = {"en", "pl"};
    private static final String BUNDLE_BASE_NAME = "bundle.translation";
    private static final String REPLACEMENT_TEXT = "{}";
    private static ResourceBundle bundle;

    private BundleUtils() {}

    public static String getMsg(String key) {
        return bundle.getString(key);
    }

    public static String getMsg(String key, String ... params) {
        String message = bundle.getString(key);
        for (int i = params.length - 1; i>= 0; i--) {
            int index = message.lastIndexOf(REPLACEMENT_TEXT);
            message = String.format("%s%s%s",
                    message.substring(0, index),
                    params[i],
                    message.substring(index + REPLACEMENT_TEXT.length())
            );
        }
        return message;
    }

    public static ResourceBundle changeResource(String language) {
        if ("pl".equals(language)) {
            bundle = ResourceBundle.getBundle(String.format("%s_%s", BUNDLE_BASE_NAME, language));
        } else {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.getDefault());
        }
        return bundle;
    }

    public static ResourceBundle loadBundle() {
        return bundle = ResourceBundle.getBundle(BundleUtils.BUNDLE_BASE_NAME, Locale.getDefault());
    }
}
