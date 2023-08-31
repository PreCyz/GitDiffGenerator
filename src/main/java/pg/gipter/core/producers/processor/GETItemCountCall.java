package pg.gipter.core.producers.processor;

import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.toolkit.sharepoint.HttpRequesterNTML;
import pg.gipter.users.SuperUserService;

import java.util.concurrent.Callable;

class GETItemCountCall implements Callable<ItemCountResponse> {

    private final String fullUrl;
    private final CustomizedTuple customizedTuple;
    private final HttpRequesterNTML httpRequesterNTML;
    private final ApplicationProperties applicationProperties;
    private final SuperUserService superUserService;

    GETItemCountCall(String fullUrl, CustomizedTuple customizedTuple, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        this.customizedTuple = customizedTuple;

        httpRequesterNTML = new HttpRequesterNTML(applicationProperties);
        this.applicationProperties = applicationProperties;
        superUserService = SuperUserService.getInstance();
    }

    @Override
    public ItemCountResponse call() throws Exception {
        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                fullUrl
        );
        return new ItemCountResponse(
                customizedTuple.getProject(),
                customizedTuple.getListName(),
                httpRequesterNTML.executeGET(sharePointConfig)
        );
    }
}
