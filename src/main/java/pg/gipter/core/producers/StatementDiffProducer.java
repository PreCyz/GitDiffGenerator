package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;

import java.io.File;
import java.nio.file.Paths;
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
        File itemFile = Paths.get(applicationProperties.itemPath()).toFile();
        if (!itemFile.exists() || !itemFile.isFile()) {
            logger.error("Statement does not exists or it is not a file. Can not produce diff.");
            throw new IllegalArgumentException("Statement does not exists or it is not a file. Can not produce diff.");
        }
        logger.info("Code protection set as STATEMENT. Statement '{}' is threaded as a diff file.", applicationProperties.itemPath());
    }
}