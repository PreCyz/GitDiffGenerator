package pg.gipter.toolkit.sharepoint.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class SharePointRestClient {

    private static final Logger logger = LoggerFactory.getLogger(SharePointRestClient.class);

    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;
    private String formDigest;

    public SharePointRestClient(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        httpRequester = new HttpRequester(applicationProperties);
    }

    public String getFormDigest() throws IOException {
        if (formDigest == null) {
            SharePointConfig sharePointConfig = new SharePointConfig(
                    applicationProperties.toolkitUsername(),
                    applicationProperties.toolkitPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitUrl(),
                    null,
                    null
            );

            formDigest = httpRequester.requestDigest(sharePointConfig);
            logger.info("Form digest: [{}]", formDigest);
        }
        return formDigest;
    }

    public String createItem() throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/AddValidateUpdateItemUsingPath",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject item = httpRequester.executePOST(sharePointConfig, createItemJson());

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
        decodedUrl.addProperty("DecodedUrl", applicationProperties.toolkitUserFolder());

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
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)/AttachmentFiles/add(FileName='%s')",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId,
                applicationProperties.fileName()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl,
                getFormDigest()
        );

        Path path = Paths.get(applicationProperties.itemPath());
        if (Files.notExists(path)) {
            String errMsg = String.format("File [%s] does not exist", path);
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }
        JsonObject jsonObject = httpRequester.executePOST(sharePointConfig, path.toFile());

        if (jsonObject.has("odata.error")) {
            String errMsg = jsonObject.get("odata.error").getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("value").getAsString();
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }
        logger.info("Attachment [{}] uploaded.", path);
    }

    public String getAuthorId(String itemId) throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject jsonObject = httpRequester.executeGET(sharePointConfig);
        if (jsonObject != null && jsonObject.has("d")) {
            String authorId = jsonObject.get("d").getAsJsonObject().get("AuthorId").getAsString();
            logger.info("AuthorId got from toolkit: {}", authorId);
            return authorId;
        }
        cleanup(itemId);
        throw new IOException("Can not get author id for itemId: " + itemId);
    }

    public void updateAuthor(String itemId, String authorId) throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl,
                getFormDigest()
        );

        JsonObject payload = new JsonObject();
        payload.addProperty("ClassificationId", 12);
        payload.addProperty("EmployeeId", authorId);

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json;odata=verbose");
        requestHeaders.put("If-Match", "*");
        requestHeaders.put("X-HTTP-Method", "MERGE");

        JsonObject jsonObject = httpRequester.executePOST(sharePointConfig, payload, requestHeaders);
        if (jsonObject.has("odata.error")) {
            String errMsg = jsonObject.get("odata.error").getAsJsonObject()
                    .get("message").getAsJsonObject()
                    .get("value").getAsString();
            logger.error(errMsg);
            cleanup(itemId);
            throw new IOException(errMsg);
        }
        logger.info("Author [{}] for the item [{}] updated.", authorId, itemId);
    }

    private void cleanup(String itemId) {
        try {
            String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items(%s)",
                    applicationProperties.toolkitUrl(),
                    applicationProperties.toolkitCopyCase(),
                    applicationProperties.toolkitCopyListName(),
                    itemId
            );
            SharePointConfig sharePointConfig = new SharePointConfig(
                    applicationProperties.toolkitUsername(),
                    applicationProperties.toolkitPassword(),
                    applicationProperties.toolkitDomain(),
                    applicationProperties.toolkitUrl(),
                    fullUrl,
                    getFormDigest()
            );

            Map<String, String> requestHeaders = new LinkedHashMap<>();
            requestHeaders.put("Accept", "application/json;odata=verbose");
            requestHeaders.put("If-Match", "*");
            requestHeaders.put("X-HTTP-Method", "DELETE");
            httpRequester.executePOST(sharePointConfig, requestHeaders);
            logger.info("Cleanup done.");
        } catch(IOException ex) {
            logger.error("Problems with cleaning up.", ex);
        }
    }

}
