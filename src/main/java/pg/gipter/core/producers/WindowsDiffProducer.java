package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;

import java.util.List;

class WindowsDiffProducer extends AbstractDiffProducer {

    WindowsDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }
}
