package pg.gipter.toolkit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.client.SoapFaultClientException;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.toolkit.helpers.XmlHelper;
import pg.gipter.toolkit.sharepoint.soap.SharePointSoapClient;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiffUploaderTest {

    private DiffUploader uploader;

    @Test
    void given_applicationProperties_when_buildAttributesMap_then_return_properMap() {
        ApplicationProperties properties = ApplicationPropertiesFactory.getInstance(new String[]{
                "uploadType=SIMPLE",
                "startDate=2018-10-18",
                "endDate=2018-10-19",
                "toolkitUsername=xxx",
                "useUI=n",
        });
        properties.setVcs(EnumSet.of(VersionControlSystem.GIT, VersionControlSystem.SVN));
        uploader = new DiffUploader(properties);
        Map<String, String> map = uploader.buildAttributesMap("viewName");

        LocalDateTime actualSubmissionDate = LocalDateTime.parse(map.get("SubmissionDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        assertThat(map).hasSize(8);
        assertThat(map.get("Title")).isEqualTo("2018-october-20181018-20181019");
        assertThat(map.get("Employee")).isEqualTo("-1;#XXX");
        assertThat(actualSubmissionDate.getYear()).isEqualTo(2018);
        assertThat(actualSubmissionDate.getMonthValue()).isEqualTo(10);
        assertThat(actualSubmissionDate.getDayOfMonth()).isEqualTo(19);
        assertThat(actualSubmissionDate.getHour()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getMinute()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getSecond()).isBetween(0, 59);
        assertThat(map.get("Classification")).isEqualTo("12;#Changeset (repository change report)");
        assertThat(map.get("Body")).isEqualTo("GIT,SVN diff file.");
        assertThat(map.get("ViewName")).isEqualTo("viewName");
        assertThat(map.get("RootFolder")).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/XXX");
        assertThat(map.get("Cmd")).isEqualTo("New");
    }

    @Test
    void givenCodeProtectionStatement_whenBuildAttributesMap_thenReturnProperMap() {
        ApplicationProperties properties = ApplicationPropertiesFactory.getInstance(new String[]{
                "itemType=STATEMENT",
                "startDate=2017-10-19",
                "endDate=2017-12-20",
                "itemFileNamePrefix=custom",
                "toolkitUsername=xxx",
                "useUI=n",
        });
        uploader = new DiffUploader(properties);
        Map<String, String> map = uploader.buildAttributesMap("viewName");

        LocalDateTime actualSubmissionDate = LocalDateTime.parse(map.get("SubmissionDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        assertThat(map).hasSize(8);
        assertThat(map.get("Title")).isEqualTo("custom");
        assertThat(map.get("Employee")).isEqualTo("-1;#XXX");
        assertThat(actualSubmissionDate.getYear()).isEqualTo(2017);
        assertThat(actualSubmissionDate.getMonthValue()).isEqualTo(12);
        assertThat(actualSubmissionDate.getDayOfMonth()).isEqualTo(20);
        assertThat(actualSubmissionDate.getHour()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getMinute()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getSecond()).isBetween(0, 59);
        assertThat(map.get("Classification")).isEqualTo("12;#Changeset (repository change report)");
        assertThat(map.get("Body")).isEqualTo("STATEMENT file.");
        assertThat(map.get("ViewName")).isEqualTo("viewName");
        assertThat(map.get("RootFolder")).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/XXX");
        assertThat(map.get("Cmd")).isEqualTo("New");
    }

    @Test
    void when_uploadDiff_then_throwSoapException() throws Exception {
        String xml = XmlHelper.documentToString(XmlHelper.getDocument(XmlHelper.getFullXmlPath("wsErrorSoap.xml")));
        Source xmlSource = new StreamSource(new StringReader(xml));
        SoapFault soapFault = mock(SoapFault.class);
        when(soapFault.getSource()).thenReturn(xmlSource);
        SoapFaultClientException soapException = mock(SoapFaultClientException.class);
        when(soapException.getSoapFault()).thenReturn(soapFault);
        SharePointSoapClient client = mock(SharePointSoapClient.class);
        when(client.getListAndView()).thenThrow(soapException);
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(eq(SharePointSoapClient.class))).thenReturn(client);
        uploader = spy(new DiffUploader());
        uploader.setSpringContext(context);

        try {
            uploader.uploadDiff();
            fail("Should throw RuntimeException with relevant message.");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Error during upload diff. Data at the root level is invalid. Line 1, position 1.");
            verify(soapException, times(1)).getSoapFault();
            verify(soapFault, times(1)).getSource();
        }
    }

    @Test
    void when_uploadDiff_then_throwException() {
        SharePointSoapClient client = mock(SharePointSoapClient.class);
        when(client.getListAndView()).thenThrow(new IllegalArgumentException());
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(eq(SharePointSoapClient.class))).thenReturn(client);
        uploader = spy(new DiffUploader());
        uploader.setSpringContext(context);

        try {
            uploader.uploadDiff();
            fail("Should throw RuntimeException with relevant message.");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Error during upload diff. null");
        }
    }

}