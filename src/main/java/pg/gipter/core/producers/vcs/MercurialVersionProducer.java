package pg.gipter.core.producers.vcs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

class MercurialVersionProducer extends AbstractVersionProducer {

    MercurialVersionProducer(String projectPath) {
        super(projectPath);
    }

    @Override
    List<String> getCommand() {
        return Stream.of("hg", "--version").collect(toCollection(LinkedList::new));
    }
}
