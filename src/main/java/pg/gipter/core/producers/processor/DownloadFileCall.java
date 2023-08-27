package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequesterNTML;

import java.nio.file.Path;
import java.util.concurrent.Callable;

class DownloadFileCall implements Callable<Path> {

    private DownloadDetails downloadDetails;
    private HttpRequesterNTML httpRequesterNTML;

    DownloadFileCall(DownloadDetails downloadDetails, ApplicationProperties applicationProperties) {
        this.downloadDetails = downloadDetails;
        httpRequesterNTML = new HttpRequesterNTML(applicationProperties);
    }

    @Override
    public Path call() throws Exception {
        return httpRequesterNTML.downloadFile(downloadDetails);
    }
}
