package pg.gipter.core.producer.processor;

import pg.gipter.core.model.SharePointConfig;

public class DownloadDetails {
    private String fileName;
    private String downloadLink;
    private SharePointConfig sharePointConfig;

    public DownloadDetails(String fileName, String downloadLink, SharePointConfig sharePointConfig) {
        this.fileName = fileName;
        this.downloadLink = downloadLink;
        this.sharePointConfig = sharePointConfig;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public SharePointConfig getSharePointConfig() {
        return sharePointConfig;
    }

    @Override
    public String toString() {
        return "DownloadDetails{" +
                "fileName='" + fileName + '\'' +
                ", downloadLink='" + downloadLink + '\'' +
                ", sharePointConfig=" + sharePointConfig +
                '}';
    }
}
