package pg.gipter.settings;

public class ApplicationPropertiesFactory {

    private ApplicationPropertiesFactory() {}

    public static ApplicationProperties getInstance(String[] args) {
        ArgExtractor argExtractor = new ArgExtractor(args);
        switch (argExtractor.preferredArgSource()) {
            case FILE:
                return new FilePreferredApplicationProperties(args);
            case UI:
                return new UIPreferredApplicationProperties(args);
            case CLI:
                if (argExtractor.isUseUI()) {
                    return new UIPreferredApplicationProperties(args);
                }
                return new CliPreferredApplicationProperties(args);
            default:
                return new CliPreferredApplicationProperties(args);
        }
    }
}
