package pg.gipter.settings;

public class ApplicationPropertiesFactory {

    private ApplicationPropertiesFactory() {
    }

    public static ApplicationProperties getInstance(String[] args) {
        ArgExtractor argExtractor = new ArgExtractor(args);
        switch (argExtractor.preferredArgSource()) {
            case FILE:
                return new FileApplicationProperties(args);
            case UI:
                return new UIApplicationProperties(args);
            case CLI:
                if (argExtractor.isUseUI()) {
                    return new UIApplicationProperties(args);
                }
                return new CliApplicationProperties(args);
            default:
                return new CliApplicationProperties(args);
        }
    }
}
