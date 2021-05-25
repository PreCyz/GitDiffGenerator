package pg.gipter.core.producers;

import org.apache.commons.io.FileUtils;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.core.producers.processor.DocumentFinder;
import pg.gipter.core.producers.processor.DocumentFinderFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class SharePointDocumentsDiffProducer extends AbstractDiffProducer {

    private final ItemType itemType;

    SharePointDocumentsDiffProducer(ItemType itemType, ApplicationProperties applicationProperties) {
        super(applicationProperties, null);
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

        zipDocumentsAndWriteToFile(documents);
        if (applicationProperties.isDeleteDownloadedFiles()) {
            deleteFiles(documents);
        }
    }

    private void zipDocumentsAndWriteToFile(List<Path> documents) {
        try (FileOutputStream fos = new FileOutputStream(applicationProperties.itemPath());
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (Path document : documents) {
                FileInputStream fis = new FileInputStream(document.toFile());
                ZipEntry zipEntry = new ZipEntry(document.getFileName().toString());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
        } catch (IOException ex) {
            String errMsg = "Could not produce diff.";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg, ex);
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
