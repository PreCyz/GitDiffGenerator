package pg.gipter.services;

import com.google.gson.*;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.dto.*;
import pg.gipter.toolkit.HttpRequester;
import pg.gipter.utils.BundleUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/** Created by Pawel Gawedzki on 26-Jul-2019. */
public class ToolkitService extends Task<List<CasesData>> {

    protected final static Logger logger = LoggerFactory.getLogger(ToolkitService.class);
    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;
    private String formDigest;

    public ToolkitService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.httpRequester = new HttpRequester(applicationProperties);
    }

    @Override
    protected List<CasesData> call() {
        return getAvailableCases();
    }

    private List<CasesData> getAvailableCases() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Cookie", CookiesService.getFedAuthString());
        String url = applicationProperties.toolkitHostUrl() + "/_goapi/UserProfile/Cases";
        List<CasesData> cases = new LinkedList<>();
        try {
            ToolkitCasePayload payload = new ToolkitCasePayload(
                    new SortFieldDefinition("ows_Created", "datetime"),
                    List.of(
                            new ItemField("title", "", "ows_Title"),
                            new ItemField("id", "", "CaseID"),
                            new ItemField("created", "", "ows_Created")
                    ),
                    false
            );
            ToolkitCaseResponse response = httpRequester.post(url, headers, payload, ToolkitCaseResponse.class);
            cases = response.cases;
        } catch (IOException ex) {
            updateMessage(BundleUtils.getMsg("toolkit.projects.downloadFail"));
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        } finally {
            updateProgress(1, 1);
        }
        return cases;
    }

    public List<CasesData> downloadUserProjects() {
        return call();
    }

    public Optional<String> lastItemModifiedDate(String userId) {
        Optional<String> modifiedDate = Optional.empty();
        if ("".equals(userId)) {
            logger.warn("UserId is empty and I can not download last submission date.");
            return modifiedDate;
        }

        String select = "$select=Body,SubmissionDate,GUID,Title,EmployeeId,Modified";
        String filter = String.format("$filter=EmployeeId+eq+%s", userId);
        String orderBy = "$orderby=Modified+desc";
        String top = "$top=1";
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s&%s",
                applicationProperties.toolkitHostUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                select,
                filter,
                orderBy,
                top
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString()
        );

        try {
            JsonObject jsonObject = httpRequester.executeGET(sharePointConfig);
            if (jsonObject == null) {
                throw new IllegalArgumentException("Null response from toolkit.");
            }
            JsonObject dElement = jsonObject.getAsJsonObject("d");
            if (dElement == null) {
                throw new IllegalArgumentException("Can not handle the response from toolkit.");
            }
            JsonArray results = dElement.getAsJsonArray("results");
            if (results == null || results.isEmpty()) {
                throw new IllegalArgumentException("Can not handle the response from toolkit. Array is empty.");
            }
            JsonObject firstElement = results.get(0).getAsJsonObject();
            JsonElement modifiedDateElement = firstElement.get("Modified");
            if (modifiedDateElement == null) {
                throw new IllegalArgumentException("Can not find submission date in the response from toolkit.");
            }
            modifiedDate = Optional.ofNullable(modifiedDateElement.getAsString());
        } catch (Exception ex) {
            logger.error("Can not download last item submission date. {}", ex.getMessage());
        }
        return modifiedDate;
    }

    public boolean isCookieWorking(String fedAuthString) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Cookie", fedAuthString);
        String url = String.format("%s/_api/web/siteusers/getbyemail('%s')",
                applicationProperties.toolkitHostUrl(),
                applicationProperties.toolkitUserEmail()
        );

        try {
            int statusCode = httpRequester.getForStatusCode(url, headers);
            return Stream.of(401, 403, 500).noneMatch(sc -> sc == statusCode);
        } catch (IOException ex) {
            logger.error("Could not download toolkit projects for user [{}]. ", applicationProperties.toolkitUsername(), ex);
        }
        return false;
    }

    public String getFormDigest() throws IOException {
        if (formDigest == null) {
            String fullUrl = applicationProperties.toolkitHostUrl() + applicationProperties.toolkitCopyCase() + "/_api/contextinfo";
            SharePointConfig sharePointConfig = new SharePointConfig(
                    applicationProperties.toolkitHostUrl(),
                    fullUrl,
                    CookiesService.getFedAuthString()
            );

            formDigest = httpRequester.requestDigest(sharePointConfig);
            logger.debug("Form digest: [{}]", formDigest);
        }
        return formDigest;
    }

    public String createItem() throws IOException {
        String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/AddValidateUpdateItemUsingPath",
                applicationProperties.toolkitHostUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString(),
                getFormDigest()
        );

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json;odata=nometadata");
        requestHeaders.put("Content-Type", "application/json;odata=nometadata");
        requestHeaders.put("Cookie", sharePointConfig.getFedAuth());

        JsonObject item = httpRequester.executePOST(sharePointConfig, createItemJson(), requestHeaders);

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
        decodedUrl.addProperty("DecodedUrl", applicationProperties.toolkitUserFolderUrl());

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
                applicationProperties.toolkitHostUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId,
                path.getFileName().toString()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString(),
                getFormDigest()
        );

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
            String fullUrl = String.format("%s/_api/web/siteusers/getbyemail('%s')",
                    applicationProperties.toolkitHostUrl(),
                    applicationProperties.toolkitUserEmail()
            );
            SharePointConfig sharePointConfig = new SharePointConfig(
                    applicationProperties.toolkitHostUrl(),
                    fullUrl,
                    CookiesService.getFedAuthString(),
                    getFormDigest()
            );

            JsonObject jsonObject = httpRequester.executeGET(sharePointConfig);
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
                applicationProperties.toolkitHostUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName(),
                itemId
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString(),
                getFormDigest()
        );

        JsonObject payload = new JsonObject();
        payload.addProperty("ClassificationId", 12);

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", "application/json;odata=verbose");
        requestHeaders.put("If-Match", "*");
        requestHeaders.put("X-HTTP-Method", "MERGE");
        requestHeaders.put("Cookie", sharePointConfig.getFedAuth());

        JsonObject jsonObject = httpRequester.executePOST(sharePointConfig, payload, requestHeaders);
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
                    applicationProperties.toolkitHostUrl(),
                    applicationProperties.toolkitCopyCase(),
                    applicationProperties.toolkitCopyListName(),
                    itemId
            );
            SharePointConfig sharePointConfig = new SharePointConfig(
                    applicationProperties.toolkitHostUrl(),
                    fullUrl,
                    CookiesService.getFedAuthString(),
                    getFormDigest()
            );

            Map<String, String> requestHeaders = new LinkedHashMap<>();
            requestHeaders.put("Accept", "application/json;odata=verbose");
            requestHeaders.put("If-Match", "*");
            requestHeaders.put("X-HTTP-Method", "DELETE");
            requestHeaders.put("Cookie", sharePointConfig.getFedAuth());

            httpRequester.executePOST(sharePointConfig, requestHeaders);
            logger.info("Cleanup done.");
        } catch (IOException ex) {
            logger.error("Problems with cleaning up.", ex);
        }
    }
}
