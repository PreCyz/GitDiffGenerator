package pg.gipter.converter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.TestDataFactory;
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ArgName;
import pg.gipter.utils.PasswordUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesConverterTest {

    private PropertiesDao propertiesDao;
    private PropertiesConverter propertiesConverter;

    @BeforeEach
    void setUp() {
        propertiesDao = DaoFactory.getPropertiesDao();
        propertiesConverter = new PropertiesConverter();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenBothApplicationProperties_whenConvertPropertiesToJson_thenCreateJsonConfigFile() {
        Properties properties = TestDataFactory.generateProperty();
        PasswordUtils.encryptPassword(properties, ArgName.toolkitPassword.name());
        propertiesDao.saveProperties(properties, ApplicationProperties.APPLICATION_PROPERTIES);
        propertiesDao.saveProperties(properties, ApplicationProperties.UI_APPLICATION_PROPERTIES);
        assertThat(Files.exists(Paths.get(ApplicationProperties.APPLICATION_PROPERTIES))).isTrue();
        assertThat(Files.exists(Paths.get(ApplicationProperties.UI_APPLICATION_PROPERTIES))).isTrue();

        propertiesConverter.convert();

        assertThat(Files.exists(Paths.get(ApplicationProperties.APPLICATION_PROPERTIES))).isFalse();
        assertThat(Files.exists(Paths.get(ApplicationProperties.UI_APPLICATION_PROPERTIES))).isFalse();
        Map<String, Properties> actual = propertiesDao.loadAllApplicationProperties();
        assertThat(actual).hasSize(2);
        assertThat(actual.keySet()).containsExactly(ApplicationProperties.APPLICATION_PROPERTIES, ApplicationProperties.UI_APPLICATION_PROPERTIES);
    }
}