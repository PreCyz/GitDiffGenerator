package pg.gipter.producer;

import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toList;

class DocumentsDiffProducer extends AbstractDiffProducer {

    DocumentsDiffProducer(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    protected List<String> getFullCommand(List<String> diffCmd) {
        return diffCmd;
    }


    @Override
    public void produceDiff() {
        List<File> itemFiles = appProps.projectPaths().stream()
                .map(File::new)
                .filter(file -> file.exists() && file.isDirectory())
                .collect(toList());

        if (itemFiles.isEmpty()) {
            logger.error("Given projects do not contains any items.");
            throw new IllegalArgumentException("Given projects do not contain any items.");
        }

        logger.info("Upload type set as {}. Documents will be zipped and uploaded.", UploadType.DOCUMENTS);
        List<File> documents = findFiles(itemFiles);
        if (documents.isEmpty()) {
            logger.warn("No documents to zip is no item to upload.", UploadType.DOCUMENTS);
            throw new IllegalArgumentException("Given projects do not contain any items.");
        } else {
            zipDocumentsAndWriteToFile(documents);
        }
    }

    private List<File> findFiles(List<File> documentDirs) {
        return documentDirs.stream()
                .map(File::listFiles)
                .flatMap(Stream::of)
                .filter(this::isValidItem)
                .collect(toList());
    }

    private boolean isValidItem(File document) {
        LocalDate lastModifiedDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(document.lastModified()),
                TimeZone.getDefault().toZoneId()
        ).toLocalDate();
        return document.isFile() &&
                lastModifiedDate.isAfter(appProps.startDate()) &&
                (lastModifiedDate.isBefore(appProps.endDate()) || lastModifiedDate.isEqual(appProps.endDate()));
    }

    private void zipDocumentsAndWriteToFile(List<File> documents) {
        try (FileOutputStream fos = new FileOutputStream(appProps.itemPath());
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
