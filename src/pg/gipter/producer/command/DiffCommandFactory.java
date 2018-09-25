package pg.gipter.producer.command;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem versionControlSystem) {
        switch (versionControlSystem) {
            case MERCURIAL:
                return new MercurialDiffCommand();
            default:
                return new GitDiffCommand();
        }
    }
}
