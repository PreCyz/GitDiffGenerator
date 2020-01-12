package pg.gipter.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.TestDataFactory;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigHelperTest {

    private ConfigHelper configHelper;

    @BeforeEach
    void setUp() {
        configHelper = new ConfigHelper();
    }

    @Test
    void givenProperties_whenBuildAppConfig_thenCreateJsonObject() {
        Properties properties = TestDataFactory.generateProperty();

        JsonObject actual = configHelper.buildAppConfig(properties).getAsJsonObject();

        assertThat(actual).isNotNull();
        for (String propertyName : ConfigHelper.APP_CONFIG_PROPERTIES) {
            assertThat(actual.get(propertyName).getAsString()).isEqualTo(properties.getProperty(propertyName));
        }
    }

    @Test
    void givenProperties_whenBuildToolkitConfig_thenCreateJsonObject() {
        Properties properties = TestDataFactory.generateProperty();

        JsonObject actual = configHelper.buildToolkitConfig(properties).getAsJsonObject();

        assertThat(actual).isNotNull();
        for (String propertyName : ConfigHelper.TOOLKIT_CONFIG_PROPERTIES) {
            assertThat(actual.get(propertyName).getAsString()).isEqualTo(properties.getProperty(propertyName));
        }
    }

    @Test
    void givenProperties_whenBuildRunConfigs_thenCreateJsonObject() {
        Properties properties = TestDataFactory.generateProperty();

        JsonArray actual = configHelper.buildRunConfigs(properties).getAsJsonArray();

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(1);
        JsonObject data = actual.get(0).getAsJsonObject();
        for (String propertyName : ConfigHelper.RUN_CONFIG_PROPERTIES) {
            JsonElement value = data.get(propertyName);
            if (value != null) {
                assertThat(value.getAsString()).isEqualTo(properties.getProperty(propertyName));
            }
        }
    }

    @Test
    void givenProperties_whenBuildFullJson_thenCreateJsonObject() {
        Properties properties = TestDataFactory.generateProperty();

        JsonObject actual = configHelper.buildFullJson(properties);

        assertThat(actual).isNotNull();
        assertThat(actual.get(ConfigHelper.APP_CONFIG)).isNotNull();
        assertThat(actual.get(ConfigHelper.TOOLKIT_CONFIG)).isNotNull();
        assertThat(actual.get(ConfigHelper.RUN_CONFIGS)).isNotNull();
    }
}