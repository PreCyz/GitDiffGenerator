package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dto.CipherDetails;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordDeserializerTest {

    PasswordDeserializer deserializer = PasswordDeserializer.getInstance();

    @BeforeEach
    void setUp() {
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenNullPassword_whenDeserialize_thenReturnToolkitConfigWithoutPassword() {
        JsonObject toolkitConfig = new JsonObject();
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
        assertThat(actual.getToolkitConfig().getToolkitPassword()).isEqualTo(ArgName.toolkitPassword.defaultValue());
    }

    @Test
    void givenEmptyPassword_whenDeserialize_thenReturnToolkitConfigWithoutPassword() {
        JsonObject toolkitConfig = new JsonObject();
        toolkitConfig.addProperty(ArgName.toolkitPassword.name(), "");
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
        assertThat(actual.getToolkitConfig().getToolkitPassword()).isEmpty();
    }

    @Test
    void givenNoCipherAndSimpleEncryptedPassword_whenDeserialize_thenReturnToolkitConfigWithDecryptedPassword() {
        String decryptedPassword = "somePassword";
        String encryptedPassword = CryptoUtils.encryptSafe(decryptedPassword);
        JsonObject toolkitConfig = new JsonObject();
        toolkitConfig.addProperty(ArgName.toolkitPassword.name(), encryptedPassword);
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
        assertThat(actual.getToolkitConfig().getToolkitPassword()).isEqualTo(decryptedPassword);
    }

    @Test
    void givenCipherAndEncryptedPassword_whenDeserialize_thenReturnToolkitConfigWithDecryptedPassword() {
        String decryptedPassword = "somePassword";
        SecurityService securityService = SecurityService.getInstance();
        CipherDetails cipherDetails = securityService.generateCipherDetails();
        JsonElement cipherDetailsJsonElement = new Gson().toJsonTree(cipherDetails, CipherDetails.class);
        String encryptedPassword = securityService.encrypt(decryptedPassword, cipherDetails);
        JsonObject toolkitConfig = new JsonObject();
        toolkitConfig.addProperty(ArgName.toolkitPassword.name(), encryptedPassword);
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);
        configuration.add("cipherDetails", cipherDetailsJsonElement);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
        assertThat(actual.getToolkitConfig().getToolkitPassword()).isEqualTo(decryptedPassword);
    }
}