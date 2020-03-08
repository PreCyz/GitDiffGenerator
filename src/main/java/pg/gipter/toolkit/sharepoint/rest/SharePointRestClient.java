package pg.gipter.toolkit.sharepoint.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producer.command.ItemType;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.util.stream.Collectors.joining;

public class SharePointRestClient {

    private final ApplicationProperties applicationProperties;
    private final HttpRequester httpRequester;

    public SharePointRestClient(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        httpRequester = new HttpRequester(applicationProperties);
    }

    private JsonObject createItemJson() {
        LocalDate endDate = applicationProperties.endDate();
        String fileName = applicationProperties.fileName();
        String title = fileName.substring(0, fileName.indexOf("."));
        String allVcs = applicationProperties.vcsSet().stream().map(Enum::name).collect(joining(","));
        String description = String.format("%s diff file.", allVcs);
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            description = String.format("%s file.", ItemType.STATEMENT);
        }
        LocalDateTime submissionDate = LocalDateTime.of(endDate, LocalTime.now());

        JsonObject decodedUrl = new JsonObject();
        //decodedUrl.addProperty("DecodedUrl", "https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems");
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
        arrayElement.addProperty("FieldName", "Employee");
        arrayElement.addProperty("FieldValue", "-1;#" + applicationProperties.toolkitUsername());
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "SubmissionDate");
        arrayElement.addProperty("FieldValue", submissionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "ClassificationId");
        //arrayElement.addProperty("FieldValue", "12;#Changeset (repository change report)");
        arrayElement.addProperty("FieldValue", 12);
        formValues.add(arrayElement);
        arrayElement = new JsonObject();
        arrayElement.addProperty("FieldName", "Body");
        arrayElement.addProperty("FieldValue", description);
        formValues.add(arrayElement);

        JsonObject type = new JsonObject();
        type.addProperty("type", String.format("SP.Data.%sListItem", applicationProperties.toolkitCopyListName()));

        JsonObject item = new JsonObject();

        item.add("__metadata", type);
        item.addProperty("Title", applicationProperties.toolkitCopyListName());

        item.add("listItemCreateInfo", listItemCreateInfo);
        item.add("formValues", formValues);
        item.addProperty("bNewDocumentUpdate", false);

        return item;
    }

    public void createItem() throws IOException {
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
                fullUrl
        );

        JsonObject jsonObject = httpRequester.executePOST(sharePointConfig, createItemJson());

        System.out.println(jsonObject.toString());
    }

    public void createItem2010() throws IOException {
        String fullUrl = String.format("%s%s/_vti_bin/ListData.svc/%s",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        );
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                applicationProperties.toolkitPassword(),
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitUrl(),
                fullUrl
        );

        JsonObject jsonObject = httpRequester.executePOST2010(sharePointConfig, createItemJson2010());

        System.out.println(jsonObject.toString());
    }

    private JsonObject createItemJson2010() throws RuntimeException, UnsupportedEncodingException {
        LocalDate endDate = applicationProperties.endDate();
        String fileName = applicationProperties.fileName();
        String title = fileName.substring(0, fileName.indexOf("."));
        String allVcs = applicationProperties.vcsSet().stream().map(Enum::name).collect(joining(","));
        //String description = String.format("\u003cp\u003e%s diff file.\u003c/p\u003e", allVcs);
        String description = URLEncoder.encode(String.format("<p>%s diff file.</p>", allVcs), StandardCharsets.UTF_8.name());
        if (applicationProperties.itemType() == ItemType.STATEMENT) {
            description = String.format("%s file.", ItemType.STATEMENT);
        }
        LocalDateTime submissionDate = LocalDateTime.of(endDate, LocalTime.now());

        JsonObject item = new JsonObject();
        item.addProperty("Path", applicationProperties.toolkitUserFolder());
        item.addProperty("Title", title);
        item.addProperty("ClassificationId", 12);
        //item.addProperty("Body", "");
        //item.addProperty("Employee", "-1;#" + applicationProperties.toolkitUsername());
        //item.addProperty("EmployeeId", 179);
        //item.addProperty("SubmissionDate", submissionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        return item;
    }
}
