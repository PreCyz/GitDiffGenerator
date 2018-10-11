package pg.gipter.producer;

import pg.gipter.settings.ApplicationProperties;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class WindowsDiffProducer extends AbstractDiffProducer {

    WindowsDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        List<String> fullCommand = new LinkedList<>(Arrays.asList("cmd.exe", "/c"));
        fullCommand.addAll(diffCmd);
        return fullCommand;
    }
}
