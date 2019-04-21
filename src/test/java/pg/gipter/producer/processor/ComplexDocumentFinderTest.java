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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexDocumentFinderTest {

    private ComplexDocumentFinder finder;

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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-1.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-2.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-3.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-4.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-5.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-6.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
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
        finder = new ComplexDocumentFinder(applicationProperties);
        String path = XmlHelper.getFullXmlPath("Item-case-7.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject js = gson.fromJson(bufferedReader, JsonObject.class);
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        Map<String, String> filesToDownload = finder.getFilesToDownload(documentDetails);

        assertThat(filesToDownload).hasSize(2);
        assertThat(filesToDownload.get("4.0v-my-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(filesToDownload.get("1.0v-D0180 - Integration Design - Topdanmark integrations - Party Master.docx"))
                .isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenProperties_whenBuildPageableUrl_thenReturnUrls() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new ComplexDocumentFinder(applicationProperties);
        int documentId = 100;

        String actual = finder.buildPageableUrl(
                applicationProperties.projectPaths().toArray(new String[1])[0],
                applicationProperties.toolkitProjectListNames().toArray(new String[1])[0],
                documentId
        );

        assertThat(actual).startsWith("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/web/lists/GetByTitle('Deliverables')/items" +
                "?$select=Id,Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size," +
                "File/Versions/Url,File/Versions/VersionLabel," +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email" +
                "&$filter=");
        assertThat(actual).contains("Created+lt+datetime'2019-03-02");
        assertThat(actual).contains("+or+Modified+ge+datetime'2019-02-24");
        assertThat(actual).contains("&$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy");
        assertThat(actual).contains("&$top=100");
        assertThat(actual).endsWith("&$skiptoken=Paged=TRUE&p_SortBehavior=0&p_ID=" + documentId);
    }

    @Test
    void givenProperties_whenBuildUrls_thenReturnListOfUrls() throws FileNotFoundException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new ComplexDocumentFinder(applicationProperties);

        String path = XmlHelper.getFullXmlPath("item-count.json");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        JsonObject itemCount = gson.fromJson(bufferedReader, JsonObject.class);

        List<ItemCountResponse> responses = Stream.of(new ItemCountResponse(
                applicationProperties.projectPaths().toArray(new String[1])[0],
                applicationProperties.toolkitProjectListNames().toArray(new String[1])[0],
                itemCount
        )).collect(Collectors.toList());

        List<String> actual = finder.buildUrls(responses);

        assertThat(actual).hasSize(12);
        for (int i = 0; i < actual.size(); ++i) {
            if (i == 0) {
                assertThat(actual.get(i)).doesNotEndWith("&$skiptoken=Paged=TRUE&p_SortBehavior=0&p_ID=");
            } else {
                assertThat(actual.get(i)).endsWith("&$skiptoken=Paged=TRUE&p_SortBehavior=0&p_ID=" + 100 * i);
            }
        }
    }
}