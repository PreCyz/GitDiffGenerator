package pg.gipter.services;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class GithubService {

    public static final String GITHUB_URL = "https://github.com/PreCyz/GitDiffGenerator";
    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    private static JsonObject latestReleaseDetails;
    private SemanticVersioning serverVersion;
    private final String githubToken;
    String distributionName;
    private final SemanticVersioning currentVersion;
    private final String JSON_TAG_NAME = "tag_name";

    public GithubService(SemanticVersioning semanticVersioning, String githubToken) {
        this.currentVersion = semanticVersioning;
        this.githubToken = githubToken;
    }

    public String getServerVersion() {
        return serverVersion.getVersion();
    }

    Optional<SemanticVersioning> getLatestVersion() {
        Optional<SemanticVersioning> latestVersion = Optional.empty();

        Optional<JsonObject> latestDistroDetails = downloadLatestDistributionDetails();
        if (latestDistroDetails.isPresent()) {
            latestReleaseDetails = latestDistroDetails.get();
            latestVersion = Optional.of(SemanticVersioning.getSemanticVersioning(
                    latestDistroDetails.get().get(JSON_TAG_NAME).getAsString()
            ));
        }

        return latestVersion;
    }

    public boolean isNewVersion() {
        boolean result = false;
        Optional<SemanticVersioning> latestVersion = getLatestVersion();
        if (latestVersion.isPresent()) {
            serverVersion = latestVersion.get();
            result = serverVersion.isNewerVersionThan(currentVersion);
        }
        return result;
    }

    Optional<JsonObject> downloadLatestDistributionDetails() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/PreCyz/GitDiffGenerator/releases/latest"))
                .GET()
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + githubToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build();

        try {
            HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() == 200) {
                try (InputStream content = res.body();
                     InputStreamReader inputStreamReader = new InputStreamReader(content);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }

                    Gson gson = new Gson();
                    Optional<JsonObject> distributionDetails = Optional.ofNullable(gson.fromJson(result.toString(), JsonObject.class));
                    if (distributionDetails.isPresent()) {
                        logger.info("Last distribution details downloaded.");
                        logger.debug("Last distribution details: {}", result);
                    } else {
                        logger.warn("Last distribution details is not available.");
                    }
                    return distributionDetails;
                }
            } else {
                res.headers()
                        .map()
                        .forEach((key, value) -> logger.error("Name: {}, Value {}.", key, value));
            }
            return Optional.empty();
        } catch (InterruptedException | IOException e) {
            logger.warn("Can not download latest distribution details.", e);
        }
        return Optional.empty();
    }

    Optional<String> downloadLatestDistribution(String downloadLocation, TaskService<?> taskService) {
        if (latestReleaseDetails == null) {
            taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.distributionDetails"));
            downloadLatestDistributionDetails().ifPresent(jsonObject -> latestReleaseDetails = jsonObject);
            taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.distributionDetails.finished"));
        }
        if (latestReleaseDetails != null) {
            Optional<String> downloadLink = getDownloadLink(latestReleaseDetails);
            if (downloadLink.isPresent()) {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(downloadLink.get()))
                        .GET()
                        .header("Accept", "application/octet-stream")
                        .header("Authorization", "Bearer " + githubToken)
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .build();

                try {
                    HttpResponse<InputStream> res = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    if (res.statusCode() == 200) {
                        downloadFile(res.body(), downloadLocation, taskService);
                        res.body().close();
                        return Optional.of(distributionName);
                    }
                    return Optional.empty();
                } catch (InterruptedException | IOException e) {
                    taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.failed"));
                    taskService.workCompleted();
                    logger.error("Can not download latest distribution details.", e);
                    throw new IllegalStateException("Can not download latest distribution details.");
                }
            }
        } else {
            taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.failed"));
            taskService.workCompleted();
            logger.error("Can not download latest distribution details.");
            throw new IllegalStateException("Can not download latest distribution details.");
        }
        return Optional.empty();
    }

    private void downloadFile(InputStream content, String downloadLocation, TaskService<?> taskService) throws IOException {
        taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.downloading", getLastVersion()));
        try (OutputStream outStream = Files.newOutputStream(Paths.get(downloadLocation, distributionName))) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            long numberOfBytesDownloaded = 0;
            while ((bytesRead = content.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
                numberOfBytesDownloaded += bytesRead;
                taskService.increaseProgress(numberOfBytesDownloaded);
            }
            taskService.updateMsg(BundleUtils.getMsg("upgrade.progress.downloaded"));
        }
    }

    Optional<String> getDownloadLink(JsonObject jsonObject) {
        Optional<String> downloadLink = Optional.empty();
        String name = jsonObject.get("name").getAsString();
        JsonArray assets = jsonObject.get("assets").getAsJsonArray();
        for (JsonElement asset : assets) {
            JsonObject element = (JsonObject) asset;
            JsonElement assetName = element.get("name");
            if (isProperAsset(name, assetName)) {
                distributionName = assetName.getAsString();
                downloadLink = Optional.ofNullable(element.get("url").getAsString());
                logger.info("New version download link: [{}]", downloadLink.orElseGet(() -> "N/A"));
                break;
            }
        }
        return downloadLink;
    }

    private boolean isProperAsset(String name, JsonElement assetName) {
        if (assetName == null) {
            return false;
        }

        final String version_1_8 = "1.8";
        final String elevenPlus = "11+";

        boolean result = !assetName.isJsonNull();
        result &= assetName.getAsString().contains(name);
        if (SystemUtils.javaVersion().startsWith(version_1_8)) {
            result &= !assetName.getAsString().startsWith(elevenPlus);
        } else {
            result &= assetName.getAsString().startsWith(elevenPlus);
        }
        return result;
    }

    private Optional<Long> getFileSize(JsonObject jsonObject) {
        Optional<Long> size = Optional.empty();
        String name = jsonObject.get("name").getAsString();
        JsonArray assets = jsonObject.get("assets").getAsJsonArray();
        for (JsonElement asset : assets) {
            JsonObject element = (JsonObject) asset;
            JsonElement assetName = element.get("name");
            if (isProperAsset(name, assetName)) {
                distributionName = assetName.getAsString();
                size = Optional.of(element.get("size").getAsLong());
                logger.info("New version file size: [{}]", size.orElseGet(() -> 0L));
                break;
            }
        }
        return size;
    }

    public Optional<String> getReleaseNotes() {
        if (latestReleaseDetails != null) {
            return Optional.ofNullable(latestReleaseDetails.get("body").getAsString());
        }
        return Optional.empty();
    }

    Optional<Long> getFileSize() {
        return getFileSize(latestReleaseDetails);
    }

    private String getLastVersion() {
        if (latestReleaseDetails != null && latestReleaseDetails.get(JSON_TAG_NAME) != null) {
            serverVersion = SemanticVersioning.getSemanticVersioning(latestReleaseDetails.get(JSON_TAG_NAME).getAsString());
        }
        return serverVersion.getVersion();
    }
}
