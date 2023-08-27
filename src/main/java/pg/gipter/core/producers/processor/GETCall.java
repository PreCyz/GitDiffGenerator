package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.toolkit.sharepoint.HttpRequesterBase;

import java.util.concurrent.Callable;

public class GETCall implements Callable<JsonObject> {

    private final SharePointConfig sharePointConfig;
    private final HttpRequesterBase httpRequester;

    public GETCall(SharePointConfig sharePointConfig, HttpRequesterBase httpRequester) {
        this.sharePointConfig = sharePointConfig;
        this.httpRequester = httpRequester;
    }

    @Override
    public JsonObject call() throws Exception {
        return httpRequester.executeGET(sharePointConfig);
    }
}
