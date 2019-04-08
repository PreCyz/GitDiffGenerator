package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.util.concurrent.Callable;

class DownloadFileCall implements Callable<File> {

    private String fullUrl;
    private String downloadedFileName;
    private HttpRequester httpRequester;

    DownloadFileCall(String fullUrl, String downloadedFileName, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        this.downloadedFileName = downloadedFileName;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public File call() throws Exception {
        return httpRequester.downloadFile(fullUrl, downloadedFileName);
    }
}
