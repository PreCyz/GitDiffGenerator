package pg.gipter.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
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
import java.util.Optional;

public class GithubService {

    public static final String GITHUB_URL= "https://github.com/PreCyz/GitDiffGenerator";

    private final ApplicationProperties applicationProperties;

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private String strippedVersion;

    public GithubService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    Optional<String> getLatestVersion() {
        Optional<String> latestVersion = Optional.empty();
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
                    JsonObject json = gson.fromJson(result.toString(), JsonObject.class);

                    latestVersion = Optional.ofNullable(json.get("tag_name").getAsString());
                }
            }
        } catch (IOException e) {
            logger.warn("Can not download latest version of application.", e);
            latestVersion = Optional.empty();
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
                    if (Integer.parseInt(newVersion[i]) > Integer.parseInt(currentVersion[i])) {
                        result = true;
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
}
