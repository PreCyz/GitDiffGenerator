package pg.gipter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class GithubService {

    public static final String GITHUB_URL = "https://github.com/PreCyz/GitDiffGenerator";

    private final String currentVersion;

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private String strippedVersion;
    private static JsonObject latestReleaseDetails;
    String distributionName;

    public GithubService(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getStrippedVersion() {
        return strippedVersion;
    }

    Optional<String> getLatestVersion() {
        Optional<String> latestVersion = Optional.empty();

        Optional<JsonObject> latestDistroDetails = downloadLatestDistributionDetails();
        if (latestDistroDetails.isPresent()) {
            latestReleaseDetails = latestDistroDetails.get();
            latestVersion = Optional.ofNullable(latestDistroDetails.get().get("tag_name").getAsString());
        }

        return latestVersion;
    }

    public boolean isNewVersion() {
        boolean result = false;
        Optional<String> latestVersion = getLatestVersion();
        if (latestVersion.isPresent()) {
            String version = latestVersion.get();
            String tagSuffix = "v";
            strippedVersion = version.substring(version.indexOf(tagSuffix) + 1);

            String[] newVersion = strippedVersion.split("\\.");
            String[] currentVersionArray = currentVersion.split("\\.");

            result = newVersion.length != currentVersionArray.length;

            if (!result) {
                for (int i = 0; i < newVersion.length; i++) {
                    try {
                        if (Integer.parseInt(newVersion[i]) > Integer.parseInt(currentVersionArray[i])) {
                            result = true;
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        logger.warn("Versions contains more than only numbers. {}", ex.getMessage());
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    Optional<JsonObject> downloadLatestDistributionDetails() {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("https://api.github.com/repos/PreCyz/GitDiffGenerator/releases/latest");
        request.addHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json");

        try {
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try (InputStream content = response.getEntity().getContent();
                     InputStreamReader inputStreamReader = new InputStreamReader(content);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }

                    Gson gson = new Gson();
                    return Optional.ofNullable(gson.fromJson(result.toString(), JsonObject.class));
                }
            } else {
                Stream.of(response.getAllHeaders())
                        .map(Header::getElements)
                        .flatMap(Arrays::stream)
                        .forEach(headerElement -> logger.error("Name: {}, Value {}.", headerElement.getName(), headerElement.getValue()));
            }
        } catch (IOException e) {
            logger.warn("Can not download latest distribution details.", e);
        }
        return Optional.empty();

    }

    Optional<String> downloadLatestDistribution(String downloadLocation, TaskService<?> taskService) {
        if (latestReleaseDetails == null) {
            taskService.updateMsg("Downloading distribution details.");
            downloadLatestDistributionDetails().ifPresent(jsonObject -> latestReleaseDetails = jsonObject);
            taskService.updateMsg("Distribution details downloaded.");
        }
        taskService.increaseProgress();
        if (latestReleaseDetails != null) {
            Optional<String> downloadLink = getDownloadLink(latestReleaseDetails);
            if (downloadLink.isPresent()) {
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet(downloadLink.get());
                try {
                    HttpResponse response = httpClient.execute(request);
                    HttpEntity entity = response.getEntity();

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
                        downloadFile(entity, downloadLocation, taskService);
                        return Optional.of(distributionName);
                    }
                } catch (IOException e) {
                    taskService.updateMsg("Upgrade failed.");
                    taskService.workCompleted();
                    logger.error("Can not download latest distribution details.", e);
                    throw new IllegalStateException("Can not download latest distribution details.");
                }
            }
        } else {
            taskService.updateMsg("Upgrade failed.");
            taskService.workCompleted();
            logger.error("Can not download latest distribution details.");
            throw new IllegalStateException("Can not download latest distribution details.");
        }
        return Optional.empty();
    }

    private void downloadFile(HttpEntity entity, String downloadLocation, TaskService<?> taskService) throws IOException {
        taskService.updateMsg("Downloading file ...");
        long fileLength = entity.getContentLength();
        int additionalProgressNumber = 4;
        taskService.increaseProgressWithNewMax(fileLength + additionalProgressNumber);

        try (OutputStream outStream = new FileOutputStream(Paths.get(downloadLocation, distributionName).toFile());
             InputStream entityContent = entity.getContent()) {

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            long numberOfBytesDownloaded = 0;
            while ((bytesRead = entityContent.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
                numberOfBytesDownloaded += bytesRead;
                taskService.increaseProgress(numberOfBytesDownloaded);
            }
            taskService.updateMsg("File downloaded.");
        }
    }

    Optional<String> getDownloadLink(JsonObject jsonObject) {
        Optional<String> downloadLink = Optional.empty();
        String name = jsonObject.get("name").getAsString();
        JsonArray assets = jsonObject.get("assets").getAsJsonArray();
        for (JsonElement asset : assets) {
            JsonObject element = (JsonObject) asset;
            JsonElement assetName = element.get("name");
            if (assetName != null && !assetName.isJsonNull() && assetName.getAsString().startsWith(name)) {
                distributionName = assetName.getAsString();
                downloadLink = Optional.ofNullable(element.get("browser_download_url").getAsString());
                break;
            }
        }
        return downloadLink;
    }
}
