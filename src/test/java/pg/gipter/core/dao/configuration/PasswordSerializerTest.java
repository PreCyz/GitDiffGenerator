package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;

class PasswordSerializerTest {

    PasswordSerializer serializer = PasswordSerializer.getInstance();

    @Test
    void givenNoToolkitConfig_whenSerialize_thenNoToolkitConfigAfterWards() {
        Configuration configuration = new Configuration();

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        assertThat(actual.getAsJsonObject().get(ToolkitConfig.TOOLKIT_CONFIG)).isNull();
    }

    @Test
    void givenToolkitConfigWithEmptyPassword_whenSerialize_thenReturnConfigurationWithToolkitConfig() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitPassword("");
        Configuration configuration = new Configuration();
        configuration.setToolkitConfig(toolkitConfig);

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonObject actualElement = actual.getAsJsonObject().getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        assertThat(actualElement.get(ArgName.toolkitPassword.name()).getAsString()).isEmpty();
    }

    @Test
    void givenToolkitConfigWithNullPassword_whenSerialize_thenReturnConfigurationWithNullPassword() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitPassword(null);
        Configuration configuration = new Configuration();
        configuration.setToolkitConfig(toolkitConfig);

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonObject actualElement = actual.getAsJsonObject().getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        assertThat(actualElement.get(ArgName.toolkitPassword.name())).isNull();
    }

    @Test
    void givenToolkitConfigAndGeneratedCipher_whenSerialize_thenReturnConfigurationWithDecryptedPassword() {
        CipherDetails cipherDetails = SecurityService.getInstance().generateCipherDetails();
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitPassword("somePassword");
        Configuration configuration = new Configuration();
        configuration.setToolkitConfig(toolkitConfig);
        configuration.setCipherDetails(cipherDetails);

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonObject actualElement = actual.getAsJsonObject().getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        assertThat(actualElement.get(ArgName.toolkitPassword.name())).isNotNull();
        assertThat(actualElement.get(ArgName.toolkitPassword.name()).getAsString()).isNotEqualTo("somePassword");
    }

    @Test
    void givenToolkitConfigWithPasswordAndNoGeneratedCipher_whenSerialize_thenReturnConfigurationWithSimplyDecryptedPassword() {
        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitPassword("somePassword");
        Configuration configuration = new Configuration();
        configuration.setToolkitConfig(toolkitConfig);

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonObject actualElement = actual.getAsJsonObject().getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        assertThat(actualElement.get(ArgName.toolkitPassword.name())).isNotNull();
        assertThat(actualElement.get(ArgName.toolkitPassword.name()).getAsString()).isEqualTo(CryptoUtils.encryptSafe("somePassword"));
    }

    @Test
    void givenRunConfigsWithSharePointConfigsAndSharePointPassword_whenSerialize_thenEncryptSharePointPassword() {
        SharePointConfig spc1 = new SharePointConfig("user1", "password1", "domain", "url", "fullUrl");
        spc1.setProject("project1");
        SharePointConfig spc2 = new SharePointConfig("user2", "password2", "domain", "url", "fullUrl");
        spc2.setProject("project2");

        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withConfigurationName("conf1")
                .withSharePointConfigs(Stream.of(spc1, spc2).collect(toCollection(LinkedHashSet::new)))
                .create();

        Configuration configuration = new Configuration();
        configuration.setRunConfigs(Collections.singletonList(runConfig));

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonArray actualElement = actual.getAsJsonObject().getAsJsonArray(RunConfig.RUN_CONFIGS);
        assertThat(actualElement).hasSize(1);
        JsonArray spcArray = actualElement.get(0).getAsJsonObject().get(SharePointConfig.SHARE_POINT_CONFIGS).getAsJsonArray();
        assertThat(spcArray).hasSize(2);
        assertThat(spcArray.get(0).getAsJsonObject().get("password").getAsString()).isNotEqualTo("password1");
        assertThat(spcArray.get(0).getAsJsonObject().get("password").getAsString().length()).isGreaterThan("password1".length());
        assertThat(spcArray.get(1).getAsJsonObject().get("password").getAsString()).isNotEqualTo("password2");
        assertThat(spcArray.get(1).getAsJsonObject().get("password").getAsString().length()).isGreaterThan("password2".length());
    }

    @Test
    void givenRunConfigsWithSharePointConfigsWithoutPassword_whenSerialize_thenDoNothing() {
        SharePointConfig spc1 = new SharePointConfig("user1", "", "domain", "url", "fullUrl");
        spc1.setProject("project1");

        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withConfigurationName("conf1")
                .withSharePointConfigs(Stream.of(spc1).collect(toCollection(LinkedHashSet::new)))
                .create();

        Configuration configuration = new Configuration();
        configuration.setRunConfigs(Collections.singletonList(runConfig));

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonArray actualElement = actual.getAsJsonObject().getAsJsonArray(RunConfig.RUN_CONFIGS);
        assertThat(actualElement).hasSize(1);
        JsonArray spcArray = actualElement.get(0).getAsJsonObject().get(SharePointConfig.SHARE_POINT_CONFIGS).getAsJsonArray();
        assertThat(spcArray).hasSize(1);
        assertThat(spcArray.get(0).getAsJsonObject().get("password").getAsString()).isEmpty();
    }

    @Test
    void givenRunConfigsWithEmptySharePointConfig_whenSerialize_thenProduceEncryptedDefaultPassword() {
        SharePointConfig spc1 = new SharePointConfig();

        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withConfigurationName("conf1")
                .withSharePointConfigs(Stream.of(spc1).collect(toCollection(LinkedHashSet::new)))
                .create();

        Configuration configuration = new Configuration();
        configuration.setRunConfigs(Collections.singletonList(runConfig));

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonArray actualElement = actual.getAsJsonObject().getAsJsonArray(RunConfig.RUN_CONFIGS);
        assertThat(actualElement).hasSize(1);
        JsonArray spcArray = actualElement.get(0).getAsJsonObject().get(SharePointConfig.SHARE_POINT_CONFIGS).getAsJsonArray();
        assertThat(spcArray).hasSize(1);
        assertThat(spcArray.get(0).getAsJsonObject().get("password").getAsString())
                .isEqualTo(CryptoUtils.encryptSafe(ArgName.toolkitPassword.defaultValue()));
    }

    @Test
    void givenRunConfigsWithoutSharePointConfigs_whenSerialize_thenDoNothing() {
        RunConfig runConfig = new RunConfigBuilder()
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withConfigurationName("conf1")
                .create();

        Configuration configuration = new Configuration();
        configuration.setRunConfigs(Collections.singletonList(runConfig));

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        JsonArray actualElement = actual.getAsJsonObject().getAsJsonArray(RunConfig.RUN_CONFIGS);
        assertThat(actualElement).hasSize(1);
        JsonElement element = actualElement.get(0).getAsJsonObject().get(SharePointConfig.SHARE_POINT_CONFIGS);
        assertThat(element).isNotNull();
    }
}