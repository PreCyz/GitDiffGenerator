package pg.gipter.core.producer;

import org.apache.commons.io.FileUtils;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.producer.command.UploadType;
import pg.gipter.core.producer.processor.DocumentFinder;
import pg.gipter.core.producer.processor.DocumentFinderFactory;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ToolkitDocumentsDiffProducer extends AbstractDiffProducer {

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
        }

        zipDocumentsAndWriteToFile(documents);
        if (applicationProperties.isDeleteDownloadedFiles()) {
            deleteFiles(documents);
        }
    }

    private void zipDocumentsAndWriteToFile(List<File> documents) {
        try (FileOutputStream fos = new FileOutputStream(applicationProperties.itemPath());
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (File document : documents) {
                FileInputStream fis = new FileInputStream(document);
                ZipEntry zipEntry = new ZipEntry(document.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }
        } catch (IOException ex) {
            String errMsg = "Statement does not exists or it is not a file. Can not produce diff.";
            logger.error(errMsg);
            throw new IllegalArgumentException(errMsg, ex);
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
