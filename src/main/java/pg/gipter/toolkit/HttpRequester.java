package pg.gipter.toolkit;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
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
        EdgeCdpClient.Response response = send(
                "GET",
                replaceSpaces(sharePointConfig.getFullRequestUrl()),
                Map.of("Accept", "application/json;odata=verbose"),
                null
        );
        JsonObject result = toJson(response);
        logIfError(result);
        return result;
    }

    private EdgeCdpClient.Response send(
            String method, String url, Map<String, String> headers, byte[] body
    ) throws IOException {
        Map<String, String> safeHeaders = Optional.ofNullable(headers).orElseGet(HashMap::new);
        Map<String, String> loggedHeaders = new HashMap<>(safeHeaders);
        loggedHeaders.replace("X-RequestDigest", "***");
        logger.info("Executing request: {} {} Headers: {}", method, url, loggedHeaders);

        EdgeCdpClient.Response response = EdgeCdpClient.request(method, url, safeHeaders, body);
        logger.info("Response: {}", response.status());
        return response;
    }

    private JsonObject toJson(EdgeCdpClient.Response response) {
        return new Gson().fromJson(new String(response.body(), StandardCharsets.UTF_8), JsonObject.class);
    }

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        EdgeCdpClient.Response response = send(
                "GET", replaceSpaces(downloadDetails.getDownloadLink()), Map.of(), null
        );
        String downloadFilePath = applicationProperties.itemPath()
                .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
        Path downloadedPath = Paths.get(downloadFilePath, downloadDetails.getFileName());
        Files.write(downloadedPath, response.body(), StandardOpenOption.CREATE_NEW);
        return downloadedPath;
    }

    public JsonObject executePOST(
            SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> requestHeaders
    ) throws IOException {
        Map<String, String> headers = new HashMap<>(Optional.ofNullable(requestHeaders).orElseGet(HashMap::new));
        headers.put("X-RequestDigest", sharePointConfig.getFormDigest());

        byte[] body = null;
        if (jsonObject != null) {
            logger.info("Request json: {}", jsonObject);
            body = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        }

        EdgeCdpClient.Response response = send(
                "POST", replaceSpaces(sharePointConfig.getFullRequestUrl()), headers, body
        );
        if (response.status() == 204) {
            return new JsonObject();
        }
        JsonObject result = toJson(response);
        logIfError(result);
        return result;
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        EdgeCdpClient.Response response = send(
                "POST",
                replaceSpaces(sharePointConfig.getFullRequestUrl()),
                Map.of(
                        "Content-Type", "application/octet-stream",
                        "Accept", "application/json",
                        "X-RequestDigest", sharePointConfig.getFormDigest()
                ),
                Files.readAllBytes(attachment.toPath())
        );
        JsonObject result = toJson(response);
        logIfError(result);
        return result;
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
        EdgeCdpClient.Response response = send(
                "POST",
                sharePointConfig.getFullRequestUrl(),
                Map.of(
                        "Accept", "application/json;odata=verbose",
                        "X-ClientService-ClientTag", "SDK-JAVA"
                ),
                null
        );
        return toJson(response).get("d").getAsJsonObject()
                .get("GetContextWebInformation").getAsJsonObject()
                .get("FormDigestValue").getAsString();
    }

    public <T> T post(String url, Map<String, String> headers, Object payload, Class<T> expectedType) throws IOException {
        EdgeCdpClient.Response response = send(
                "POST", url, headers, new Gson().toJson(payload).getBytes(StandardCharsets.UTF_8)
        );
        return fromJson(response, expectedType);
    }

    public int getForStatusCode(String url, Map<String, String> headers) throws IOException {
        return send("GET", url, headers, null).status();
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> expectedType) throws IOException {
        return fromJson(send("GET", url, headers, null), expectedType);
    }

    private <T> T fromJson(EdgeCdpClient.Response response, Class<T> expectedType) {
        return new GsonBuilder().create().fromJson(
                new String(response.body(), StandardCharsets.UTF_8), TypeToken.get(expectedType)
        );
    }

}
