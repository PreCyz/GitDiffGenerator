package pg.gipter.core.producers;

import pg.gipter.core.ApplicationProperties;

import java.nio.file.Files;
import java.nio.file.Path;
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
        Path itemFile = Paths.get(applicationProperties.itemPath());
        if (!Files.exists(itemFile) || !Files.isRegularFile(itemFile)) {
            logger.error("Statement does not exists or it is not a file. Can not produce diff.");
            throw new IllegalArgumentException("Statement does not exists or it is not a file. Can not produce diff.");
        }
        logger.info("Code protection set as STATEMENT. Statement '{}' is threaded as a diff file.",
                applicationProperties.itemPath());
    }
}
