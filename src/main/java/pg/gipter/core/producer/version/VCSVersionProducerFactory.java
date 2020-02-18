package pg.gipter.core.producer.version;

import pg.gipter.core.producer.command.VersionControlSystem;

public final class VCSVersionProducerFactory {

    private VCSVersionProducerFactory() { }

    public static VCSVersionProducer getInstance(VersionControlSystem vcs, String projectPath) {
        switch (vcs) {
            case MERCURIAL:
                return new MercurialVersionProducer(projectPath);
            case SVN:
                return new SvnVersionProducer(projectPath);
            default:
                return new GitVersionProducer(projectPath);
        }
    }
}
