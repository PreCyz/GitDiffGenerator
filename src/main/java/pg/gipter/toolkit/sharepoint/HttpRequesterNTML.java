package pg.gipter.toolkit.sharepoint;

import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.DownloadDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class HttpRequesterNTML extends HttpRequesterBase {

    public HttpRequesterNTML(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpget.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        if (sharePointConfig.getFedAuth() != null && !sharePointConfig.getFedAuth().isEmpty()) {
            httpget.addHeader("Cookie", sharePointConfig.getFedAuth());
        }
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            return retriveJsonObject(response);
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
        requestHeaders = Optional.ofNullable(requestHeaders).orElseGet(HashMap::new);
        requestHeaders.put("X-RequestDigest", sharePointConfig.getFormDigest());
        addHeaders(httpPost, requestHeaders);
        logger.info("Request headers [{}]", requestHeaders);

        addStringEntity(jsonObject, httpPost);

        logger.info("Executing request {}", httpPost.getRequestLine());
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
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

    public void executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        executePOST(sharePointConfig, null, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json");
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.setEntity(new FileEntity(attachment, ContentType.APPLICATION_OCTET_STREAM));

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();

             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            return retriveJsonObject(response);
        }
    }

    public String requestDigest(SharePointConfig sharePointConfig) throws IOException {
        String fullUrl = applicationProperties.toolkitRESTUrl() + applicationProperties.toolkitCopyCase() + "/_api/contextinfo";
        HttpPost httpPost = new HttpPost(fullUrl);
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(sharePointConfig))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            JsonObject result = retriveJsonObject(response);
            return result.get("d").getAsJsonObject()
                    .get("GetContextWebInformation").getAsJsonObject()
                    .get("FormDigestValue").getAsString();
        }
    }
}
