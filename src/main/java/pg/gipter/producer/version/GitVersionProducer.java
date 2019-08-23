package pg.gipter.producer.version;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

class GitVersionProducer extends AbstractVersionProducer {

    GitVersionProducer(String projectPath) {
        super(projectPath);
    }

    @Override
    List<String> getCommand() {
        return Stream.of("git", "--version").collect(toCollection(LinkedList::new));
    }
}
