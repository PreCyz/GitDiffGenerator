package pg.gipter.utils;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ArgName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

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

    @NotNull
    private Properties generateProperty() {
        Properties properties = new Properties();
        for (ArgName argName : ArgName.values()) {
            properties.put(argName.name(), argName.defaultValue());
        }
        properties.put(ArgName.configurationName.name(), String.valueOf(new Random().nextInt(100)));
        return properties;
    }

    @Test
    void givenGeneratedProperty_whenAddAndSave_thenCreateNewJsonFile() {
        Properties properties = generateProperty();

        propertiesHelper.addAndSaveApplicationProperties(properties);

        JsonArray actual = propertiesHelper.readJsonConfig();
        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(1);
    }

    @Test
    void givenJsonFile_whenLoadAllApplicationProperties_thenReturnMap() {
        Properties properties = generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);
        properties = generateProperty();
        propertiesHelper.addAndSaveApplicationProperties(properties);

        Map<String, Properties> actual = propertiesHelper.loadAllApplicationProperties();

        assertThat(actual).hasSize(2);
    }

    @Test
    void givenBothApplicationProperties_whenConvertPropertiesToJson_thenCreateJsonConfigFile() {
        Properties properties = generateProperty();
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
}