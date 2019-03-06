package pg.gipter.settings;

import pg.gipter.util.StringUtils;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
class UIPreferredApplicationProperties extends CliPreferredApplicationProperties {

    UIPreferredApplicationProperties(String[] args) {
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

}
