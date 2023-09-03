package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.util.concurrent.Callable;

class GETItemCountCall implements Callable<ItemCountResponse> {

    private final String fullUrl;
    private final CustomizedTuple customizedTuple;
    private final HttpRequester httpRequester;
    private final ApplicationProperties applicationProperties;

    GETItemCountCall(String fullUrl, CustomizedTuple customizedTuple, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        this.customizedTuple = customizedTuple;
        this.applicationProperties = applicationProperties;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public ItemCountResponse call() throws Exception {
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString()
        );
        return new ItemCountResponse(
                customizedTuple.getProject(),
                customizedTuple.getListName(),
                httpRequester.executeGET(sharePointConfig)
        );
    }
}
