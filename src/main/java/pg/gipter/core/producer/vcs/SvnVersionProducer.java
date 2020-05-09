package pg.gipter.core.producer.vcs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

class SvnVersionProducer extends AbstractVersionProducer {

    SvnVersionProducer(String projectPath) {
        super(projectPath);
    }

    @Override
    List<String> getCommand() {
        return Stream.of("svn", "--version").collect(toCollection(LinkedList::new));
    }
}
