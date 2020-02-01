package pg.gipter.core.producer.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.util.concurrent.Callable;

public class GETCall implements Callable<JsonObject> {

    private String fullUrl;
    private HttpRequester httpRequester;

    public GETCall(String fullUrl, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public JsonObject call() throws Exception {
        return httpRequester.executeGET(fullUrl);
    }
}
