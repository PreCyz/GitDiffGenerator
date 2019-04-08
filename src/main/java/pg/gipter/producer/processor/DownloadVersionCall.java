package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;

import java.util.concurrent.Callable;

class DownloadVersionCall implements Callable<CustomizedTriple> {

    private final String project;
    private final String fullUrl;
    private final String guid;
    private final HttpRequester httpRequester;

    DownloadVersionCall(String project, String fullUrl, String guid, ApplicationProperties applicationProperties) {
        this.project = project;
        this.fullUrl = fullUrl;
        this.guid = guid;
        this.httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public CustomizedTriple call() throws Exception {
        return new CustomizedTriple(project, guid, httpRequester.executeGET(fullUrl));
    }
}
