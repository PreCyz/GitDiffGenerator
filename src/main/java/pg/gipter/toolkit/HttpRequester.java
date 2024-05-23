package pg.gipter.toolkit;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.DownloadDetails;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class HttpRequester {

    protected final static Logger logger = LoggerFactory.getLogger(HttpRequester.class);

    private final ApplicationProperties applicationProperties;

    public HttpRequester(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private String replaceSpaces(String fileReference) {
        return fileReference.replaceAll(" ", "%20");
    }

    public JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpget.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpget.addHeader("Cookie", sharePointConfig.getFedAuth());
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpget);
             Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        ) {
            logger.info("Response {}", response.getStatusLine());
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            logIfError(result);
            EntityUtils.consume(response.getEntity());
            return result;
        }
    }

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        HttpGet httpget = new HttpGet(replaceSpaces(downloadDetails.getDownloadLink()));
        httpget.addHeader("Cookie", downloadDetails.getSharePointConfig().getFedAuth());
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {} {}", callId, response.getStatusLine());
            String downloadFilePath = applicationProperties.itemPath()
                    .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
            Path downloadedPath = Paths.get(downloadFilePath, downloadDetails.getFileName());
            Files.copy(response.getEntity().getContent(), downloadedPath);
            EntityUtils.consume(response.getEntity());
            return downloadedPath;
        }
    }

    public JsonObject executePOST(
            SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> requestHeaders
    ) throws IOException {

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));

        if (jsonObject != null) {
            logger.info("Request json: {}", jsonObject);
            httpPost.setEntity(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON));
        }

        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            Map<String, String> filteredHeaders = new HashMap<>(requestHeaders);
            filteredHeaders.replace("Cookie", "***");
            logger.info("Request headers [{}]", filteredHeaders);
            requestHeaders.forEach(httpPost::addHeader);
        }
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 204) {
                Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
                JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                reader.close();
                EntityUtils.consume(response.getEntity());
                logIfError(result);
                return result;
            }
            return new JsonObject();
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        return executePOST(sharePointConfig, null, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.addHeader("Cookie", sharePointConfig.getFedAuth());
        httpPost.setEntity(new FileEntity(attachment, ContentType.APPLICATION_OCTET_STREAM));

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpPost);
             Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        ) {
            logger.info("Response {}", response.getStatusLine());
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());
            logIfError(result);
            return result;
        }
    }

    private void logIfError(JsonObject result) {
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

    public String requestDigest(SharePointConfig sharePointConfig) throws IOException {
        HttpPost httpPost = new HttpPost(sharePointConfig.getFullRequestUrl());
        httpPost.addHeader("Accept", "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");
        httpPost.addHeader("Cookie", sharePointConfig.getFedAuth());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpPost);
             Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
        ) {
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());
            return result.get("d").getAsJsonObject()
                    .get("GetContextWebInformation").getAsJsonObject()
                    .get("FormDigestValue").getAsString();
        }
    }

    public <T> T post(String url, Map<String, String> headers, Object payload, Class<T> expectedType) throws IOException {
        HttpPost httppost = new HttpPost(url);
        headers.forEach(httppost::addHeader);
        httppost.setEntity(new StringEntity(new Gson().toJson(payload)));
        logger.info("Executing request {}", httppost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
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

    public int postForStatusCode(String url, Map<String, String> headers, Object payload) throws IOException {
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new StringEntity(new Gson().toJson(payload)));
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(httppost::addHeader);
        logger.info("Executing request {}", httppost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httppost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            EntityUtils.consume(response.getEntity());
            return response.getStatusLine().getStatusCode();
        }
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> expectedType) throws IOException {
        HttpGet httpget = new HttpGet(url);
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(httpget::addHeader);
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpget);
             InputStreamReader inputStreamReader = new InputStreamReader(
                     response.getEntity().getContent(), StandardCharsets.UTF_8
             );
        ) {
            logger.info("Response {}", response.getStatusLine());
            Gson gson = new GsonBuilder().create();
            T entity = gson.fromJson(inputStreamReader, TypeToken.get(expectedType));
            EntityUtils.consume(response.getEntity());
            return entity;
        }
    }
}
