package pg.gipter.core.dao.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.CryptoUtils;

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
}