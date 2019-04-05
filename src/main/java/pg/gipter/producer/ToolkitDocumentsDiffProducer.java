package pg.gipter.producer;

import org.springframework.context.ApplicationContext;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.spring.SpringInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ToolkitDocumentsDiffProducer extends AbstractDiffProducer {

    private ApplicationContext springContext;

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

        springContext = SpringInitializer.getSpringContext(applicationProperties);

        logger.info("Upload type set as {}. Documents will be zipped and uploaded.", UploadType.TOOLKIT_DOCUMENTS);
        List<File> documents = findFiles(applicationProperties.projectPaths());
        if (documents.isEmpty()) {
            logger.warn("No documents to zip is no item to upload.", UploadType.TOOLKIT_DOCUMENTS);
            throw new IllegalArgumentException("Given projects do not contain any items.");
        } else {
            zipDocumentsAndWriteToFile(documents);
        }
    }

    private List<File> findFiles(Set<String> toolkitProjects) {
        //scan toolkit
        //find items
        //download items

        return new ArrayList<>();
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

}
