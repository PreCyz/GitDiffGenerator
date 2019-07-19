package pg.gipter.utils;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ArgName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static pg.gipter.utils.PropertiesHelper.APPLICATION_PROPERTIES_JSON;

class PropertiesHelperTest {

    private PropertiesHelper propertiesHelper;

    @BeforeEach
    void setUp() {
        propertiesHelper = new PropertiesHelper();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get(APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenGeneratedProperty_whenAddAndSave_thenCreateNewJsonFile() {
        Properties properties = TestDataFactory.generateProperty();

        propertiesHelper.addAndSaveApplicationProperties(properties);

        JsonObject actual = propertiesHelper.readJsonConfig();
        assertThat(actual).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonArray(ConfigHelper.RUN_CONFIGS)).hasSize(1);
    }

    @Test
    void givenTwoPropertiesWithTheSameConfiguration_whenAddAndSaveApplicationProperties_thenCreateOnlyLastJsonConfig() {
        Properties properties = TestDataFactory.generateProperty();
        final String confName = properties.getProperty(ArgName.configurationName.name());
        propertiesHelper.encryptPassword(properties);
        propertiesHelper.addAndSaveApplicationProperties(properties);
        final String lastPeriodInDays = "1";
        properties.put(ArgName.periodInDays.name(), lastPeriodInDays);

        propertiesHelper.addAndSaveApplicationProperties(properties);

        Map<String, Properties> actual = propertiesHelper.loadAllApplicationProperties();
        assertThat(actual).hasSize(1);
        assertThat(actual.keySet()).containsExactly(confName);
        actual.forEach((k, v) -> assertThat(v.getProperty(ArgName.periodInDays.name())).isEqualTo(lastPeriodInDays));
    }

    @Test
    void givenJsonFile_whenLoadAllApplicationProperties_thenReturnMap() {
        Properties properties = TestDataFactory.generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);
        properties = TestDataFactory.generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);

        Map<String, Properties> actual = propertiesHelper.loadAllApplicationProperties();

        assertThat(actual).hasSize(2);
    }

    @Test
    void givenBothApplicationProperties_whenConvertPropertiesToJson_thenCreateJsonConfigFile() {
        Properties properties = TestDataFactory.generateProperty();
        propertiesHelper.encryptPassword(properties);
        propertiesHelper.saveProperties(properties, PropertiesHelper.APPLICATION_PROPERTIES);
        propertiesHelper.saveProperties(properties, PropertiesHelper.UI_APPLICATION_PROPERTIES);
        assertThat(Files.exists(Paths.get(PropertiesHelper.APPLICATION_PROPERTIES))).isTrue();
        assertThat(Files.exists(Paths.get(PropertiesHelper.UI_APPLICATION_PROPERTIES))).isTrue();

        propertiesHelper.convertPropertiesToNewFormat();

        assertThat(Files.exists(Paths.get(PropertiesHelper.APPLICATION_PROPERTIES))).isFalse();
        assertThat(Files.exists(Paths.get(PropertiesHelper.UI_APPLICATION_PROPERTIES))).isFalse();
        Map<String, Properties> actual = propertiesHelper.loadAllApplicationProperties();
        assertThat(actual).hasSize(2);
        assertThat(actual.keySet()).containsExactly(PropertiesHelper.APPLICATION_PROPERTIES, PropertiesHelper.UI_APPLICATION_PROPERTIES);
    }

    @Test
    void givenApplicationProperties_whenRemoveConfig_thenRemoveThatConfigFromFile() {
        Properties properties = TestDataFactory.generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);

        propertiesHelper.removeConfig(properties.getProperty(ArgName.configurationName.name()));

        JsonObject actual = propertiesHelper.readJsonConfig();
        assertThat(actual.getAsJsonArray(ConfigHelper.RUN_CONFIGS)).hasSize(0);
        assertThat(actual.getAsJsonObject(ConfigHelper.APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG)).isNotNull();
    }

    @Test
    void givenApplicationProperties_whenLoadArgumentArray_thenRemoveThatConfigFromFile() {
        Properties properties = TestDataFactory.generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);

        String[] actual = propertiesHelper.loadArgumentArray(properties.getProperty(ArgName.configurationName.name()));

        assertThat(actual).isNotNull();
    }
}