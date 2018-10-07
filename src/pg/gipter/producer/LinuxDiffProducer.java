package pg.gipter.producer;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class LinuxDiffProducer extends AbstractDiffProducer {

    LinuxDiffProducer(String[] programParameters) {
        super(programParameters);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd.stream()
                .map(value -> value.replace("\"", ""))
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
