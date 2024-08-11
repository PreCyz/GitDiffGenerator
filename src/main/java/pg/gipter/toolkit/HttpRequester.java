package pg.gipter.toolkit;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.*;
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
        HttpGet httpGet = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpGet.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpGet.addHeader(HttpHeaders.COOKIE, sharePointConfig.getFedAuth());
        logRequest(httpGet);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpGet, res -> {
                try (Reader reader = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                    logResponse(res);
                    JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                    logIfError(result);
                    EntityUtils.consume(res.getEntity());
                    return result;
                }
            });
        }
    }

    private void logResponse(ClassicHttpResponse res) {
        logger.info("Response: {} {} {}", res.getVersion().format(), res.getCode(), res.getReasonPhrase());
    }

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        HttpGet httpGet = new HttpGet(replaceSpaces(downloadDetails.getDownloadLink()));
        httpGet.addHeader(HttpHeaders.COOKIE, downloadDetails.getSharePointConfig().getFedAuth());
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpGet.getRequestUri());

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpGet, res -> {
                logResponse(res);
                String downloadFilePath = applicationProperties.itemPath()
                        .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
                Path downloadedPath = Paths.get(downloadFilePath, downloadDetails.getFileName());
                Files.copy(res.getEntity().getContent(), downloadedPath);
                EntityUtils.consume(res.getEntity());
                return downloadedPath;
            });
        }
    }

    public JsonObject executePOST(
            SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> requestHeaders
    ) throws IOException {

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));

        httpPost.setEntity(new StringEntity(""));
        if (jsonObject != null) {
            logger.info("Request json: {}", jsonObject);
            httpPost.setEntity(new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON));
        }

        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            Map<String, String> filteredHeaders = new HashMap<>(requestHeaders);
            filteredHeaders.replace(HttpHeaders.COOKIE, "***");
            logger.info("Request headers [{}]", filteredHeaders);
            requestHeaders.forEach(httpPost::addHeader);
        }
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());

        logRequest(httpPost);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpPost, res -> {
                logResponse(res);
                if (res.getCode() != HttpStatus.SC_NO_CONTENT) {
                    try (Reader reader = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                        JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                        reader.close();
                        EntityUtils.consume(res.getEntity());
                        logIfError(result);
                        return result;
                    }
                }
                return new JsonObject();
            });
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        return executePOST(sharePointConfig, null, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON);
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.addHeader(HttpHeaders.COOKIE, sharePointConfig.getFedAuth());
        httpPost.setEntity(new FileEntity(attachment, ContentType.APPLICATION_OCTET_STREAM));

        logRequest(httpPost);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpPost, res -> {
                try (Reader reader = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                    logResponse(res);
                    JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                    EntityUtils.consume(res.getEntity());
                    logIfError(result);
                    return result;
                }
            });
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
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");
        httpPost.addHeader(HttpHeaders.COOKIE, sharePointConfig.getFedAuth());
        httpPost.setEntity(new StringEntity(""));

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpPost, res -> {
                try (Reader reader = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                    JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                    EntityUtils.consume(res.getEntity());
                    return result.get("d").getAsJsonObject()
                            .get("GetContextWebInformation").getAsJsonObject()
                            .get("FormDigestValue").getAsString();
                }
            });
        }
    }

    public <T> T post(String url, Map<String, String> headers, Object payload, Class<T> expectedType) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        headers.forEach(httpPost::addHeader);
        httpPost.setEntity(new StringEntity(new Gson().toJson(payload)));
        logRequest(httpPost);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpPost, res -> {
                try (InputStreamReader isr = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                    logResponse(res);
                    Gson gson = new GsonBuilder().create();
                    T entity = gson.fromJson(isr, TypeToken.get(expectedType));
                    EntityUtils.consume(res.getEntity());
                    return entity;
                }
            });

        }
    }

    public int getForStatusCode(String url, Map<String, String> headers) throws IOException {
        HttpGet httppost = new HttpGet(url);
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(httppost::addHeader);
        logRequest(httppost);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httppost, res -> {
                logResponse(res);
                EntityUtils.consume(res.getEntity());
                return res.getCode();
            });
        }
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> expectedType) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(httpGet::addHeader);
        logRequest(httpGet);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpGet, res -> {
                try (InputStreamReader isr = new InputStreamReader(res.getEntity().getContent(), StandardCharsets.UTF_8)) {
                    logResponse(res);
                    Gson gson = new GsonBuilder().create();
                    T entity = gson.fromJson(isr, TypeToken.get(expectedType));
                    EntityUtils.consume(res.getEntity());
                    return entity;
                }
            });
        }
    }

    private void logRequest(HttpUriRequestBase requestBase) {
        HashMap<String, List<String>> headers = new HashMap<>(requestBase.getHeaders().map());
        headers.replace("Cookie", List.of("***"));
        logger.info("Executing request {} {} {} Headers: {}",
                requestBase.getVersion().getProtocol(),
                requestBase.getMethod(),
                requestBase.getRequestUri(),
                headers);
    }
}
