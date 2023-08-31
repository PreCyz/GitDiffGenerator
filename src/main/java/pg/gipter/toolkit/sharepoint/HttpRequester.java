package pg.gipter.toolkit.sharepoint;

import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequester extends HttpRequesterBase {

    public HttpRequester(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        setHeaders(sharePointConfig, httpget);
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            return retriveJsonObject(response);
        }
    }

    private void setHeaders(SharePointConfig sharePointConfig, HttpRequestBase requestBase) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        headers.put("Cookie", sharePointConfig.getFedAuth());
        addHeaders(requestBase, headers);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> headers)
            throws IOException {

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        addStringEntity(jsonObject, httpPost);
        headers = Optional.ofNullable(headers).orElseGet(HashMap::new);
        headers.put("X-RequestDigest", sharePointConfig.getFormDigest());
        setHeaders(sharePointConfig, httpPost);
        logger.info("Request headers [{}]", headers);

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 204) {
                return retriveJsonObject(response);
            }
            return new JsonObject();
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, JsonObject jsonObject) throws IOException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(HttpHeaders.ACCEPT, "application/json;odata=nometadata");
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, "application/json;odata=nometadata");
        return executePOST(sharePointConfig, jsonObject, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        return executePOST(sharePointConfig, null, requestHeaders);
    }
}
