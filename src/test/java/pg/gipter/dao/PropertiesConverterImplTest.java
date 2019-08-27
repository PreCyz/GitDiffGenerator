package pg.gipter.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ArgName;
import pg.gipter.utils.PasswordUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesConverterImplTest {

    private PropertiesDaoImpl propertiesDao;
    private PropertiesConverterImpl propertiesConverter;

    @BeforeEach
    void setUp() {
        propertiesDao = new PropertiesDaoImpl();
        propertiesConverter = new PropertiesConverterImpl();
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
        propertiesDao.saveProperties(properties, PropertiesDaoImpl.APPLICATION_PROPERTIES);
        propertiesDao.saveProperties(properties, PropertiesDaoImpl.UI_APPLICATION_PROPERTIES);
        assertThat(Files.exists(Paths.get(PropertiesDaoImpl.APPLICATION_PROPERTIES))).isTrue();
        assertThat(Files.exists(Paths.get(PropertiesDaoImpl.UI_APPLICATION_PROPERTIES))).isTrue();

        propertiesConverter.convertPropertiesToNewFormat();

        assertThat(Files.exists(Paths.get(PropertiesDaoImpl.APPLICATION_PROPERTIES))).isFalse();
        assertThat(Files.exists(Paths.get(PropertiesDaoImpl.UI_APPLICATION_PROPERTIES))).isFalse();
        Map<String, Properties> actual = propertiesDao.loadAllApplicationProperties();
        assertThat(actual).hasSize(2);
        assertThat(actual.keySet()).containsExactly(PropertiesDaoImpl.APPLICATION_PROPERTIES, PropertiesDaoImpl.UI_APPLICATION_PROPERTIES);
    }
}