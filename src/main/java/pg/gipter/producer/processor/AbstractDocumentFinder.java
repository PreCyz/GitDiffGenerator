package pg.gipter.producer.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.DocumentDetailsBuilder;
import pg.gipter.toolkit.dto.User;
import pg.gipter.toolkit.dto.VersionDetails;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    //for test purposes
    void setHttpRequester(HttpRequester httpRequester) {
        this.httpRequester = httpRequester;
    }

    private String getFullDownloadUrl(String fileReference, String projectCase) {
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
        if (jsonObject == null) {
            return new ArrayList<>();
        }
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

    private User convertToUser(JsonObject object) {
        if (object == null) {
            return null;
        }
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

    Map<String, String> getFilesToDownload(String project, List<DocumentDetails> documentDetails) {
        Map<String, String> filesToDownloadMap = new HashMap<>();
        for (DocumentDetails dd : documentDetails) {
            if (dd.getVersions().isEmpty() && dd.getLastModifier().getLoginName().equals(applicationProperties.toolkitUsername())) {
                filesToDownloadMap.put(dd.getCurrentVersion() + "v-" + dd.getFileLeafRef(), getFullDownloadUrl(dd.getFileRef(), project));
            } else if (!dd.getVersions().isEmpty()) {
                Optional<VersionDetails> minMe;
                double minMeCurrentVersion = 0;
                do {
                    final double currentVersion = minMeCurrentVersion;
                    minMe = dd.getVersions().stream()
                            .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                            .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                            .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                            .filter(vd -> vd.getVersionLabel() > currentVersion)
                            .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));

                    if (minMe.isPresent()) {
                        final VersionDetails minMeV = minMe.get();
                        dd.getVersions().stream()
                                .filter(vd -> !vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                                .filter(vd -> vd.getVersionLabel() < minMeV.getVersionLabel())
                                .max(Comparator.comparingDouble(VersionDetails::getVersionLabel))
                                .ifPresent(versionDetails -> filesToDownloadMap.put(
                                        versionDetails.getVersionLabel() + "v-" + dd.getFileLeafRef(),
                                        getFullDownloadUrl(versionDetails.getDownloadUrl(), project)
                                ));
                        double difference = 0.0;
                        Optional<VersionDetails> nextMinMe;
                        do {
                            final double diff = ++difference;
                            nextMinMe = dd.getVersions().stream()
                                    .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                                    .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                                    .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                                    .filter(vd -> vd.getVersionLabel() > minMeV.getVersionLabel())
                                    .filter(vd -> vd.getVersionLabel() - minMeV.getVersionLabel() == diff)
                                    .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));
                            if (nextMinMe.isPresent()) {
                                minMe = nextMinMe;
                            }
                        } while (nextMinMe.isPresent() &&
                                nextMinMe.get().getVersionLabel() < Double.valueOf(dd.getCurrentVersion())
                        );

                        filesToDownloadMap.put(
                                minMe.get().getVersionLabel() + "v-my-" + dd.getFileLeafRef(),
                                getFullDownloadUrl(minMe.get().getDownloadUrl(), project)
                        );
                        minMeCurrentVersion = minMe.get().getVersionLabel();
                    }
                } while (minMe.isPresent() && minMe.get().getVersionLabel() < Double.valueOf(dd.getCurrentVersion()));
            }
        }
        return filesToDownloadMap;
    }

    List<DocumentDetails> convertToDocumentDetails(JsonObject object) {
        JsonObject d = object.getAsJsonObject("d");
        JsonArray results = d.getAsJsonArray("results");
        List<DocumentDetails> result = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObject jsonObject = results.get(i).getAsJsonObject();

            LocalDateTime created = LocalDateTime.parse(jsonObject.get("Created").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime modified = LocalDateTime.parse(jsonObject.get("Modified").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            String fileRef = jsonObject.get("FileRef").getAsString();
            String fileLeafRef = jsonObject.get("FileLeafRef").getAsString();
            String docIcon = getNullForNull(jsonObject.get("DocIcon"), String.class);
            String currentVersion = jsonObject.get("OData__UIVersionString").getAsString();
            String guid = jsonObject.get("GUID").getAsString();
            String title = getNullForNull(jsonObject.get("Title"), String.class);

            JsonObject file = getNullForNull(jsonObject.get("File"), JsonObject.class);
            User author = convertToUser(getNullForNull(file.get("Author"), JsonObject.class));
            User modifier = convertToUser(getNullForNull(file.get("ModifiedBy"), JsonObject.class));
            int majorVersion = file.get("MajorVersion").getAsInt();
            int minorVersion = file.get("MinorVersion").getAsInt();
            String name = file.get("Name").getAsString();
            String serverRelativeUrl = file.get("ServerRelativeUrl").getAsString();
            LocalDateTime timeLastModified = LocalDateTime.parse(
                    file.get("TimeLastModified").getAsString(), DateTimeFormatter.ISO_DATE_TIME
            );
            String fileTitle = getNullForNull(file.get("Title"), String.class);

            List<VersionDetails> versionList = convertToVersions(
                    getNullForNull(file.getAsJsonObject("Versions"), JsonObject.class)
            );
            DocumentDetails dd = new DocumentDetailsBuilder()
                    .withCreated(created)
                    .withModified(modified)
                    .withFileRef(fileRef)
                    .withFileLeafRef(fileLeafRef)
                    .withDocType(docIcon)
                    .withCurrentVersion(currentVersion)
                    .withGuid(guid)
                    .withTitle(title)
                    .withAuthor(author)
                    .withModifier(modifier)
                    .withMajorVersion(majorVersion)
                    .withMinorVersion(minorVersion)
                    .withFileName(name)
                    .withServerRelativeUrl(serverRelativeUrl)
                    .withTimeLastModified(timeLastModified)
                    .withFileName(fileTitle)
                    .withVersions(versionList)
                    .create();
            result.add(dd);
        }
        return result;
    }

    private <T> T getNullForNull(JsonElement element, Class<T> tClazz) {
        if (element != null) {
            if (tClazz == String.class) {
                return (T) element.getAsString();
            } else if (tClazz == JsonObject.class) {
                return (T) element.getAsJsonObject();
            }
        }
        return null;
    }

    public abstract List<File> find();
}
