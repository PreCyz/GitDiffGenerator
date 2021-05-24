package pg.gipter.services.vcs;

import pg.gipter.core.producers.command.VersionControlSystem;

import java.util.Collections;
import java.util.List;

class MercurialService extends AbstractVcsService {

    @Override
    protected List<String> getAvailabilityCommand() {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getUserNameCommand() {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getUserEmailCommand() {
        return Collections.emptyList();
    }

    @Override
    protected VersionControlSystem getVcs() {
        return VersionControlSystem.MERCURIAL;
    }

}
