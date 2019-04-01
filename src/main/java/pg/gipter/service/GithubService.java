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
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.BundleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class GithubService {

    private final ApplicationProperties applicationProperties;

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    public GithubService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private Optional<String> getLatestVersion() {
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
        Optional<String> latestVersion = getLatestVersion();
        if (latestVersion.isPresent()) {
            String version = latestVersion.get();
            String tagSuffix = "v";
            String strippedVersion = version.substring(version.indexOf(tagSuffix) + 1);

            String[] newVersion = strippedVersion.split("\\.");
            String[] currentVersion = applicationProperties.version().split("\\.");

            boolean showUpgradeWindow = newVersion.length != currentVersion.length;

            if (!showUpgradeWindow) {
                for (int i = 0; i < newVersion.length; i++) {
                    if (Integer.valueOf(newVersion[i]) > Integer.valueOf(currentVersion[i])) {
                        showUpgradeWindow = true;
                        break;
                    }
                }
            }

            if (showUpgradeWindow) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withMessage(BundleUtils.getMsg("upgrade.message", strippedVersion))
                        .withLink("https://github.com/PreCyz/GitDiffGenerator/releases/latest")
                        .withWindowType(WindowType.BROWSER_WINDOW)
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .buildAndDisplayWindow()
                );
            }
        }
    }
}
