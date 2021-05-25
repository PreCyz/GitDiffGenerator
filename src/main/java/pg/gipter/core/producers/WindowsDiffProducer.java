package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;

import java.util.List;
import java.util.concurrent.Executor;

class WindowsDiffProducer extends AbstractDiffProducer {

    WindowsDiffProducer(ApplicationProperties applicationProperties, Executor executor) {
        super(applicationProperties, executor);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }
}
