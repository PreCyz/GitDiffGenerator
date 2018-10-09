package pg.gipter.producer.command;

public class DiffCommandFactory {

    private DiffCommandFactory() { }

    public static DiffCommand getInstance(VersionControlSystem versionControlSystem, boolean codeProtected) {
        switch (versionControlSystem) {
            case MERCURIAL:
                return new MercurialDiffCommand(codeProtected);
            case SVN:
                return new SvnDiffCommand(codeProtected);
            default:
                return new GitDiffCommand(codeProtected);
        }
    }
}
