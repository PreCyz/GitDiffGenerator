package pg.gipter.toolkit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import pg.gipter.MockitoExtension;
import pg.gipter.settings.ApplicationProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiffUploaderTest {

    private DiffUploader uploader;

    @Test
    void given_applicationProperties_when_buildAttributesMap_then_return_properMap() {
        ApplicationProperties properties = new ApplicationProperties(new String[]{
                "codeProtection=NONE",
                "startDate=2018-10-18",
                "endDate=2018-10-19",
                "toolkitUsername=xxx",
        });
        uploader = new DiffUploader(properties);
        Map<String, String> map = uploader.buildAttributesMap("viewName");

        LocalDateTime actualSubmissionDate = LocalDateTime.parse(map.get("SubmissionDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        assertThat(map).hasSize(8);
        assertThat(map.get("Title")).isEqualTo("2018-october-20181018-20181019");
        assertThat(map.get("Employee")).isEqualTo("-1;#xxx@netcompany.com");
        assertThat(actualSubmissionDate.getYear()).isEqualTo(2018);
        assertThat(actualSubmissionDate.getMonthValue()).isEqualTo(10);
        assertThat(actualSubmissionDate.getDayOfMonth()).isEqualTo(19);
        assertThat(actualSubmissionDate.getHour()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getMinute()).isBetween(0, 59);
        assertThat(actualSubmissionDate.getSecond()).isBetween(0, 59);
        assertThat(map.get("Classification")).isEqualTo("12;#Changeset (repository change report)");
        assertThat(map.get("Body")).isEqualTo("GIT diff file.");
        assertThat(map.get("ViewName")).isEqualTo("viewName");
        assertThat(map.get("RootFolder")).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/Lists/WorkItems/XXX");
        assertThat(map.get("Cmd")).isEqualTo("New");
    }

    @Test
    void given_codeProtectionStatement_when_buildAttributesMap_then_return_properMap() {
        ApplicationProperties properties = new ApplicationProperties(new String[]{
                "codeProtection=STATEMENT",
                "startDate=2017-10-19",
                "endDate=2017-12-20",
                "itemFileNamePrefix=custom",
                "toolkitUsername=xxx",
        });
        uploader = new DiffUploader(properties);
        Map<String, String> map = uploader.buildAttributesMap("viewName");

        LocalDateTime actualSubmissionDate = LocalDateTime.parse(map.get("SubmissionDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        assertThat(map).hasSize(8);
        assertThat(map.get("Title")).isEqualTo("custom-2017-december-20171019-20171220");
        assertThat(map.get("Employee")).isEqualTo("-1;#xxx@netcompany.com");
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
    void given_springEnvironment_when_setToolkitProperties_then_addPropertiesToEnvironment() {
        ApplicationProperties properties = new ApplicationProperties(new String[]{
                "toolkitUsername=xxx",
                "toolkitPassword=pass",
        });
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        MutablePropertySources mps = spy(new MutablePropertySources());
        when(env.getPropertySources()).thenReturn(mps);

        uploader = new DiffUploader(properties);
        uploader.setToolkitProperties(env);

        verify(env, times(1)).getPropertySources();
        verify(mps, times(1)).addLast(argThat(propertySource -> {
            assertThat(propertySource).isInstanceOf(PropertiesPropertySource.class);
            assertThat(propertySource.getName()).isEqualTo("toolkit");
            assertThat(propertySource.getProperty("toolkit.username")).isEqualTo("XXX");
            assertThat(propertySource.getProperty("toolkit.password")).isEqualTo("pass");
            assertThat(propertySource.getProperty("toolkit.domain")).isEqualTo("NCDMZ");
            assertThat(propertySource.getProperty("toolkit.WSUrl"))
                    .isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx");
            assertThat(propertySource.getProperty("toolkit.listName")).isEqualTo("WorkItems");
            return true;
        }));
    }
}