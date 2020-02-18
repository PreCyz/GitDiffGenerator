package pg.gipter.core;

/** Created by Pawel Gawedzki on 06-Mar-2019. */
class UIApplicationProperties extends FileApplicationProperties {

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
        if (!containsArg(argName) && applicationConfig.getActiveTray() != null) {
            activeTray = applicationConfig.getActiveTray();
        }
        return activeTray;
    }

    @Override
    public boolean isEnableOnStartup() {
        boolean enableOnStartup = argExtractor.isEnableOnStartup();
        String argName = ArgName.enableOnStartup.name();
        if (!containsArg(argName) && applicationConfig.getEnableOnStartup() != null) {
            enableOnStartup = applicationConfig.getEnableOnStartup();
        }
        return enableOnStartup;
    }

}
