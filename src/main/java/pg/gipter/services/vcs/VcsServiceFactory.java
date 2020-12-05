package pg.gipter.services.vcs;

public final class VcsServiceFactory {
    public static VcsService getInstance() {
        return new GitService();
    }
}
