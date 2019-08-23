package pg.gipter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.commons.io.FileUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class GithubService {

    public static final String GITHUB_URL= "https://github.com/PreCyz/GitDiffGenerator";

    private final ApplicationProperties applicationProperties;

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private String strippedVersion;
    private static JsonObject latestReleaseDetails;
    String distributionName;

    public GithubService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
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

    public void checkUpgrades() {
        if (isNewVersion()) {
            logger.info("New version available: {}.", strippedVersion);
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", strippedVersion))
                    .withLink(GITHUB_URL + "/releases/latest")
                    .withWindowType(WindowType.BROWSER_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .buildAndDisplayWindow()
            );
        }
    }

    boolean isNewVersion() {
        boolean result = false;
        Optional<String> latestVersion = getLatestVersion();
        if (latestVersion.isPresent()) {
            String version = latestVersion.get();
            String tagSuffix = "v";
            strippedVersion = version.substring(version.indexOf(tagSuffix) + 1);

            String[] newVersion = strippedVersion.split("\\.");
            String[] currentVersion = applicationProperties.version().split("\\.");

            result = newVersion.length != currentVersion.length;

            if (!result) {
                for (int i = 0; i < newVersion.length; i++) {
                    try {
                        if (Integer.parseInt(newVersion[i]) > Integer.parseInt(currentVersion[i])) {
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

    public void checkUpgradesWithPopups() {
        if (isNewVersion()) {
            logger.info("New version available: {}.", strippedVersion);
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", strippedVersion))
                    .withLink(GITHUB_URL + "/releases/latest")
                    .withWindowType(WindowType.BROWSER_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .buildAndDisplayWindow()
            );
        } else {
            logger.info("Your version is up to date: {}.", strippedVersion);
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.no.upgrade.message"))
                    .withWindowType(WindowType.CONFIRMATION_WINDOW)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withImage(ImageFile.FINGER_UP)
                    .buildAndDisplayWindow()
            );
        }
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

    Optional<String> downloadLatestDistribution(String downloadLocation) {
        if (latestReleaseDetails == null) {
            downloadLatestDistributionDetails().ifPresent(jsonObject -> latestReleaseDetails = jsonObject);
        }
        if (latestReleaseDetails != null) {
            Optional<String> downloadLink = getDownloadLink(latestReleaseDetails);
            if (downloadLink.isPresent()) {
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet request = new HttpGet(downloadLink.get());
                try {
                    HttpResponse response = httpClient.execute(request);
                    HttpEntity entity = response.getEntity();

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
                        FileUtils.copyInputStreamToFile(entity.getContent(), Paths.get(downloadLocation, distributionName).toFile());
                        return Optional.of(distributionName);
                    }
                } catch (IOException e) {
                    logger.error("Can not download latest distribution details.", e);
                    throw new IllegalStateException("Can not download latest distribution details.");
                }
            }
        } else {
            logger.error("Can not download latest distribution details.");
            throw new IllegalStateException("Can not download latest distribution details.");
        }
        return Optional.empty();
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
