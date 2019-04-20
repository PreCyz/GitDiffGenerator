package pg.gipter.producer.processor;

import com.google.gson.JsonObject;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.util.concurrent.Callable;

class GETCall implements Callable<JsonObject> {

    private String fullUrl;
    private HttpRequester httpRequester;

    GETCall(String fullUrl, ApplicationProperties applicationProperties) {
        this.fullUrl = fullUrl;
        httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    public JsonObject call() throws Exception {
        return httpRequester.executeGET(fullUrl);
    }
}
