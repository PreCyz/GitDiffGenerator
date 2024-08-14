package pg.gipter.toolkit;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.processor.DownloadDetails;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class HttpRequester {

    protected final static Logger logger = LoggerFactory.getLogger(HttpRequester.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private final ApplicationProperties applicationProperties;

    public HttpRequester(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private String replaceSpaces(String fileReference) {
        return fileReference.replaceAll(" ", "%20");
    }

    public JsonObject executeGET(SharePointConfig sharePointConfig) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(replaceSpaces(sharePointConfig.getFullRequestUrl())))
                .GET()
                .header("Accept", "application/json;odata=verbose")
                .header("Cookie", sharePointConfig.getFedAuth())
                .build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream inputStream = res.body();
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                logResponse(res);
                JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                logIfError(result);
                return result;
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private void logResponse(HttpResponse<?> res) {
        logger.info("Response: {} {}", res.version(), res.statusCode());
    }

    public Path downloadFile(DownloadDetails downloadDetails) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(replaceSpaces(downloadDetails.getDownloadLink())))
                .GET()
                .header("Cookie", downloadDetails.getSharePointConfig().getFedAuth())
                .build();
        logRequest(request);

        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, request.uri());

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            logResponse(res);
            String downloadFilePath = applicationProperties.itemPath()
                    .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
            Path downloadedPath = Paths.get(downloadFilePath, downloadDetails.getFileName());
            Files.copy(res.body(), downloadedPath);
            return downloadedPath;
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public JsonObject executePOST(
            SharePointConfig sharePointConfig, JsonObject jsonObject, Map<String, String> requestHeaders
    ) throws IOException {

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(replaceSpaces(sharePointConfig.getFullRequestUrl())))
                .POST(HttpRequest.BodyPublishers.noBody());

        if (jsonObject != null) {
            logger.info("Request json: {}", jsonObject);
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()));
        }

        if (requestHeaders != null) {
            requestHeaders.forEach(requestBuilder::header);
        }
        requestBuilder.header("X-RequestDigest", sharePointConfig.getFormDigest());

        HttpRequest request = requestBuilder.build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            logResponse(res);
            if (res.statusCode() != 204) {
                try (InputStream inputStream = res.body();
                     Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                    logIfError(result);
                    return result;
                }
            }
            return new JsonObject();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, Map<String, String> requestHeaders) throws IOException {
        return executePOST(sharePointConfig, null, requestHeaders);
    }

    public JsonObject executePOST(SharePointConfig sharePointConfig, File attachment) throws IOException {
        logger.info("Attachment: {}", attachment.getAbsolutePath());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(replaceSpaces(replaceSpaces(sharePointConfig.getFullRequestUrl()))))
                .POST(HttpRequest.BodyPublishers.ofFile(attachment.toPath()))
                .header("Content-Type", "application/octet-stream")
                .header("Accept", "application/json")
                .header("X-RequestDigest", sharePointConfig.getFormDigest())
                .header("Cookie", sharePointConfig.getFedAuth())
                .build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream inputStream = res.body();
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                logResponse(res);
                JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                logIfError(result);
                return result;
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sharePointConfig.getFullRequestUrl()))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json;odata=verbose")
                .header("X-ClientService-ClientTag", "SDK-JAVA")
                .header("Cookie", sharePointConfig.getFedAuth())
                .build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream inputStream = res.body();
                 Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject result = new Gson().fromJson(reader, JsonObject.class);
                return result.get("d").getAsJsonObject()
                        .get("GetContextWebInformation").getAsJsonObject()
                        .get("FormDigestValue").getAsString();
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public <T> T post(String url, Map<String, String> headers, Object payload, Class<T> expectedType) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(payload)));
        headers.forEach(builder::header);
        HttpRequest request = builder.build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream inputStream = res.body();
                 InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                logResponse(res);
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(isr, TypeToken.get(expectedType));
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public int getForStatusCode(String url, Map<String, String> headers) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(builder::header);
        HttpRequest request = builder.build();
        logRequest(request);

        try {
            HttpResponse<String> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            logResponse(res);
            return res.statusCode();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    public <T> T get(String url, Map<String, String> headers, Class<T> expectedType) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();
        Optional.ofNullable(headers).orElseGet(HashMap::new).forEach(builder::header);
        HttpRequest request = builder.build();
        logRequest(request);

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream inputStream = res.body();
                 InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                logResponse(res);
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(isr, TypeToken.get(expectedType));
            }
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
    }

    private void logRequest(HttpRequest request) {
        HashMap<String, List<String>> headers = new HashMap<>(request.headers().map());
        headers.replace("Cookie", List.of("***"));
        headers.replace("X-RequestDigest", List.of("***"));
        logger.info("Executing request: {} {} {} Headers: {}",
                request.version().map(Enum::toString).orElseGet(() -> ""),
                request.method(),
                request.uri().toString(),
                headers
        );
    }
}
