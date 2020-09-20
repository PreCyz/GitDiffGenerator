package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.nio.file.Path;
import java.util.concurrent.Callable;

class DownloadFileCall implements Callable<Path> {

    private DownloadDetails downloadDetails;
    private HttpRequester httpRequester;

    DownloadFileCall(DownloadDetails downloadDetails, ApplicationProperties applicationProperties) {
        this.downloadDetails = downloadDetails;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public Path call() throws Exception {
        return httpRequester.downloadFile(downloadDetails);
    }
}
