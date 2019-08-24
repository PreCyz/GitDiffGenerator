package pg.gipter.settings;

import pg.gipter.utils.StringUtils;

/** Created by Pawel Gawedzki on 06-Mar-2019. */
class UIApplicationProperties extends CliApplicationProperties {

    UIApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public boolean isUseUI() {
        return true;
    }

    @Override
    public boolean isActiveTray() {
        boolean activeTray = argExtractor.isActiveTray();
        String argName = ArgName.activeTray.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            activeTray = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(activeTray)));
        }
        return activeTray;
    }

    @Override
    public boolean isEnableOnStartup() {
        boolean enableOnStartup = argExtractor.isEnableOnStartup();
        String argName = ArgName.enableOnStartup.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            enableOnStartup = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(enableOnStartup)));
        }
        return enableOnStartup;
    }

}
