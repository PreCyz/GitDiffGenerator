package pg.gipter.producer;

import pg.gipter.producer.command.DiffCommand;
import pg.gipter.producer.command.DiffCommandFactory;

abstract class AbstractDiffProducer implements DiffProducer {
    protected final ApplicationProperties appProps;
    protected final DiffCommand diffCommand;

    AbstractDiffProducer(String[] programParameters) {
        appProps = new ApplicationProperties(programParameters).init();
        diffCommand = DiffCommandFactory.getInstance(appProps.versionControlSystem());
    }
}
