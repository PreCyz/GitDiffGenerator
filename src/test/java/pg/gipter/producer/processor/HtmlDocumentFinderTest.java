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
import pg.gipter.utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class HtmlDocumentFinderTest {

    private HtmlDocumentFinder finder;

    private JsonObject getJsonObject(String s) throws FileNotFoundException {
        String path = XmlHelper.getFullXmlPath(s);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
        Gson gson = new Gson();
        return gson.fromJson(bufferedReader, JsonObject.class);
    }

    @Test
    void givenItemsJsonWithDatesInThePast_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-25",
                        ArgName.endDate + "=2019-04-06",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("customItem.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(2);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(1.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 13, 51, 27));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(1).getVersion()).isEqualTo(3.0);
        assertThat(htmlDocuments.get(1).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 14, 31, 07));
        assertThat(htmlDocuments.get(1).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }


    @Test
    void givenItemsWithComplexHistory_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-1.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(4);

        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(1.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 13, 51, 27));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(1).getVersion()).isEqualTo(2.0);
        assertThat(htmlDocuments.get(1).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 14, 22, 7));
        assertThat(htmlDocuments.get(1).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(2).getVersion()).isEqualTo(3.0);
        assertThat(htmlDocuments.get(2).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 7, 36, 38));
        assertThat(htmlDocuments.get(2).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(3).getVersion()).isEqualTo(5.0);
        assertThat(htmlDocuments.get(3).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 28, 14, 31, 7));
        assertThat(htmlDocuments.get(3).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase2_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-2.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(1);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(1.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 14, 22, 7));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase3_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-3.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(4);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(1.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 13, 51, 27));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(1).getVersion()).isEqualTo(2.0);
        assertThat(htmlDocuments.get(1).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 14, 22, 7));
        assertThat(htmlDocuments.get(1).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3072/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(2).getVersion()).isEqualTo(3.0);
        assertThat(htmlDocuments.get(2).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 7, 36, 38));
        assertThat(htmlDocuments.get(2).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(3).getVersion()).isEqualTo(4.0);
        assertThat(htmlDocuments.get(3).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 14, 31, 7));
        assertThat(htmlDocuments.get(3).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase4_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-4.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(1);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(4.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 14, 31, 7));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase5_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-5.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).isEmpty();
    }

    @Test
    void givenItemsCase6_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-6.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(1);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(3.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 7, 36, 38));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/4096/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase7_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-24",
                        ArgName.endDate + "=2019-03-02",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-7.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(2);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(1.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 26, 13, 51, 27));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/2560/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(1).getVersion()).isEqualTo(4.0);
        assertThat(htmlDocuments.get(1).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 2, 27, 14, 31, 7));
        assertThat(htmlDocuments.get(1).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/3584/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenItemsCase8_whenGetFilesToDownload_thenReturnListOfHtmlDocs() throws IOException {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-04-01",
                        ArgName.endDate + "=2019-04-13",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("Item-case-8.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js).stream()
                .filter(dd -> !StringUtils.nullOrEmpty(dd.getDocType()))
                .collect(toList());

        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        assertThat(htmlDocuments).hasSize(4);
        assertThat(htmlDocuments.get(0).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(0).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(0).getVersion()).isEqualTo(38.0);
        assertThat(htmlDocuments.get(0).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 3, 29, 12, 29, 38));
        assertThat(htmlDocuments.get(0).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/19456/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(1).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(1).getVersion()).isEqualTo(39.0);
        assertThat(htmlDocuments.get(1).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 4, 5, 13, 56, 31));
        assertThat(htmlDocuments.get(1).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/19968/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(2).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(2).getVersion()).isEqualTo(42.0);
        assertThat(htmlDocuments.get(2).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 4, 9, 9, 16, 28));
        assertThat(htmlDocuments.get(2).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/21504/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getFileName()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
        assertThat(htmlDocuments.get(3).getTitle()).isEqualTo("D0180 - Integration Design - Topdanmark integrations - Party Master");
        assertThat(htmlDocuments.get(3).getVersion()).isEqualTo(44.0);
        assertThat(htmlDocuments.get(3).getModificationDate()).isEqualTo(LocalDateTime.of(2019, 4, 12, 10, 01, 29));
        assertThat(htmlDocuments.get(3).getLink()).isEqualTo("https://goto.netcompany.com/cases/GTE440/TOEDNLD/Deliverables/D0180 - Integration design/Topdanmark integrations/D0180 - Integration Design - Topdanmark integrations - Party Master.docx");
    }

    @Test
    void givenDocumentDetails_whenCreateHtml_thenReturnHtml() throws Exception {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{
                        ArgName.preferredArgSource + "=" + PreferredArgSource.CLI.name(),
                        ArgName.startDate + "=2019-02-25",
                        ArgName.endDate + "=2019-04-06",
                        ArgName.toolkitUsername + "=pawg",
                        ArgName.projectPath + "=/cases/GTE440/TOEDNLD"
                });
        finder = new HtmlDocumentFinder(applicationProperties);
        JsonObject js = getJsonObject("customItem.json");
        List<DocumentDetails> documentDetails = finder.convertToDocumentDetails(js);
        List<HtmlDocument> htmlDocuments = finder.getHtmlDocuments(documentDetails);

        String html = finder.createHtml(htmlDocuments);

        assertThat(html).isNotBlank();
    }
}