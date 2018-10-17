package pg.gipter.producer;

import pg.gipter.settings.ApplicationProperties;

import java.util.List;

class StatementDiffProducer extends AbstractDiffProducer {

    StatementDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }

    @Override
    public void produceDiff() {
        String statementPath = appProps.statementPath();
        if (statementPath.isEmpty()) {
            logger.error("Statement path is empty. Can not produce diff.");
            throw new IllegalArgumentException("Statement path is empty. Can not produce diff.");
        }
        logger.info("Code protection set as STATEMENT. Statement '{}' is threaded as a diff file.", statementPath);
    }
}
