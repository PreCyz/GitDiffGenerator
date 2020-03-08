package pg.gipter.core.producer.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
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

        httpRequester = new HttpRequester(applicationProperties);
        this.applicationProperties = applicationProperties;
    }

    @Override
    public ItemCountResponse call() throws Exception {
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl
        );
        return new ItemCountResponse(
                customizedTuple.getProject(),
                customizedTuple.getListName(),
                httpRequester.executeGET(sharePointConfig)
        );
    }
}
