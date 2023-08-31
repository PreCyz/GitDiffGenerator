package pg.gipter.toolkit.sharepoint.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SmartZipService;
import pg.gipter.toolkit.sharepoint.HttpRequester;
import pg.gipter.toolkit.sharepoint.HttpRequesterNTML;
import pg.gipter.users.SuperUserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class SharePointRestClient {

    private static final Logger logger = LoggerFactory.getLogger(SharePointRestClient.class);

    private final ApplicationProperties applicationProperties;
    private final HttpRequesterNTML httpRequesterNTML;
    private final SuperUserService superUserService;
    private String formDigest;

    public SharePointRestClient(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        httpRequesterNTML = new HttpRequesterNTML(applicationProperties);
        superUserService = SuperUserService.getInstance();
    }

    public String getFormDigest() throws IOException {
        if (formDigest == null) {
            SharePointConfig sharePointConfig = new SharePointConfig(
                    superUserService.getUserName(),
                    superUserService.getPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitRESTUrl(),
                    null,
                    null
            );

            formDigest = httpRequesterNTML.requestDigest(sharePointConfig);
            logger.debug("Form digest: [{}]", formDigest);
        }
        return formDigest;
    }

    public String createItem() throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/AddValidateUpdateItemUsingPath",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject item = httpRequesterNTML.executePOST(sharePointConfig, createItemJson());

        String itemId = "";
        JsonArray value = item.get("value").getAsJsonArray();
        for (int idx = 0; idx < value.size(); ++idx) {
            JsonObject jsonObject = value.get(idx).getAsJsonObject();
            if (Boolean.parseBoolean(jsonObject.get("HasException").getAsString())) {
                String errMsg = String.format("Field name [%s], error message: [%s]",
                        jsonObject.has("FieldName"),
                        jsonObject.has("ErrorMessage")
                );
                logger.error(errMsg);
                throw new IOException(errMsg);
            }
            if (jsonObject.has("FieldName") && "Id".equals(jsonObject.get("FieldName").getAsString())) {
                itemId = jsonObject.get("FieldValue").getAsString();
                logger.info("New item created. ItemId [{}]", itemId);
            }
        }
        return itemId;
    }

    private JsonObject createItemJson() {
        LocalDate endDate = applicationProperties.endDate();
        String fileName = applicationProperties.fileName();
        String title = fileName.substring(0, fileName.indexOf("."));
        String allVcs = applicationProperties.vcsSet().stream().map(Enum::name).collect(joining(","));
        String description = description(allVcs);
        LocalDateTime submissionDate = LocalDateTime.of(endDate, LocalTime.now());

        JsonObject decodedUrl = new JsonObject();
        decodedUrl.addProperty("DecodedUrl", applicationProperties.toolkitWSUserFolder());

        JsonObject listItemCreateInfo = new JsonObject();
        listItemCreateInfo.add("FolderPath", decodedUrl);
        listItemCreateInfo.addProperty("UnderlyingObjectType", 0);

        JsonArray formValues = new JsonArray();
        JsonObject arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "Title");
        arrayElement.addProperty("FieldValue", title);
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "SubmissionDate");
        arrayElement.addProperty("FieldValue", submissionDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "Body");
        arrayElement.addProperty("FieldValue", description);
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "Employee");
        arrayElement.addProperty("FieldValue", "[{'Key':'" + applicationProperties.toolkitUsername() + "'}]");
        formValues.add(arrayElement);

        JsonObject item = new JsonObject();
        item.add("listItemCreateInfo", listItemCreateInfo);
        item.add("formValues", formValues);
        item.addProperty("bNewDocumentUpdate", false);

        return item;
    }

    private String description(String allVcs) {
        String description = String.format("%s diff file.", allVcs);
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            description = String.format("%s file.", ItemType.STATEMENT);
        } else if (applicationProperties.itemType() == ItemType.TOOLKIT_DOCS) {
            description = "Item as zipped file containing changed documents.";
        }
        return description;
    }

    public void uploadAttachment(String itemId) throws IOException {
        SmartZipService smartZipService = new SmartZipService();
        Path path = Paths.get(applicationProperties.itemPath());
        if (applicationProperties.isSmartZip()) {
            path = smartZipService.zipFile(path);
        }
        if (Files.notExists(path)) {
            String errMsg = String.format("File [%s] does not exist", path);
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }

        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)/AttachmentFiles/add(FileName='%s')",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId,
                path.getFileName().toString()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject jsonObject = httpRequesterNTML.executePOST(sharePointConfig, path.toFile());

        if (jsonObject.has("odata.error")) {
            String errMsg = jsonObject.get("odata.error").getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("value").getAsString();
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }
        logger.info("Attachment [{}] uploaded.", path);
        if (applicationProperties.isSmartZip() && smartZipService.shouldZip(path)) {
            try {
                Files.deleteIfExists(path);
                logger.info("Zipped file [{}] deleted from the hard drive.", path);
            } catch (IOException ex) {
                logger.error("Could not delete zipped file [{}].", path);
            }
        }
    }

    public Optional<String> getUserId() {
        try {
            String fullUrl = String.format("%s%s/_api/web/siteusers/getbyemail('%s')",
                    applicationProperties.toolkitRESTUrl(),
                    applicationProperties.toolkitCopyCase(),
                    applicationProperties.toolkitUserEmail()
            );
            SharePointConfig sharePointConfig = new SharePointConfig(
                    superUserService.getUserName(),
                    superUserService.getPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitRESTUrl(),
                    fullUrl,
                    getFormDigest()
            );

            JsonObject jsonObject = httpRequesterNTML.executeGET(sharePointConfig);
            if (jsonObject != null && jsonObject.has("d")) {
                String userId = jsonObject.get("d").getAsJsonObject().get("Id").getAsString();
                logger.info("UserId got from toolkit: {}", userId);
                return Optional.ofNullable(userId);
            }
        } catch (Exception ex) {
            logger.warn("Can not get user id by email");
        }
        return Optional.empty();
    }

    public void updateClassificationId(String itemId) throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                superUserService.getUserName(),
                superUserService.getPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject payload = new JsonObject();
        payload.addProperty("ClassificationId", 12);

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json;odata=verbose");
        requestHeaders.put("If-Match", "*");
        requestHeaders.put("X-HTTP-Method", "MERGE");

        JsonObject jsonObject = httpRequesterNTML.executePOST(sharePointConfig, payload, requestHeaders);
        if (jsonObject.has("odata.error")) {
            String errMsg = jsonObject.get("odata.error").getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("value").getAsString();
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }
        logger.info("Classification ID for the item [{}] updated.", itemId);
    }

    private void cleanup(String itemId) {
        try {
            String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)",
                    applicationProperties.toolkitRESTUrl(),
                    applicationProperties.toolkitCopyCase(),
                    applicationProperties.toolkitCopyListName(),
                    itemId
            );
            SharePointConfig sharePointConfig = new SharePointConfig(
                    superUserService.getUserName(),
                    superUserService.getPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitRESTUrl(),
                    fullUrl,
                    getFormDigest()
            );

            Map<String, String> requestHeaders = new LinkedHashMap<>();
            requestHeaders.put("Accept", "application/json;odata=verbose");
            requestHeaders.put("If-Match", "*");
            requestHeaders.put("X-HTTP-Method", "DELETE");
            httpRequesterNTML.executePOST(sharePointConfig, requestHeaders);
            logger.info("Cleanup done.");
        } catch (IOException ex) {
            logger.error("Problems with cleaning up.", ex);
        }
    }

}
