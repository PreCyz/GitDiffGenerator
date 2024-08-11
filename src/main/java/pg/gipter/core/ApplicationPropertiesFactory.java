package pg.gipter.core;

public class ApplicationPropertiesFactory {

    private ApplicationPropertiesFactory() {
    }

    public static ApplicationProperties getInstance(String[] args) {
        ArgExtractor argExtractor = new ArgExtractor(args);
        ApplicationProperties applicationProperties;
        switch (argExtractor.preferredArgSource()) {
            case FILE:
                applicationProperties = new FileApplicationProperties(args);
                break;
            case UI:
                applicationProperties = new UIApplicationProperties(args);
                break;
            case CLI:
                if (argExtractor.isUseUI()) {
                    applicationProperties = new UIApplicationProperties(args);
                } else {
                    applicationProperties = new CliApplicationProperties(args);
                }
                break;
            default:
                applicationProperties = new CliApplicationProperties(args);
                break;
        }
        return applicationProperties.init();
    }
}
