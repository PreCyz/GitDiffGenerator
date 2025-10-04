package pg.gipter.ui;

import atlantafx.base.theme.*;
import pg.gipter.utils.BundleUtils;

public enum UITheme {
    DEFAULT(null, false),
    DRACULA(new Dracula(), true),
    PRIMER_DARK(new PrimerDark(), true),
    CUPERTINO_DARK(new CupertinoDark(), true),
    NORD_DARK(new NordDark(), true),
    NORD_LIGHT(new NordLight(), false),
    PRIMER_LIGHT(new PrimerLight(), false),
    CUPERTINO_LIGHT(new CupertinoLight(), false);

    private final Theme theme;
    private final boolean darkMode;

    UITheme(Theme theme, boolean darkMode) {
        this.theme = theme;
        this.darkMode = darkMode;
    }

    public static UITheme valueOfTranslation(String translation) {
        if (DRACULA.getTranslation().equals(translation)) {
            return UITheme.DRACULA;
        } else if (PRIMER_DARK.getTranslation().equals(translation)) {
            return UITheme.PRIMER_DARK;
        } else if (CUPERTINO_DARK.getTranslation().equals(translation)) {
            return UITheme.CUPERTINO_DARK;
        } else if (NORD_DARK.getTranslation().equals(translation)) {
            return UITheme.NORD_DARK;
        } else if (NORD_LIGHT.getTranslation().equals(translation)) {
            return UITheme.NORD_LIGHT;
        } else if (PRIMER_LIGHT.getTranslation().equals(translation)) {
            return UITheme.PRIMER_LIGHT;
        } else if (CUPERTINO_LIGHT.getTranslation().equals(translation)) {
            return UITheme.CUPERTINO_LIGHT;
        }
        return UITheme.DEFAULT;
    }

    public String getTranslation() {
        return switch (this) {
            case DRACULA -> BundleUtils.getMsg("ui.theme.dracula");
            case NORD_DARK -> BundleUtils.getMsg("ui.theme.nordDark");
            case NORD_LIGHT -> BundleUtils.getMsg("ui.theme.nordLight");
            case PRIMER_DARK -> BundleUtils.getMsg("ui.theme.primerDark");
            case PRIMER_LIGHT -> BundleUtils.getMsg("ui.theme.primerLight");
            case CUPERTINO_DARK -> BundleUtils.getMsg("ui.theme.cupertinoDark");
            case CUPERTINO_LIGHT -> BundleUtils.getMsg("ui.theme.cupertinoLight");
            default -> BundleUtils.getMsg("ui.theme.default");
        };
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public String userAgentStylesheet() {
        return theme.getUserAgentStylesheet();
    }
}
