package pg.gipter.toolkit.sharepoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.DownloadDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

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
        httpget.addHeader("accept", "application/json;odata=verbose");
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            logIfError(result);
            EntityUtils.consume(response.getEntity());
            return result;
        }
    }

    private CredentialsProvider getCredentialsProvider(SharePointConfig sharePointConfig) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials(
                        sharePointConfig.getUsername(),
                        sharePointConfig.getPassword(),
                        sharePointConfig.getUrl(),
                        sharePointConfig.getDomain()
                )
        );
        return credentialsProvider;
    }

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        HttpGet httpget = new HttpGet(replaceSpaces(downloadDetails.getDownloadLink()));
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(downloadDetails.getSharePointConfig()))
                .build();
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
            logger.info("Request headers [{}]", requestHeaders);
            requestHeaders.forEach(httpPost::addHeader);
        }
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 204) {
                Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
                JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                EntityUtils.consume(response.getEntity());
                logIfError(result);
                return result;
            }
            return new JsonObject();
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, JsonObject jsonObject) throws IOException {
        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json;odata=nometadata");
        requestHeaders.put("Content-Type", "application/json;odata=nometadata");
        return executePOST(sharePointConfig, jsonObject, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        return executePOST(sharePointConfig, null, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.setEntity(new FileEntity(attachment, ContentType.APPLICATION_OCTET_STREAM));

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();

             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            logger.info("Response {}", response.getStatusLine());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
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
        String fullUrl = applicationProperties.toolkitRESTUrl() + applicationProperties.toolkitCopyCase() + "/_api/contextinfo";
        HttpPost httpPost = new HttpPost(fullUrl);
        httpPost.addHeader("Accept", "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());

            return result.get("d").getAsJsonObject()
                    .get("GetContextWebInformation").getAsJsonObject()
                    .get("FormDigestValue").getAsString();
        }
    }

    public String downloadPageSource(SharePointConfig sharePointConfig) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpget.addHeader("accept", "application/json;odata=verbose");
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            InputStreamReader inputStreamReader = new InputStreamReader(
                    response.getEntity().getContent(), StandardCharsets.UTF_8
            );
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            inputStreamReader.close();
            reader.close();
            EntityUtils.consume(response.getEntity());
            return sb.toString();
        }
    }
}
