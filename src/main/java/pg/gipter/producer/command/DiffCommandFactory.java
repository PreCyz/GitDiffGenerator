package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(ApplicationProperties applicationProperties) {
        if (applicationProperties.codeProtection() == CodeProtection.STATEMENT) {
            return new EmptyDiffCommand(applicationProperties);
        }
        switch (applicationProperties.versionControlSystem()) {
            case MERCURIAL:
                return new MercurialDiffCommand(applicationProperties);
            case SVN:
                return new SvnDiffCommand(applicationProperties);
            default:
                return new GitDiffCommand(applicationProperties);
        }
    }
}
