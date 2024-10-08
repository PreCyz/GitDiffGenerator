package pg.gipter.utils;

import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 05-Mar-2019. */
public final class BundleUtils {

    private enum SupportedLanguages {
        EN("en"),
        PL("pl");

        final String language;

        SupportedLanguages(String language) {
            this.language = language;
        }
    }

    private static final String BUNDLE_BASE_NAME = "bundle.translation";
    private static final String REPLACEMENT_TEXT = "{}";
    private static ResourceBundle bundle;

    private BundleUtils() {
    }

    public static String getMsg(String key, String... params) {
        if (bundle == null) {
            loadBundle();
        }
        String message = bundle.getString(key);
        for (int i = params.length - 1; i >= 0; i--) {
            int index = message.lastIndexOf(REPLACEMENT_TEXT);
            message = String.format("%s%s%s",
                    message.substring(0, index),
                    params[i],
                    message.substring(index + REPLACEMENT_TEXT.length())
            );
        }
        return message;
    }

    public static void changeBundle(String language) {
        SupportedLanguages supportedLanguage = Arrays.stream(SupportedLanguages.values())
                .filter(sl -> sl.language.equals(language))
                .findFirst()
                .orElseGet(() -> SupportedLanguages.EN);

        if (supportedLanguage == SupportedLanguages.PL) {
            bundle = ResourceBundle.getBundle(String.format("%s_%s", BUNDLE_BASE_NAME, language));
        } else {
            bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.getDefault());
        }
    }

    public static ResourceBundle loadBundle() {
        if (bundle == null) {
            try {
                return bundle = ResourceBundle.getBundle(BundleUtils.BUNDLE_BASE_NAME, Locale.getDefault());
            } catch (Exception ex) {
                LoggerFactory.getLogger(BundleUtils.class).warn("Could not load bundle {}. Default will be loaded", Locale.getDefault());
            }
            return bundle = ResourceBundle.getBundle(BundleUtils.BUNDLE_BASE_NAME);
        }
        return bundle;
    }

    public static String getDefaultLanguage() {
        return SupportedLanguages.EN.language;
    }

    public static boolean isLanguageSupported(String language) {
        return EnumSet.allOf(SupportedLanguages.class).stream().anyMatch(sl -> sl.language.equals(language));
    }

    public static List<String> getSupportedLanguages() {
        return EnumSet.allOf(SupportedLanguages.class).stream()
                .map(sl -> sl.language)
                .collect(toCollection(LinkedList::new));
    }

    public static String getBundleName() {
        return bundle.getBaseBundleName();
    }
}
