package pg.gipter.core.producers.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.DocumentDetailsBuilder;
import pg.gipter.toolkit.dto.User;
import pg.gipter.toolkit.dto.VersionDetails;
import pg.gipter.utils.StringUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toCollection;

abstract class AbstractDocumentFinder implements DocumentFinder {

    protected Logger logger;
    protected ApplicationProperties applicationProperties;
    protected final ParallelProcessor parallelProcessor;

    AbstractDocumentFinder(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        logger = LoggerFactory.getLogger(getClass());
        parallelProcessor = new ParallelProcessor(applicationProperties);
    }

    List<JsonObject> getItems(List<SharePointConfig> sharePointConfigs) {
        return parallelProcessor.processConfigs(sharePointConfigs);
    }

    List<DocumentDetails> convertToDocumentDetails(JsonObject object) {
        JsonObject d = object.getAsJsonObject("d");
        if (d == null) {
            return Collections.emptyList();
        }
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

            User author = null;
            User modifier = null;
            int majorVersion = -1;
            int minorVersion = -1;
            String name = null;
            String serverRelativeUrl = null;
            String project = null;
            LocalDateTime timeLastModified = null;
            String fileTitle = null;
            List<VersionDetails> versionList = new LinkedList<>();

            JsonObject file = getNullForNull(jsonObject.get("File"), JsonObject.class);
            if (file != null) {
                author = convertToUser(getNullForNull(file.get("Author"), JsonObject.class));
                modifier = convertToUser(getNullForNull(file.get("ModifiedBy"), JsonObject.class));
                majorVersion = getNullForNull(file.get("MajorVersion"), Integer.class);
                minorVersion = getNullForNull(file.get("MinorVersion"), Integer.class);
                name = getNullForNull(file.get("Name"), String.class);
                serverRelativeUrl = getNullForNull(file.get("ServerRelativeUrl"), String.class);
                project = getProject(serverRelativeUrl);

                String lastModified = getNullForNull(file.get("TimeLastModified"), String.class);
                timeLastModified = null;
                if (!StringUtils.nullOrEmpty(lastModified)) {
                    timeLastModified = LocalDateTime.parse(lastModified, DateTimeFormatter.ISO_DATE_TIME);

                }
                fileTitle = getNullForNull(file.get("Title"), String.class);

                versionList = convertToVersions(getNullForNull(file.getAsJsonObject("Versions"), JsonObject.class));
                if (modifier != null) {
                    VersionDetails lastVersion = new VersionDetails(modifier, "", modified, -1, true, 0, fileRef, Double.parseDouble(currentVersion));
                    versionList.add(lastVersion);
                }
            }

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
                    .withProject(project)
                    .withTimeLastModified(timeLastModified)
                    .withFileName(fileTitle)
                    .withVersions(versionList)
                    .create();
            result.add(dd);
        }
        return result;
    }

    String getProject(String serverRelativeUrl) {
        if (StringUtils.nullOrEmpty(serverRelativeUrl)) {
            return null;
        }
        for (String listName : applicationProperties.toolkitProjectListNames()) {
            int index = serverRelativeUrl.indexOf(listName);
            if (index > 0) {
                return serverRelativeUrl.substring(0, index);
            } else {
                String[] pathArray = serverRelativeUrl.split("/");
                return String.format("/%s/%s/%s/", pathArray[1], pathArray[2], pathArray[3]);
            }
        }
        return null;
    }

    private <T> T getNullForNull(JsonElement element, Class<T> tClazz) {
        if (element != null && !(element instanceof JsonNull)) {
            if (tClazz == String.class) {
                return tClazz.cast(element.getAsString());
            } else if (tClazz == JsonObject.class) {
                return tClazz.cast(element.getAsJsonObject());
            } else if (tClazz == Integer.class) {
                return tClazz.cast(0);
            }
        }
        if (element == null && tClazz == Integer.class) {
            return tClazz.cast(0);
        }
        return null;
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

    final Map<String, String> getFilesToDownload(List<DocumentDetails> documentDetails) {
        Map<String, String> filesToDownloadMap = new HashMap<>();
        for (DocumentDetails dd : documentDetails) {
            final String author = applicationProperties.toolkitUsername();
            if (dd.getVersions().isEmpty() && dd.getLastModifier().getLoginName().equals(author)) {
                filesToDownloadMap.put(dd.getCurrentVersion() + "v-" + dd.getFileLeafRef(), getFullDownloadUrl(dd.getFileRef()));
            } else if (!dd.getVersions().isEmpty()) {
                Optional<VersionDetails> minMe;
                double minMeCurrentVersion = 0;
                do {
                    final double currentVersion = minMeCurrentVersion;
                    minMe = dd.getVersions().stream()
                            .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(author))
                            .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                            .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                            .filter(vd -> vd.getVersionLabel() > currentVersion)
                            .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));

                    if (minMe.isPresent()) {
                        final VersionDetails minMeV = minMe.get();
                        dd.getVersions().stream()
                                .filter(vd -> !vd.getCreator().getLoginName().equalsIgnoreCase(author))
                                .filter(vd -> vd.getVersionLabel() < minMeV.getVersionLabel())
                                .max(Comparator.comparingDouble(VersionDetails::getVersionLabel))
                                .ifPresent(versionDetails -> filesToDownloadMap.put(
                                        versionDetails.getVersionLabel() + "v-" + dd.getFileLeafRef(),
                                        getFullDownloadUrl(dd.getProject() + versionDetails.getDownloadUrl())
                                ));
                        double difference = 0.0;
                        Optional<VersionDetails> nextMinMe;
                        do {
                            final double diff = ++difference;
                            nextMinMe = dd.getVersions().stream()
                                    .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(author))
                                    .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                                    .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                                    .filter(vd -> vd.getVersionLabel() > minMeV.getVersionLabel())
                                    .filter(vd -> vd.getVersionLabel() - minMeV.getVersionLabel() == diff)
                                    .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));
                            if (nextMinMe.isPresent()) {
                                minMe = nextMinMe;
                            }
                        } while (nextMinMe.isPresent() &&
                                nextMinMe.get().getVersionLabel() < Double.parseDouble(dd.getCurrentVersion())
                        );

                        String downloadUrl = getFullDownloadUrl(dd.getProject() + minMe.get().getDownloadUrl());
                        if (minMe.get().getDownloadUrl().startsWith(dd.getProject())) {
                            downloadUrl = getFullDownloadUrl(minMe.get().getDownloadUrl());
                        }
                        filesToDownloadMap.put(minMe.get().getVersionLabel() + "v-my-" + dd.getFileLeafRef(), downloadUrl);

                        minMeCurrentVersion = minMe.get().getVersionLabel();
                    }
                } while (minMe.isPresent() && minMe.get().getVersionLabel() < Double.parseDouble(dd.getCurrentVersion()));
            }
        }
        return filesToDownloadMap;
    }

    String getFullDownloadUrl(String fileReference) {
        return String.format("%s%s", applicationProperties.toolkitWSUrl(), fileReference);
    }

    List<Path> downloadDocuments(List<DownloadDetails> downloadDetails) {
        if (downloadDetails.isEmpty()) {
            throw new IllegalArgumentException("No files to download.");
        }
        return parallelProcessor.downloadFiles(downloadDetails);
    }

    List<DownloadDetails> createDownloadDetails(List<SharePointConfig> sharePointConfigs, Map<String, String> filesToDownload) {
        List<DownloadDetails> result = new LinkedList<>();
        for (Map.Entry<String, String> entry : filesToDownload.entrySet()) {
            SharePointConfig sharePointConfig = sharePointConfigs.stream()
                    .filter(sc -> entry.getValue().contains(sc.getProject()))
                    .collect(toCollection(LinkedList::new))
                    .getFirst();
            result.add(new DownloadDetails(entry.getKey(), entry.getValue(), sharePointConfig));
        }
        return result;
    }

    protected String select() {
        return "$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel," +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email";
    }

    protected String filter() {
        return String.format("$filter=Modified+ge+datetime'%s'+and+Modified+le+datetime'%s'+and+FSObjType+eq+0",
                LocalDateTime.of(applicationProperties.startDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.of(applicationProperties.endDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    protected String expand() {
        return "$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy";
    }

    public abstract List<Path> find();
}
