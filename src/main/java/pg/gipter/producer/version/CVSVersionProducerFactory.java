package pg.gipter.producer.version;

import pg.gipter.producer.command.VersionControlSystem;

public final class CVSVersionProducerFactory {

    private CVSVersionProducerFactory() { }

    public static CVSVersionProducer getInstance(VersionControlSystem vcs, String projectPath) {
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
