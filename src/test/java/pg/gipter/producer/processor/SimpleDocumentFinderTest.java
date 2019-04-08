package pg.gipter.producer.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.helper.XmlHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleDocumentFinderTest {

    private SimpleDocumentFinder finder;

    @Test
    void givenItemsJson_whenExtractItemDetails_thenReturnListOfItemDetails() throws FileNotFoundException {
        finder = new SimpleDocumentFinder(null);
        String path = XmlHelper.getFullXmlPath("items.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);

        List<DocumentDetails> actual = finder.convertToDocumentDetails(js);

        assertThat(actual).hasSize(9);
    }

    @Test
    void givenItemsJson_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-03",
                        ArgName.endDate + "=2019-04-06",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("items.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails);

        assertThat(filesToDownload).hasSize(3);
    }

    @Test
    void givenItemsJsonWithDatesInThePast_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-25",
                        ArgName.endDate + "=2019-04-06",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("customItem.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("after_change-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("before_change-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenCustomItems_whenDownload_thenReturnDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-25",
                        ArgName.endDate + "=2019-04-06",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.toolkitPassword + "=JanuarY12!@",
                        ArgName.itemPath + "=/home/gawa/IdeaProjects/GitDiffGenerator/src/test/java/resources/xml",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                }
        );
        finder = new SimpleDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("customItem.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);
        Map<String, String> filesToDownload = finder.getFilesToDownload(new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails);

        List<File> actual = finder.downloadDocuments(filesToDownload);

        assertThat(actual).hasSize(2);
    }

}