package pg.gipter.toolkit.sharepoint;

import com.google.gson.JsonObject;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.DownloadDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpRequester extends HttpRequesterBase {

    public HttpRequester(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpget.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpget.addHeader("Cookie", sharePointConfig.getFedAuth());
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            return retriveJsonObject(response);
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> headers)
            throws IOException {
        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpPost.addHeader("Cookie", sharePointConfig.getFedAuth());
        headers.forEach(httpPost::addHeader);
        addStringEntity(jsonObject, httpPost);

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

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        HttpGet httpget = new HttpGet(replaceSpaces(downloadDetails.getDownloadLink()));
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        httpget.addHeader("Cookie", downloadDetails.getSharePointConfig().getFedAuth());
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
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

    public String requestDigest(SharePointConfig sharePointConfig) throws IOException {
        String fullUrl = applicationProperties.toolkitWSUrl() + applicationProperties.toolkitCopyCase() + "/_api/contextinfo";
        HttpPost httpPost = new HttpPost(fullUrl);
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json;odata=verbose");
        httpPost.addHeader("X-ClientService-ClientTag", "SDK-JAVA");
        httpPost.addHeader("Cookie", sharePointConfig.getFedAuth());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            JsonObject result = retriveJsonObject(response);
            return result.get("d").getAsJsonObject()
                    .get("GetContextWebInformation").getAsJsonObject()
                    .get("FormDigestValue").getAsString();
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpPost httpPost = new HttpPost(replaceSpaces(sharePointConfig.getFullRequestUrl()));
        httpPost.addHeader(HttpHeaders.ACCEPT, "application/json");
        httpPost.addHeader("X-RequestDigest", sharePointConfig.getFormDigest());
        httpPost.addHeader("Cookie", sharePointConfig.getFedAuth());
        httpPost.setEntity(new FileEntity(attachment, ContentType.APPLICATION_OCTET_STREAM));

        logger.info("Executing request {}", httpPost.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpPost)
        ) {
            return retriveJsonObject(response);
        }
    }
}
