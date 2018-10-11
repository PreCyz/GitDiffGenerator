package pg.gipter.producer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class WindowsDiffProducer extends AbstractDiffProducer {

    WindowsDiffProducer(String[] programParameters) {
        super(programParameters);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        List<String> fullCommand = new LinkedList<>(Arrays.asList("cmd.exe", "/c"));
        fullCommand.addAll(diffCmd);
        return fullCommand;
    }
}
