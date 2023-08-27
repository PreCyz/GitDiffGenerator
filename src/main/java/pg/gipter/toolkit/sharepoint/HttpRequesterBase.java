package pg.gipter.toolkit.sharepoint;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class HttpRequesterBase {

    protected final ApplicationProperties applicationProperties;
    protected final Logger logger;

    protected HttpRequesterBase(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected String replaceSpaces(String fileReference) {
        return fileReference.replaceAll(" ", "%20");
    }

    protected void logIfError(JsonObject result) {
        if (result == null) {
            logger.error("Could not get the response: response is null.");
        } else {
            JsonElement error = result.get("error");
            if (error != null) {
                String errorMessage = error.getAsJsonObject().get("message").getAsJsonObject().get("value").getAsString();
                logger.error("Error when calling Sharepoint REST API: {}", errorMessage);
            }
        }
    }

    protected JsonObject retriveJsonObject(CloseableHttpResponse response) throws IOException {
        logger.info("Response {}", response.getStatusLine());
        Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        JsonObject result = new Gson().fromJson(reader, JsonObject.class);
        logIfError(result);
        EntityUtils.consume(response.getEntity());
        return result;
    }

    public <T> T post(String url, Map<String, String> headers, Object payload, Class<T> expectedType) throws IOException {
        HttpPost httppost = new HttpPost(url);
        addHeaders(httppost, headers);
        httppost.setEntity(new StringEntity(new Gson().toJson(payload)));
        logger.info("Executing request {}", httppost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httppost);
             InputStreamReader inputStreamReader = new InputStreamReader(
                     response.getEntity().getContent(), StandardCharsets.UTF_8)
        ) {
            logger.info("Response {}", response.getStatusLine());

            Gson gson = new GsonBuilder().create();
            T entity = gson.fromJson(inputStreamReader, TypeToken.get(expectedType));

            EntityUtils.consume(response.getEntity());
            return entity;
        }
    }

    protected void addHeaders(HttpRequestBase requestBase, Map<String, String> headers) {
        headers.forEach(requestBase::addHeader);
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> expectedType) throws IOException {
        HttpGet httpget = new HttpGet(url);
        addHeaders(httpget, headers);
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            InputStreamReader inputStreamReader = new InputStreamReader(
                    response.getEntity().getContent(), StandardCharsets.UTF_8
            );
            Gson gson = new GsonBuilder().create();
            T entity = gson.fromJson(inputStreamReader, TypeToken.get(expectedType));

            inputStreamReader.close();
            EntityUtils.consume(response.getEntity());
            return entity;
        }
    }

    public int postForStatusCode(String url, Map<String, String> headers, Object payload) throws IOException {
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(new Gson().toJson(payload)));
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(httppost::addHeader);
        logger.info("Executing request {}", httppost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httppost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            EntityUtils.consume(response.getEntity());
            return response.getStatusLine().getStatusCode();
        }
    }

    protected void addStringEntity(JsonObject jsonObject, HttpPost httpPost) {
        if (jsonObject != null) {
            logger.info("Request json: {}", jsonObject);
            httpPost.setEntity(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON));
        }
    }

    public abstract JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException;
}
