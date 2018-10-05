package pg.gipter.producer.command;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem versionControlSystem, boolean codeProtected) {
        switch (versionControlSystem) {
            case MERCURIAL:
                return new MercurialDiffCommand(codeProtected);
            default:
                return new GitDiffCommand(codeProtected);
        }
    }
}
