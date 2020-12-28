package pg.gipter.jobs;

import pg.gipter.core.ApplicationProperties;

public final class JobCreatorFactory {
    private JobCreatorFactory() { }

    public static JobCreator lastItemJobCreator(ApplicationProperties applicationProperties) {
        return new LastItemJobCreator(applicationProperties);
    }

    public static JobCreator upgradeJobCreator() {
        return new UpgradeJobCreator();
    }
}
