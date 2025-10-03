package pg.gipter.ui;

import atlantafx.base.theme.*;
import pg.gipter.utils.BundleUtils;

public enum UITheme {
    DEFAULT(null, BundleUtils.getMsg("ui.theme.default"), false),
    DRACULA(new Dracula(), BundleUtils.getMsg("ui.theme.dracula"), true),
    PRIMER_DARK(new PrimerDark(), BundleUtils.getMsg("ui.theme.primerDark"), true),
    CUPERTINO_DARK(new CupertinoDark(), BundleUtils.getMsg("ui.theme.cupertinoDark"), true),
    NORD_DARK(new NordDark(), BundleUtils.getMsg("ui.theme.nordDark"), true),
    NORD_LIGHT(new NordLight(), BundleUtils.getMsg("ui.theme.nordLight"), false),
    PRIMER_LIGHT(new PrimerLight(), BundleUtils.getMsg("ui.theme.primerLight"), false),
    CUPERTINO_LIGHT(new CupertinoLight(), BundleUtils.getMsg("ui.theme.cupertinoLight"), false);

    private final Theme theme;
    private final String value;
    private final boolean darkMode;

    UITheme(Theme theme, String value, boolean darkMode) {
        this.theme = theme;
        this.value = value;
        this.darkMode = darkMode;
    }

    public static UITheme valueFromKey(String argumentValue) {
        if (DRACULA.value.equals(argumentValue)) {
            return UITheme.DRACULA;
        } else if (PRIMER_DARK.value.equals(argumentValue)) {
            return UITheme.PRIMER_DARK;
        } else if (CUPERTINO_DARK.value.equals(argumentValue)) {
            return UITheme.CUPERTINO_DARK;
        } else if (NORD_DARK.value.equals(argumentValue)) {
            return UITheme.NORD_DARK;
        } else if (NORD_LIGHT.value.equals(argumentValue)) {
            return UITheme.NORD_LIGHT;
        } else if (PRIMER_LIGHT.value.equals(argumentValue)) {
            return UITheme.PRIMER_LIGHT;
        } else if (CUPERTINO_LIGHT.value.equals(argumentValue)) {
            return UITheme.CUPERTINO_LIGHT;
        }
        return UITheme.DEFAULT;
    }

    public String value() {
        return value;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public String userAgentStylesheet() {
        return theme.getUserAgentStylesheet();
    }
}
