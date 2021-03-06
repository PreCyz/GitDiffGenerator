package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class LinuxDiffProducer extends AbstractDiffProducer {

    LinuxDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd.stream()
                .map(value -> value.replace("\"", ""))
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
