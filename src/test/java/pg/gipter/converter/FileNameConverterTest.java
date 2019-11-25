package pg.gipter.converter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.dao.DaoConstants;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.NameSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/** Created by Pawel Gawedzki on 25-Nov-2019. */
class FileNameConverterTest {

    private PropertiesDao propertiesDao;
    private FileNameConverter converter;

    @BeforeEach
    void setUp() {
        propertiesDao = DaoFactory.getPropertiesDao();
        converter = new FileNameConverter();
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
    void givenCustomName_whenConvert_thenReturnTrueAndSaveProperName() {
        NameSetting nameSetting = new NameSetting();
        nameSetting.addSetting("{cm}", NamePatternValue.CURRENT_MONTH_NAME);
        nameSetting.addSetting("{cy}", NamePatternValue.CURRENT_YEAR);
        propertiesDao.saveFileNameSetting(nameSetting);

        String configurationName = "test";
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
        properties.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        properties.setProperty(ArgName.configurationName.name(), configurationName);
        properties.setProperty(ArgName.itemFileNamePrefix.name(), "y-{cm}-{cy}");

        propertiesDao.saveRunConfig(properties);
        propertiesDao.saveToolkitSettings(properties);

        boolean actualResult = converter.convert();

        assertThat(actualResult).isTrue();
        Optional<Properties> actual = propertiesDao.loadApplicationProperties(configurationName);
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getProperty(ArgName.itemFileNamePrefix.name())).isEqualTo("y-{CURRENT_MONTH_NAME}-{CURRENT_YEAR}");
    }

    @Test
    void givenCustomNameAndRegularItemFileName_whenConvert_thenReturnTrueAndSaveProperName() {
        NameSetting nameSetting = new NameSetting();
        nameSetting.addSetting("{cm}", NamePatternValue.CURRENT_MONTH_NAME);
        nameSetting.addSetting("{cy}", NamePatternValue.CURRENT_YEAR);
        propertiesDao.saveFileNameSetting(nameSetting);

        String configurationName = "test";
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
        properties.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        properties.setProperty(ArgName.configurationName.name(), configurationName);
        properties.setProperty(ArgName.itemFileNamePrefix.name(), "y");

        propertiesDao.saveRunConfig(properties);
        propertiesDao.saveToolkitSettings(properties);

        boolean actualResult = converter.convert();

        assertThat(actualResult).isTrue();
        Optional<Properties> actual = propertiesDao.loadApplicationProperties(configurationName);
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getProperty(ArgName.itemFileNamePrefix.name())).isEqualTo("y");
    }

    @Test
    void givenCustomNameAndNoItemFileName_whenConvert_thenReturnTrueAndSaveProperName() {
        NameSetting nameSetting = new NameSetting();
        nameSetting.addSetting("{cm}", NamePatternValue.CURRENT_MONTH_NAME);
        nameSetting.addSetting("{cy}", NamePatternValue.CURRENT_YEAR);
        propertiesDao.saveFileNameSetting(nameSetting);

        String configurationName = "test";
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
        properties.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        properties.setProperty(ArgName.configurationName.name(), configurationName);
        properties.setProperty(ArgName.itemFileNamePrefix.name(), "");

        propertiesDao.saveRunConfig(properties);
        propertiesDao.saveToolkitSettings(properties);

        boolean actualResult = converter.convert();

        assertThat(actualResult).isTrue();
        Optional<Properties> actual = propertiesDao.loadApplicationProperties(configurationName);
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getProperty(ArgName.itemFileNamePrefix.name())).isNull();
    }

    @Test
    void givenNoCustomName_whenConvert_thenReturnFalse() {
        String configurationName = "test";
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
        properties.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        properties.setProperty(ArgName.configurationName.name(), configurationName);
        properties.setProperty(ArgName.itemFileNamePrefix.name(), "aa");

        propertiesDao.saveRunConfig(properties);
        propertiesDao.saveToolkitSettings(properties);

        boolean actualResult = converter.convert();

        assertThat(actualResult).isFalse();
    }

    @Test
    void givenCustomNames_whenConvert_thenReturnTrueAndDeleteCustomNameSettings() {
        NameSetting nameSetting = new NameSetting();
        nameSetting.addSetting("{cm}", NamePatternValue.CURRENT_MONTH_NAME);
        nameSetting.addSetting("{cy}", NamePatternValue.CURRENT_YEAR);
        propertiesDao.saveFileNameSetting(nameSetting);

        String configurationName = "test";
        Properties properties = new Properties();
        properties.setProperty(ArgName.toolkitUsername.name(), ArgName.toolkitUsername.defaultValue());
        properties.setProperty(ArgName.toolkitPassword.name(), ArgName.toolkitPassword.defaultValue());
        properties.setProperty(ArgName.configurationName.name(), configurationName);
        properties.setProperty(ArgName.itemFileNamePrefix.name(), "y-{cm}-{cy}");

        propertiesDao.saveRunConfig(properties);
        propertiesDao.saveToolkitSettings(properties);

        boolean actualResult = converter.convert();

        assertThat(actualResult).isTrue();
        Optional<NameSetting> actualNameSetting = propertiesDao.loadFileNameSetting();
        assertThat(actualNameSetting.isPresent()).isFalse();
    }
}