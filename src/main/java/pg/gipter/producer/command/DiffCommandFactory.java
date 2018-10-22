package pg.gipter.producer.command;

import pg.gipter.settings.ApplicationProperties;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem vcs, ApplicationProperties applicationProperties) {
        if (applicationProperties.codeProtection() == CodeProtection.STATEMENT) {
            return new EmptyDiffCommand(applicationProperties);
        }
        switch (vcs) {
            case MERCURIAL:
                return new MercurialDiffCommand(applicationProperties);
            case SVN:
                return new SvnDiffCommand(applicationProperties);
            case TFVC:
                return new TfvcDiffCommand(applicationProperties);
            default:
                return new GitDiffCommand(applicationProperties);
        }
    }
}
