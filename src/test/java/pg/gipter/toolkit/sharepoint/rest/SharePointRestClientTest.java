package pg.gipter.toolkit.sharepoint.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.toolkit.sharepoint.HttpRequester;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class SharePointRestClientTest {

    @Test
    void name() throws IOException {
        ApplicationProperties appProp = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.toolkitUsername + "=PAWG",
                        ArgName.toolkitPassword + "=JanuarY12!@"
                }
        );

        HttpRequester requester = new HttpRequester(appProp);

        JsonObject jsonObject = requester.executeGET("https://goto.netcompany.com/cases/GTE106/NCSCOPY/_api/web/lists/GetByTitle('WorkItems')/items");

        try (Writer writer = new FileWriter("WorkItems.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        }

        assertThat(jsonObject).isNotNull();
    }

    @Test
    void requestDigest() throws IOException {
        ApplicationProperties appProp = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.toolkitUsername + "=PAWG",
                        ArgName.toolkitPassword + "=JanuarY12!@"
                }
        );

        HttpRequester requester = new HttpRequester(appProp);

        String requestDigest = requester.requestDigest();
        System.out.println(requestDigest);

        assertThat(requestDigest).isNotEmpty();
    }

    @Test
    void createItem() throws IOException {
        ApplicationProperties appProp = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.toolkitUsername + "=PAWG",
                        ArgName.toolkitPassword + "=JanuarY12!@"
                }
        );

        SharePointRestClient client = new SharePointRestClient(appProp);

        client.createItem();
    }

    @Test
    void createItem2010() throws IOException {
        ApplicationProperties appProp = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.toolkitUsername + "=PAWG",
                        ArgName.toolkitPassword + "=JanuarY12!@"
                }
        );

        SharePointRestClient client = new SharePointRestClient(appProp);

        client.createItem2010();
    }

    @Test
    void getItems() throws IOException {

        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.toolkitUsername + "=PAWG",
                        ArgName.toolkitPassword + "=JanuarY12!@"
                }
        );

        String claim = String.format("'i:0#.w|%s\\%s'",
                applicationProperties.toolkitDomain(), applicationProperties.toolkitUsername()
        );
        String fullUrl = applicationProperties.toolkitUrl() +
                "/_api/web/siteusers(@v)?@v=" + URLEncoder.encode(claim, StandardCharsets.UTF_8.name()) +
                "&expand=UserId";

        HttpRequester requester = new HttpRequester(applicationProperties);
        JsonObject jsonObject = requester.executeGET(fullUrl);

        System.out.println(jsonObject);

    }
}