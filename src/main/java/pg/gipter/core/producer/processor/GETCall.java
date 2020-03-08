package pg.gipter.core.producer.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.util.concurrent.Callable;

public class GETCall implements Callable<JsonObject> {

    private SharePointConfig sharePointConfig;
    private HttpRequester httpRequester;

    public GETCall(SharePointConfig sharePointConfig, ApplicationProperties applicationProperties) {
        this.sharePointConfig = sharePointConfig;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public JsonObject call() throws Exception {
        return httpRequester.executeGET(sharePointConfig);
    }
}
