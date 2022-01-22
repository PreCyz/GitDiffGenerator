package pg.gipter.core.producers;

import org.apache.commons.io.FileUtils;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.processor.DocumentFinder;
import pg.gipter.core.producers.processor.DocumentFinderFactory;
import pg.gipter.services.SmartZipService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class SharePointDocumentsDiffProducer extends AbstractDiffProducer {

    private final ItemType itemType;

    SharePointDocumentsDiffProducer(ItemType itemType, ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.itemType = itemType;
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }

    @Override
    public void produceDiff() {
        if (applicationProperties.projectPaths().isEmpty()) {
            logger.error("Given projects do not contains any items.");
            throw new IllegalArgumentException("Given projects do not contain any items.");
        }

        logger.info("Item type set as {}. Documents will be zipped and uploaded.", itemType);
        List<Path> documents = DocumentFinderFactory.getInstance(applicationProperties)
                .map(DocumentFinder::find)
                .orElseGet(ArrayList::new);
        if (documents.isEmpty()) {
            logger.warn("No documents to zip == no item to upload. [{}].", itemType);
            throw new IllegalArgumentException("Given projects do not contain any items.");
        }

        new SmartZipService().zipDocumentsAndWriteToFile(documents, applicationProperties.itemPath());
        if (applicationProperties.isDeleteDownloadedFiles()) {
            deleteFiles(documents);
        }
    }

    private void deleteFiles(List<Path> documents) {
        for (Path doc : documents) {
            try {
                Files.deleteIfExists(doc);
                logger.info("File [{}] deleted.", doc.getFileName().toString());
            } catch (IOException e) {
                try {
                    FileUtils.forceDelete(doc.toFile());
                } catch (IOException ioException) {
                    logger.warn("Can not delete file [{}].", doc.getFileName().toString());
                }
            }
        }
    }

}
