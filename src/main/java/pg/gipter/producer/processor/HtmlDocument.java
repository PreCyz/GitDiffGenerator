package pg.gipter.producer.processor;

import java.time.LocalDateTime;

class HtmlDocument {

    private final String fileName;
    private final double version;
    private final LocalDateTime modificationDate;
    private final String link;

    HtmlDocument(String fileName, double version, LocalDateTime modificationDate, String link) {
        this.fileName = fileName;
        this.version = version;
        this.modificationDate = modificationDate;
        this.link = link;
    }

    String getTitle() {
        if (fileName.lastIndexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return getFileName();
    }

    String getFileName() {
        return fileName;
    }

    double getVersion() {
        return version;
    }

    LocalDateTime getModificationDate() {
        return modificationDate;
    }

    String getLink() {
        return link;
    }
}
