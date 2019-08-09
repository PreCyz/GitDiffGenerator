package pg.gipter.producer.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;

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
                ArgName.toolkitPassword.name() + "=give-password-here",
        };

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(args);
        String select = "$select=Body,SubmissionDate,GUID,Title";
        String orderBy = "$orderby=SubmissionDate+desc";
        String top = "$top=1";

        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?",
                applicationProperties.toolkitUrl(),
                applicationProperties.toolkitCopyCase(),
                applicationProperties.toolkitCopyListName()
        ) + select + "&" + orderBy + "&" + top
                ;
        JsonObject jsonObject = new GETCall(url, applicationProperties).call();

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