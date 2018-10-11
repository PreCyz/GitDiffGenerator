package pg.gipter.producer.command;

import pg.gipter.producer.ApplicationProperties;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem versionControlSystem, ApplicationProperties applicationProperties) {
        switch (versionControlSystem) {
            case MERCURIAL:
                return new MercurialDiffCommand(applicationProperties);
            case SVN:
                return new SvnDiffCommand(applicationProperties);
            default:
                return new GitDiffCommand(applicationProperties);
        }
    }
}
