package pg.gipter.producer.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.helper.XmlHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SimpleDocumentFinderTest {

    private SimpleDocumentFinder finder;

    @Test
    void givenItemsJson_whenExtractItemDetails_thenReturnListOfItemDetails() throws FileNotFoundException {
        finder = new SimpleDocumentFinder(null);
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

        Map<String, String> filesToDownload = finder.getFilesToDownload(new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("3.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    @Disabled
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
        JsonObject js = getJsonObject("customItem.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);
        Map<String, String> filesToDownload = finder.getFilesToDownload(new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails);

        List<File> actual = finder.downloadDocuments(filesToDownload);

        assertThat(actual).hasSize(2);
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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

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

        Map<String, String> filesToDownload = finder.getFilesToDownload(
                new ArrayList<>(applicationProperties.projectPaths()).get(0), documentDetails
        );

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("4.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    private JsonObject getJsonObject(String s) throws FileNotFoundException {
        String path = XmlHelper.getFullXmlPath(s);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, JsonObject.class);
    }

    @Test
    void find() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-06",
                        ArgName.endDate + "=2019-04-13",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        finder.find();
    }


    @Test
    void givenApplicationProperties_whenGetItemsWithVersions_thenReturnProperJsonObject() throws IOException {
        HttpRequester mockHttpRequester = mock(HttpRequester.class);
        when(mockHttpRequester.executeGET(anyString())).thenReturn(new JsonObject());
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-06",
                        ArgName.endDate + "=2019-04-13",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new SimpleDocumentFinder(applicationProperties);
        finder.setHttpRequester(mockHttpRequester);

        finder.getItemsWithVersions(
                applicationProperties.projectPaths().toArray(new String[1])[0],
                applicationProperties.toolkitProjectListNames().toArray(new String[1])[0]
        );

        verify(mockHttpRequester, times(1)).executeGET(argThat(url -> url.startsWith("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/web/lists/GetByTitle('Deliverables')/items?$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString,File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel,File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email,File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email,File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel,File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email&$filter=Modified+ge+datetime'2019-04-06")
                && url.contains("+and+Modified+le+datetime'2019-04-13")
                && url.endsWith("&$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy"))
        );
    }
}
