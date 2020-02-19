package pg.gipter.core.producer.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pg.gipter.core.*;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.utils.StringUtils;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class SimpleDocumentFinderTest {

    private SimpleDocumentFinder finder;

    private JsonObject getJsonObject(String s) throws FileNotFoundException {
        String path = XmlHelper.getFullXmlPath(s);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, JsonObject.class);
    }

    @Test
    void givenItemsJson_whenExtractItemDetails_thenReturnListOfItemDetails() throws FileNotFoundException {
        finder = new SimpleDocumentFinder(ApplicationPropertiesFactory.getInstance(new String[]{}));
        JsonObject js = getJsonObject("items.json");

        List<DocumentDetails> actual = finder.convertToDocumentDetails(js);

        assertThat(actual).hasSize(9);
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
        JsonObject js = getJsonObject("customItem.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("3.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenEmptyMap_whenDownloadDocuments_thenThrowIllegalArgumentException() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name()}
        );
        finder = new SimpleDocumentFinder(applicationProperties);

        try {
            finder.downloadDocuments(new HashMap<>());
            fail("Should throw IllegalArgumentException");
        }catch (IllegalArgumentException ex) {
            assertThat(true).isTrue();
        }
    }

    @Test
    void givenItemsWithComplexHistory_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-1.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(4);
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("2.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("3.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("5.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase2_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-2.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(1);
        assertThat(filesToDownload.get("1.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase3_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-3.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(4);
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("2.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("3.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("4.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase4_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-4.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(1);
        assertThat(filesToDownload.get("4.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase5_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-5.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).isEmpty();
    }

    @Test
    void givenItemsCase6_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-6.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(1);
        assertThat(filesToDownload.get("3.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase7_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-7.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("4.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase8_whenGetFilesToDownload_thenReturnMapWithFilesToDownload() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-01",
                        ArgName.endDate + "=2019-04-13",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-8.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js).stream()
                .filter(dd -> !StringUtils.nullOrEmpty(dd.getDocType()))
                .collect(toList());

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(4);
        assertThat(filesToDownload.get("38.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/19456/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("39.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/19968/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("42.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/21504/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("44.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenProperties_whenBuildUrls_thenReturnUrls() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-06",
                        ArgName.endDate + "=2019-04-13",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);

        List<String> actual = finder.buildFullUrls();

        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).startsWith("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/web/lists/GetByTitle('Deliverables')/items?$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString,File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel,File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email,File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email,File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel,File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email&$filter=Modified+ge+datetime'2019-04-06");
        assertThat(actual.get(0)).contains("+and+Modified+le+datetime'2019-04-13");
        assertThat(actual.get(0)).endsWith("&$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy");
    }

    @Test
    void givenValueWithListName_whenGetProject_thenReturnProject() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(new String[]{});
        finder = new SimpleDocumentFinder(applicationProperties);

        String actual = finder.getProject("/cases/GTE440/TOEDNLD/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");

        assertThat(actual).isEqualTo("/cases/GTE440/TOEDNLD/");
    }

    @Test
    void givenDifferentValueWithListName_whenGetProject_thenReturnProject() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(new String[]{
                ArgName.toolkitProjectListNames + "=SlutProdukter"
        });
        finder = new SimpleDocumentFinder(applicationProperties);

        String actual = finder.getProject("/cases/GTE440/TOEDNLD/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");

        assertThat(actual).isEqualTo("/cases/GTE440/TOEDNLD/");
    }
}
