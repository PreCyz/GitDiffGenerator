package pg.gipter.settings;

public class ApplicationPropertiesFactory {

    private ApplicationPropertiesFactory() {}

    public static ApplicationProperties getInstance(String[] args) {
        ArgExtractor argExtractor = new ArgExtractor(args);
        if (argExtractor.preferredArgSource() == PreferredArgSource.FILE) {
            return new FilePreferredApplicationProperties(args);
        }
        return new CliPreferredApplicationProperties(args);
    }
}
