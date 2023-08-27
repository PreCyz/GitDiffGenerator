package pg.gipter.core.producers.processor;

import com.google.gson.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.core.*;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.toolkit.sharepoint.HttpRequesterNTML;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class GETCallTest {

    @Test
    void pocForDownloadingTheLastUploadDate() throws Exception {
        String[] args = new String[]{
                ArgName.toolkitUsername.name() + "=PAWG",
        };

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        String select = "$select=Body,SubmissionDate,GUID,Title";
        String orderBy = "$orderby=SubmissionDate+desc";
        String top = "$top=1";

        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?",
                applicationProperties.toolkitRESTUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        ) + select + "&" + orderBy + "&" + top;

        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitUsername(),
                "give-password-here",
                applicationProperties.toolkitDomain(),
                applicationProperties.toolkitRESTUrl(),
                url
        );
        JsonObject jsonObject = new GETCall(sharePointConfig, new HttpRequesterNTML(applicationProperties)).call();

        assertThat(jsonObject).isNotNull();

        JsonObject actual = jsonObject.getAsJsonObject("d").getAsJsonArray("results").get(0).getAsJsonObject();

        String submissionDate = actual.get("SubmissionDate").getAsString();
        LocalDateTime submissionDateTime = LocalDateTime.parse(submissionDate, DateTimeFormatter.ISO_DATE_TIME);
        assertThat(submissionDateTime.getYear()).isEqualTo(2019);
        assertThat(submissionDateTime.getMonthValue()).isEqualTo(7);
        assertThat(submissionDateTime.getDayOfMonth()).isEqualTo(26);
        assertThat(submissionDateTime.getHour()).isEqualTo(13);
        assertThat(submissionDateTime.getMinute()).isEqualTo(30);
        assertThat(submissionDateTime.getSecond()).isEqualTo(6);

        write(jsonObject);
    }

    void write(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonObject);
        try (OutputStream os = new FileOutputStream("WorkItems.json");
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }
}