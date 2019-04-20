package pg.gipter.producer.processor;

import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.util.concurrent.Callable;

class GETItemCountCall implements Callable<ItemCountResponse> {

    private final String fullUrl;
    private final CustomizedTuple customizedTuple;
    private final HttpRequester httpRequester;

    GETItemCountCall(String fullUrl, CustomizedTuple customizedTuple, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        this.customizedTuple = customizedTuple;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public ItemCountResponse call() throws Exception {
        return new ItemCountResponse(
                customizedTuple.getProject(),
                customizedTuple.getListName(),
                httpRequester.executeGET(fullUrl)
        );
    }
}
