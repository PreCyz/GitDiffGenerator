package pg.gipter.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.NameSetting;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
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
        createJsonConfig(properties);

        JsonObject actual = propertiesHelper.readJsonConfig();
        assertThat(actual).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonArray(ConfigHelper.RUN_CONFIGS)).hasSize(1);
    }

    private void createJsonConfig(Properties properties) {
        propertiesHelper.saveAppSettings(properties);
        propertiesHelper.saveToolkitSettings(properties);
        propertiesHelper.saveRunConfig(properties);
    }

    @Test
    void givenJsonFile_whenLoadAllApplicationProperties_thenReturnMap() {
        Properties properties = TestDataFactory.generateProperty();
        createJsonConfig(properties);
        properties.setProperty(ArgName.configurationName.name(), "other");
        createJsonConfig(properties);

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
        createJsonConfig(properties);

        propertiesHelper.removeConfig(properties.getProperty(ArgName.configurationName.name()));

        JsonObject actual = propertiesHelper.readJsonConfig();
        assertThat(actual.getAsJsonArray(ConfigHelper.RUN_CONFIGS)).hasSize(0);
        assertThat(actual.getAsJsonObject(ConfigHelper.APP_CONFIG)).isNotNull();
        assertThat(actual.getAsJsonObject(ConfigHelper.TOOLKIT_CONFIG)).isNotNull();
    }

    @Test
    void givenApplicationProperties_whenLoadArgumentArray_thenRemoveThatConfigFromFile() {
        Properties properties = TestDataFactory.generateProperty();
        createJsonConfig(properties);

        String[] actual = propertiesHelper.loadArgumentArray(properties.getProperty(ArgName.configurationName.name()));

        assertThat(actual).isNotNull();
    }

    @Test
    void givenRunConfigWithoutName_whenSaveRunConfig_thenNoRunConfigSaved() {
        Properties properties = TestDataFactory.generateProperty();
        properties.remove(ArgName.configurationName.name());

        propertiesHelper.saveRunConfig(properties);

        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();
        assertThat(map).isEmpty();
    }

    @Test
    void givenRunConfig_whenSaveRunConfig_thenRunConfigSaved() {
        Properties properties = TestDataFactory.generateProperty();

        propertiesHelper.saveRunConfig(properties);

        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();
        assertThat(map).hasSize(1);
        assertThat(map.keySet()).containsExactly(properties.getProperty(ArgName.configurationName.name()));
    }

    @Test
    void given2SameRunConfig_whenSaveRunConfig_thenLastOneRunConfigSaved() {
        Properties properties = TestDataFactory.generateProperty();
        Properties last = TestDataFactory.generateProperty();
        last.setProperty(ArgName.configurationName.name(), properties.getProperty(ArgName.configurationName.name()));
        last.setProperty(ArgName.periodInDays.name(), "8");

        propertiesHelper.saveRunConfig(properties);
        propertiesHelper.saveRunConfig(last);


        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();
        assertThat(map).hasSize(1);
        assertThat(map.keySet()).containsExactly(properties.getProperty(ArgName.configurationName.name()));
        assertThat(map.get(properties.getProperty(ArgName.configurationName.name())).getProperty(ArgName.periodInDays.name())).isEqualTo("8");
    }

    @Test
    void givenPropertiesWithoutRunConfig_whenSaveAppConfig_thenSaveIt() {
        Properties properties = TestDataFactory.generateProperty();
        properties.remove(ArgName.configurationName.name());

        propertiesHelper.saveAppSettings(properties);

        JsonObject jsonObject = propertiesHelper.readJsonConfig();
        assertThat(jsonObject).isNotNull();
        assertThat(jsonObject.has(ConfigHelper.APP_CONFIG)).isTrue();
        assertThat(jsonObject.get(ConfigHelper.APP_CONFIG).getAsJsonObject()).isNotNull();
    }

    @Test
    void givenPropertiesWithoutRunConfig_whenSaveToolkitConfig_thenSaveIt() {
        Properties properties = TestDataFactory.generateProperty();
        properties.remove(ArgName.configurationName.name());

        propertiesHelper.saveToolkitSettings(properties);

        JsonObject jsonObject = propertiesHelper.readJsonConfig();
        assertThat(jsonObject).isNotNull();
        assertThat(jsonObject.has(ConfigHelper.TOOLKIT_CONFIG)).isTrue();
        assertThat(jsonObject.get(ConfigHelper.TOOLKIT_CONFIG).getAsJsonObject()).isNotNull();
    }

    @Test
    void givenProperties_whenLoadAllApplicationProperties_thenReturnEmptyMap() {
        Properties properties = TestDataFactory.generateProperty();
        createJsonConfig(properties);
        propertiesHelper.removeConfig(properties.getProperty(ArgName.configurationName.name()));

        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();

        assertThat(map).isEmpty();
    }

    @Test
    void givenProperties_whenSaveRunConfig_thenReturnMap() {
        Properties properties = TestDataFactory.generateProperty();

        propertiesHelper.saveRunConfig(properties);
        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();

        assertThat(map).hasSize(1);
    }

    @Test
    void given2sameRunConfig_whenSaveRunConfig_thenReturnMapWithOneConfig() {
        Properties properties = TestDataFactory.generateProperty();
        propertiesHelper.saveRunConfig(properties);
        properties.put(ArgName.periodInDays.name(), "8");
        propertiesHelper.saveRunConfig(properties);

        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();
        assertThat(map).hasSize(1);
        assertThat(map.get(properties.getProperty(ArgName.configurationName.name())).getProperty(ArgName.periodInDays.name())).isEqualTo("8");
    }

    @Test
    void givenProperties_whenSaveAppSettings_thenReturnEmptyMap() {
        propertiesHelper.writeJsonConfig(new ConfigHelper().buildFullJson(new Properties()));
        Properties properties = TestDataFactory.generateProperty();
        properties.remove(ArgName.configurationName.name());

        propertiesHelper.saveAppSettings(properties);

        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();
        assertThat(map).isEmpty();
    }

    @Test
    void givenProperties_whenSaveToolkitSettings_thenReturnEmptyMap() {
        Properties properties = TestDataFactory.generateProperty();
        properties.remove(ArgName.configurationName.name());

        propertiesHelper.saveToolkitSettings(properties);
        Map<String, Properties> map = propertiesHelper.loadAllApplicationProperties();

        assertThat(map).isEmpty();
    }

    @Test
    void givenNoConfig_whenLoadToolkitCredentials_thenReturnDefaults() {
        Properties actual = propertiesHelper.loadToolkitCredentials();

        assertThat(actual.getProperty(ArgName.toolkitUsername.name())).isEqualTo(ArgName.toolkitUsername.defaultValue());
        assertThat(actual.getProperty(ArgName.toolkitPassword.name())).isEqualTo(ArgName.toolkitPassword.defaultValue());
    }

    @Test
    void givenConfigWithoutToolkitConfig_whenLoadToolkitCredentials_thenReturnDefaults() {
        Properties properties = TestDataFactory.generateProperty();
        JsonObject jsonObject = new ConfigHelper().buildFullJson(properties);
        jsonObject.remove(ConfigHelper.TOOLKIT_CONFIG);
        propertiesHelper.writeJsonConfig(jsonObject);

        Properties actual = propertiesHelper.loadToolkitCredentials();

        assertThat(actual.getProperty(ArgName.toolkitUsername.name())).isEqualTo(ArgName.toolkitUsername.defaultValue());
        assertThat(actual.getProperty(ArgName.toolkitPassword.name())).isEqualTo(ArgName.toolkitPassword.defaultValue());
    }

    @Test
    void givenConfigWithToolkitConfig_whenLoadToolkitCredentials_thenReturnCredentialsFromConfig() {
        String username = "username";
        String password = "password";
        Properties properties = TestDataFactory.generateProperty();
        properties.setProperty(ArgName.toolkitUsername.name(), username);
        properties.setProperty(ArgName.toolkitPassword.name(), password);
        propertiesHelper.saveToolkitSettings(properties);

        Properties actual = propertiesHelper.loadToolkitCredentials();

        assertThat(actual.getProperty(ArgName.toolkitUsername.name())).isEqualTo(username);
        assertThat(actual.getProperty(ArgName.toolkitPassword.name())).isEqualTo(password);
    }

    @Test
    void givenUserNameWithPolishSigns_whenLoadToolkitCredentials_thenReturnCredentialsWithProperCoding() throws UnsupportedEncodingException {
        String username = "ęóąśłńćźż";
        Properties properties = TestDataFactory.generateProperty();
        properties.setProperty(ArgName.toolkitUsername.name(), username);
        propertiesHelper.saveToolkitSettings(properties);

        Properties actual = propertiesHelper.loadToolkitCredentials();

        assertThat(actual.getProperty(ArgName.toolkitUsername.name())).isEqualTo(username);
    }

    @Test
    void givenFileNameSetting_whenSaveFileNameSetting_thenSettingsAreSaved() {
        NameSetting fns = new NameSetting();
        fns.addSetting("{CURRENT_MONTH_NAME}", NamePatternValue.CURRENT_MONTH_NAME);
        fns.addSetting("{CURRENT_WEEK_NUMBER}", NamePatternValue.CURRENT_WEEK_NUMBER);
        fns.addSetting("{CURRENT_YEAR}", NamePatternValue.CURRENT_YEAR);
        fns.addSetting("{END_DATE}", NamePatternValue.END_DATE);
        fns.addSetting("{START_DATE}", NamePatternValue.START_DATE);

        propertiesHelper.saveFileNameSetting(fns);

        JsonObject jsonObject = propertiesHelper.readJsonConfig();
        NameSetting actual = new Gson().fromJson(jsonObject.get(ConfigHelper.FILE_NAME_SETTING), NameSetting.class);
        Map<String, NamePatternValue> nameSettings = actual.getNameSettings();
        assertThat(nameSettings).isNotEmpty();
        assertThat(nameSettings.get("{CURRENT_MONTH_NAME}")).isEqualTo(fns.getNameSettings().get("{CURRENT_MONTH_NAME}"));
        assertThat(nameSettings.get("{CURRENT_WEEK_NUMBER}")).isEqualTo(fns.getNameSettings().get("{CURRENT_WEEK_NUMBER}"));
        assertThat(nameSettings.get("{CURRENT_YEAR}")).isEqualTo(fns.getNameSettings().get("{CURRENT_YEAR}"));
        assertThat(nameSettings.get("{END_DATE}")).isEqualTo(fns.getNameSettings().get("{END_DATE}"));
        assertThat(nameSettings.get("{START_DATE}")).isEqualTo(fns.getNameSettings().get("{START_DATE}"));
    }

    @Test
    void givenFileNameSetting_whenLoadFileNameSetting_thenReturnFileNameSettings() {
        NameSetting fns = new NameSetting();
        fns.addSetting("{CURRENT_MONTH_NAME}", NamePatternValue.CURRENT_MONTH_NAME);
        fns.addSetting("{CURRENT_WEEK_NUMBER}", NamePatternValue.CURRENT_WEEK_NUMBER);
        fns.addSetting("{CURRENT_YEAR}", NamePatternValue.CURRENT_YEAR);
        fns.addSetting("{END_DATE}", NamePatternValue.END_DATE);
        fns.addSetting("{START_DATE}", NamePatternValue.START_DATE);
        propertiesHelper.saveFileNameSetting(fns);

        Optional<NameSetting> actual = propertiesHelper.loadFileNameSetting();

        assertThat(actual.isPresent()).isTrue();
        Map<String, NamePatternValue> nameSettings = actual.get().getNameSettings();
        assertThat(nameSettings).isNotEmpty();
        assertThat(nameSettings.get("{CURRENT_MONTH_NAME}")).isEqualTo(fns.getNameSettings().get("{CURRENT_MONTH_NAME}"));
        assertThat(nameSettings.get("{CURRENT_WEEK_NUMBER}")).isEqualTo(fns.getNameSettings().get("{CURRENT_WEEK_NUMBER}"));
        assertThat(nameSettings.get("{CURRENT_YEAR}")).isEqualTo(fns.getNameSettings().get("{CURRENT_YEAR}"));
        assertThat(nameSettings.get("{END_DATE}")).isEqualTo(fns.getNameSettings().get("{END_DATE}"));
        assertThat(nameSettings.get("{START_DATE}")).isEqualTo(fns.getNameSettings().get("{START_DATE}"));
    }

    @Test
    void givenNoFileNameSetting_whenLoadFileNameSetting_thenReturnEmptyOptional() {
        Optional<NameSetting> actual = propertiesHelper.loadFileNameSetting();

        assertThat(actual.isPresent()).isFalse();
    }
}