package pg.gipter.core.producer.command;

import pg.gipter.core.ApplicationProperties;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem vcs, ApplicationProperties applicationProperties) {
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            return new EmptyDiffCommand(applicationProperties);
        }
        switch (vcs) {
            case MERCURIAL:
                return new MercurialDiffCommand(applicationProperties);
            case SVN:
                return new SvnDiffCommand(applicationProperties);
            default:
                return new GitDiffCommand(applicationProperties);
        }
    }
}
