package pg.gipter.services.vcs;

import pg.gipter.core.producers.command.VersionControlSystem;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class GitService extends AbstractVcsService {

    @Override
    protected List<String> getAvailabilityCommand() {
        return Stream.of("git", "--version").collect(toList());
    }

    @Override
    protected List<String> getUserNameCommand() {
        return Stream.of("git", "config", "--get", "user.name").collect(toList());
    }

    @Override
    protected List<String> getUserEmailCommand() {
        return Stream.of("git", "config", "--get", "user.email").collect(toList());
    }

    @Override
    protected VersionControlSystem getVcs() {
        return VersionControlSystem.GIT;
    }
}
