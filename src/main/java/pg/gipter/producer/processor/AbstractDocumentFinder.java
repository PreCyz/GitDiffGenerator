package pg.gipter.producer.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.User;
import pg.gipter.toolkit.dto.VersionDetails;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

abstract class AbstractDocumentFinder implements DocumentFinder {

    private final int numberOfThreads = 10;
    protected static Logger logger;
    protected ApplicationProperties applicationProperties;
    protected HttpRequester httpRequester;
    ExecutorService executor;

    AbstractDocumentFinder(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        logger = LoggerFactory.getLogger(getClass());
        this.httpRequester = new HttpRequester(applicationProperties);
        this.executor = Executors.newFixedThreadPool(numberOfThreads);
    }

    final String getFullDownloadUrl(String fileReference, String projectCase) {
        //String projectCase = "/cases/GTE440/TOEDNLD";
        if (fileReference.startsWith(projectCase)) {
            return String.format("%s%s", applicationProperties.toolkitUrl(), fileReference);
        }
        return String.format("%s%s/%s", applicationProperties.toolkitUrl(), projectCase, fileReference);
    }

    List<File> downloadDocuments(Map<String, String> filesToDownload) {
        if (filesToDownload.isEmpty()) {
            throw new IllegalArgumentException("No files to download.");
        }
        CompletionService<File> ecs = new ExecutorCompletionService<>(executor);
        filesToDownload.forEach((downloadedFileName, fullUrl) ->
                ecs.submit(new DownloadFileCall(fullUrl, downloadedFileName, applicationProperties))
        );

        int numberOfCalls = filesToDownload.size();
        List<File> result = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting torrents.", e);
            }
        }
        return result;
    }

    List<VersionDetails> convertToVersions(JsonObject jsonObject) {
        JsonArray results = jsonObject.getAsJsonArray("results");
        List<VersionDetails> result = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObject object = results.get(i).getAsJsonObject();
            User createdBy = convertToUser(object.get("CreatedBy").getAsJsonObject());
            String checkInComment = object.get("CheckInComment").getAsString();
            LocalDateTime created = LocalDateTime.parse(object.get("Created").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            int id = object.get("ID").getAsInt();
            boolean isCurrentVersion = object.get("IsCurrentVersion").getAsBoolean();
            long size = object.get("Size").getAsLong();
            String downloadUrl = object.get("Url").getAsString();
            double versionLabel = object.get("VersionLabel").getAsDouble();
            VersionDetails vd = new VersionDetails(
                    createdBy, checkInComment, created, id, isCurrentVersion, size, downloadUrl, versionLabel
            );
            result.add(vd);
        }
        return result;
    }

    User convertToUser(JsonObject object) {
        int id = -1;
        JsonElement idElement = object.get("Id");
        if (idElement != null) {
            id = idElement.getAsInt();
        }
        String fullLoginName = object.get("LoginName").getAsString();
        String loginName = fullLoginName.substring(fullLoginName.lastIndexOf("\\") + 1);
        String fullName = object.get("Title").getAsString();
        String email = object.get("Email").getAsString();
        return new User(id, loginName, fullName, email);
    }

    public abstract List<File> find();
    abstract List<DocumentDetails> convertToDocumentDetails(JsonObject jsonObject);
}
