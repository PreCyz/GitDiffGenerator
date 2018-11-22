package pg.gipter.producer;

import pg.gipter.settings.ApplicationProperties;

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
