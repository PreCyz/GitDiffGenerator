package pg.gipter.core.producer;

import pg.gipter.core.ApplicationProperties;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** Created by Pawel Gawedzki on 16-Sep-2019. */
class MacDiffProducer extends AbstractDiffProducer {

    MacDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd.stream()
                .map(value -> value.replace("\"", ""))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
