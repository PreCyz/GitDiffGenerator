package pg.gipter.core.producer.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.io.File;
import java.util.concurrent.Callable;

class DownloadFileCall implements Callable<File> {

    private DownloadDetails downloadDetails;
    private HttpRequester httpRequester;

    DownloadFileCall(DownloadDetails downloadDetails, ApplicationProperties applicationProperties) {
        this.downloadDetails = downloadDetails;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public File call() throws Exception {
        return httpRequester.downloadFile(downloadDetails);
    }
}
