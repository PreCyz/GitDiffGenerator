package pg.gipter.core.producer;

import pg.gipter.core.ApplicationProperties;

import java.util.ArrayList;
import java.util.List;

class WindowsDiffProducer extends AbstractDiffProducer {

    WindowsDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        ArrayList<String> command = new ArrayList<>(diffCmd.size() + 2);
        command.add("powershell.exe");
        command.add("-Command");
        command.addAll(diffCmd);
        return command;
    }
}
