package pg.gipter.producer.processor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.DocumentDetailsBuilder;
import pg.gipter.toolkit.dto.User;
import pg.gipter.toolkit.dto.VersionDetails;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class SimpleDocumentFinder extends AbstractDocumentFinder {

    SimpleDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<File> find() {
        try {
            Map<String, String> filesToDownload = new HashMap<>();
            for (String project : applicationProperties.projectPaths()) {
                Set<String> listTitles = applicationProperties.toolkitProjectListNames();
                for (String listTitle : listTitles) {
                    JsonObject itemsWithVersions = getItemsWithVersions(project, listTitle);
                    List<DocumentDetails> documentDetails = extractDocumentDetails(itemsWithVersions);
                    filesToDownload.putAll(getFilesToDownload(project, documentDetails));
                }
            }
            return downloadFilesFast(filesToDownload);
        } catch (IOException ex) {
            logger.warn("Can not find items to upload.", UploadType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Can not find items to upload.");
        }
    }

    Map<String, String> getFilesToDownload(String project, List<DocumentDetails> documentDetails) {
        final int firstMajorVersion = 1;
        Map<String, String> filesToDownloadMap = new HashMap<>();
        for (DocumentDetails dd : documentDetails) {
            if (dd.getLastModifier().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername())) {
                filesToDownloadMap.put("after_change-" + dd.getFileLeafRef(), getFullDownloadUrl(dd.getFileRef(), project));
                if (dd.getMajorVersion() > firstMajorVersion && !dd.getVersions().isEmpty()) {
                    dd.getVersions().stream()
                            .filter(vd -> !vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                            .max(Comparator.comparingDouble(VersionDetails::getVersionLabel))
                            .ifPresent(vd -> filesToDownloadMap.put(
                                    "before_change-" + dd.getFileLeafRef(),
                                    getFullDownloadUrl(vd.getDownloadUrl(), project)
                            ));

                }
            } else if (!dd.getVersions().isEmpty()) {
                Optional<VersionDetails> versionAfter = dd.getVersions().stream()
                        .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.of(0, 0, 0))))
                        .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                        .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                        .max(Comparator.comparingDouble(VersionDetails::getVersionLabel));
                if (versionAfter.isPresent()) {
                    filesToDownloadMap.put(
                            "after_change-" + dd.getFileLeafRef(),
                            getFullDownloadUrl(versionAfter.get().getDownloadUrl(), project)
                    );
                    dd.getVersions().stream()
                            .filter(vd -> !vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                            .filter(vd -> vd.getVersionLabel() < versionAfter.get().getVersionLabel())
                            .max(Comparator.comparingDouble(VersionDetails::getVersionLabel))
                            .ifPresent(versionDetails -> filesToDownloadMap.put(
                                    "before_change-" + dd.getFileLeafRef(),
                                    getFullDownloadUrl(versionDetails.getDownloadUrl(), project)
                            ));
                }
            }
        }
        return filesToDownloadMap;
    }

    private JsonObject getItemsWithVersions(String project, String listTitle) throws IOException {
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items",
                applicationProperties.toolkitUrl(),
                project,
                listTitle);
        String select = "$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel" +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email,File/Versions/VersionLabel";
        String filter = String.format("$filter=Modified+ge+datetime'%s'+and+Modified+le+datetime'%s'",
                LocalDateTime.of(applicationProperties.startDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.of(applicationProperties.endDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME)
        );
        String expand = "$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy";
        String fullUrl = String.format("%s?%s&%s&%s", url, select, filter, expand);

        HttpGet httpget = new HttpGet(fullUrl);
        httpget.addHeader("accept", "application/json;odata=verbose");
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider())
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());
            return result;
        }
    }

    List<DocumentDetails> extractDocumentDetails(JsonObject object) {
        JsonObject d = object.getAsJsonObject("d");
        JsonArray results = d.getAsJsonArray("results");
        List<DocumentDetails> result = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObject jsonObject = results.get(i).getAsJsonObject();

            LocalDateTime created = LocalDateTime.parse(jsonObject.get("Created").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime modified = LocalDateTime.parse(jsonObject.get("Modified").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            String fileRef = jsonObject.get("FileRef").getAsString();
            String fileLeafRef = jsonObject.get("FileLeafRef").getAsString();
            String docIcon = jsonObject.get("DocIcon").getAsString();
            String currentVersion = jsonObject.get("OData__UIVersionString").getAsString();
            String guid = jsonObject.get("GUID").getAsString();
            String title = jsonObject.get("Title").getAsString();

            JsonObject file = jsonObject.get("File").getAsJsonObject();
            User author = getUser(file.get("Author").getAsJsonObject());
            User modifier = getUser(file.get("ModifiedBy").getAsJsonObject());
            int majorVersion = file.get("MajorVersion").getAsInt();
            int minorVersion = file.get("MinorVersion").getAsInt();
            String name = file.get("Name").getAsString();
            String serverRelativeUrl = file.get("ServerRelativeUrl").getAsString();
            LocalDateTime timeLastModified = LocalDateTime.parse(file.get("TimeLastModified").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            String fileTitle = file.get("Title").getAsString();
            //String fileCurrentVersion = file.get("UIVersionLabel").getAsString();

            JsonObject versions = file.getAsJsonObject("Versions");
            List<VersionDetails> versionList = new ArrayList<>();
            if (versions != null) {
                versionList = getVersions(versions);
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
                    .withTimeLastModified(timeLastModified)
                    .withFileName(fileTitle)
                    .withVersions(versionList)
                    .create();
            result.add(dd);
        }
        return result;
    }

    private User getUser(JsonObject object) {
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

    private List<VersionDetails> getVersions(JsonObject jsonObject) {
        JsonArray results = jsonObject.getAsJsonArray("results");
        List<VersionDetails> result = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObject object = results.get(i).getAsJsonObject();
            User createdBy = getUser(object.get("CreatedBy").getAsJsonObject());
            String checkInComment = object.get("CheckInComment").getAsString();
            LocalDateTime created = LocalDateTime.parse(object.get("Created").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            int id = object.get("ID").getAsInt();
            boolean isCurrentVersion = object.get("IsCurrentVersion").getAsBoolean();
            long size = object.get("Size").getAsLong();
            String downloadUrl = object.get("Url").getAsString();
            double versionLabel = object.get("VersionLabel").getAsDouble();
            VersionDetails vd = new VersionDetails(createdBy, checkInComment, created, id, isCurrentVersion, size, downloadUrl, versionLabel);
            result.add(vd);
        }
        return result;
    }

}
