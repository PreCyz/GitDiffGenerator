package pg.gipter.producer;

import org.apache.commons.io.FileUtils;
import pg.gipter.producer.command.UploadType;
import pg.gipter.producer.processor.DocumentFinder;
import pg.gipter.producer.processor.DocumentFinderFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;

class ToolkitDocumentsDiffProducer extends DocumentsDiffProducer {

    ToolkitDocumentsDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
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

        logger.info("Upload type set as {}. Documents will be zipped and uploaded.", UploadType.TOOLKIT_DOCS);
        DocumentFinder documentFinder = DocumentFinderFactory.getInstance(applicationProperties);
        List<File> documents = documentFinder.find();
        if (documents.isEmpty()) {
            logger.warn("No documents to zip is no item to upload. [{}].", UploadType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Given projects do not contain any items.");
        } else {
            zipDocumentsAndWriteToFile(documents);
            if (applicationProperties.isDeleteDownloadedFiles()) {
                deleteFiles(documents);
            }
        }
    }

    private void deleteFiles(List<File> documents) {
        for (File doc : documents) {
            try {
                FileUtils.forceDelete(doc);
                logger.info("File [{}] deleted.", doc.getName());
            } catch (IOException e) {
                logger.warn("Can not delete file [{}].", doc.getName());
            }
        }
    }

}
