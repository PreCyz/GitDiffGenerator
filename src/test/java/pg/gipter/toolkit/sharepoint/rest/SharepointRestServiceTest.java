package pg.gipter.toolkit.sharepoint.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pg.gipter.TestUtils;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.utils.PropertiesHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SharepointRestServiceTest {

    @Test
    void givenItemsJson_whenExtractItemDetails_thenReturnListOfItemDetails() throws FileNotFoundException {
        SharepointRestService service = new SharepointRestService(null);
        String path = XmlHelper.getFullXmlPath("items.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);

        List<DocumentDetails> actual = service.extractDocumentDetails(js);

        assertThat(actual).hasSize(9);
    }

    @Test
    void givenItemsJson_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("startDate", "2019-04-03");
        properties.setProperty("endDate", "2019-04-06");
        properties.setProperty("toolkitUsername", "pawg");
        properties.setProperty("toolkitPassword", "JanuarY12!@");
        PropertiesHelper loader = TestUtils.mockPropertiesLoader(properties);
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.preferredArgSource + "=" + PreferredArgSource.FILE.name()
                });
        applicationProperties.init(new String[]{}, loader);
        SharepointRestService service = new SharepointRestService(applicationProperties);
        String path = XmlHelper.getFullXmlPath("items.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = service.extractDocumentDetails(js);

        Map<String, String> filesToDownload = service.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(3);
    }

    @Test
    void givenItemsJsonWithDatesInThePast_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("startDate", "2019-02-25");
        properties.setProperty("endDate", "2019-04-06");
        properties.setProperty("toolkitUsername", "pawg");
        properties.setProperty("toolkitPassword", "JanuarY12!@");
        PropertiesHelper loader = TestUtils.mockPropertiesLoader(properties);
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.preferredArgSource + "=" + PreferredArgSource.FILE.name()
                });
        applicationProperties.init(new String[]{}, loader);
        SharepointRestService service = new SharepointRestService(applicationProperties);
        String path = XmlHelper.getFullXmlPath("customItem.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = service.extractDocumentDetails(js);

        Map<String, String> filesToDownload = service.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("after_change-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180%20-%20Integration%20design/Topdanmark%20integrations/D0180%20-%20Integration%20Design%20-%20Topdanmark%20integrations%20-%20Party%20Master.docx");
        assertThat(filesToDownload.get("before_change-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180%20-%20Integration%20design/Topdanmark%20integrations/D0180%20-%20Integration%20Design%20-%20Topdanmark%20integrations%20-%20Party%20Master.docx");
    }

    @Test
    void givenCustomItems_whenDownload_thenReturnDownload() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("startDate", "2019-02-25");
        properties.setProperty("endDate", "2019-04-06");
        properties.setProperty("toolkitUsername", "pawg");
        properties.setProperty("toolkitPassword", "JanuarY12!@");
        properties.setProperty("itemPath", "/home/gawa/IdeaProjects/GitDiffGenerator/src/test/java/resources/xml");
        PropertiesHelper loader = TestUtils.mockPropertiesLoader(properties);
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.preferredArgSource + "=" + PreferredArgSource.FILE.name()}
        );
        applicationProperties.init(new String[]{}, loader);
        SharepointRestService service = new SharepointRestService(applicationProperties);
        String path = XmlHelper.getFullXmlPath("customItem.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = service.extractDocumentDetails(js);
        Map<String, String> filesToDownload = service.getFilesToDownload(documentDetails);

        service.downloadFiles(filesToDownload);
    }
}