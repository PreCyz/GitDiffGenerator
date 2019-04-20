package pg.gipter.toolkit.sharepoint;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class HttpRequester {

    protected static Logger logger = LoggerFactory.getLogger(HttpRequester.class);

    private final ApplicationProperties applicationProperties;

    public HttpRequester(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private String replaceSpaces(String fileReference) {
        return fileReference.replaceAll(" ", "%20");
    }

    public JsonObject executeGET(String fullUrl) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(fullUrl));
        httpget.addHeader("accept", "application/json;odata=verbose");
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());
            return result;
        }
    }

    private CredentialsProvider getCredentialsProvider(ApplicationProperties applicationProperties) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials(
                        applicationProperties.toolkitUsername(),
                        applicationProperties.toolkitPassword(),
                        applicationProperties.toolkitUrl(),
                        applicationProperties.toolkitDomain()
                )
        );
        return credentialsProvider;
    }

    public File downloadFile(String fullUrl, String downloadedFileName) throws Exception {
        HttpGet httpget = new HttpGet(replaceSpaces(fullUrl));
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {} {}", callId, response.getStatusLine());
            String downloadFilePath = applicationProperties.itemPath()
                    .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
            File downloadedFile = new File(downloadFilePath + File.separator + downloadedFileName);
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
            EntityUtils.consume(response.getEntity());
            return downloadedFile;
        }
    }

    public JsonObject executePOST(String fullUrl, JsonObject jsonObject) throws IOException {
        logger.info("Request json: {}", jsonObject.toString());

        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        HttpPost httpPost = new HttpPost(replaceSpaces(fullUrl));
        httpPost.addHeader("Accept", "application/json;odata=nometadata");
        httpPost.addHeader("Content-Type", "application/json;odata=nometadata");
        httpPost.addHeader("Content-Length", String.valueOf(stringEntity.getContentLength()));
        httpPost.addHeader("X-RequestDigest", requestDigest());
        httpPost.setEntity(stringEntity);

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
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

    public JsonObject executePOST2010(String fullUrl, JsonObject jsonObject) throws IOException {
        logger.info("Request json: {}", jsonObject.toString());

        HttpPost httpPost = new HttpPost(replaceSpaces(fullUrl));
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(jsonObject.toString()));

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
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
        JsonElement error = result.get("error");
        if (error != null) {
            String errorMessage = error.getAsJsonObject().get("message").getAsJsonObject().get("value").getAsString();
            logger.error("Error when creating item: {}", errorMessage);
        }
    }

    public String requestDigest() throws IOException {
        String fullUrl = applicationProperties.toolkitUrl() + applicationProperties.toolkitCopyCase() + "/_api/contextinfo";
        HttpPost httpPost = new HttpPost(fullUrl);
        httpPost.addHeader("Accept", "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
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
}
