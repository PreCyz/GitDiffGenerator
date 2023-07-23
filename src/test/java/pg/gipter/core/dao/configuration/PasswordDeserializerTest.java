package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
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
    }

    @Test
    void givenEmptyPassword_whenDeserialize_thenReturnToolkitConfigWithoutPassword() {
        JsonObject toolkitConfig = new JsonObject();
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
    }

    @Test
    void givenNoCipherAndSimpleEncryptedPassword_whenDeserialize_thenReturnToolkitConfigWithDecryptedPassword() {
        String decryptedPassword = "somePassword";
        String encryptedPassword = CryptoUtils.encryptSafe(decryptedPassword);
        JsonObject toolkitConfig = new JsonObject();
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
    }

    @Test
    void givenCipherAndEncryptedPassword_whenDeserialize_thenReturnToolkitConfigWithDecryptedPassword() {
        String decryptedPassword = "somePassword";
        SecurityService securityService = SecurityService.getInstance();
        CipherDetails cipherDetails = securityService.generateCipherDetails();
        JsonElement cipherDetailsJsonElement = new Gson().toJsonTree(cipherDetails, CipherDetails.class);
        String encryptedPassword = securityService.encrypt(decryptedPassword, cipherDetails);
        JsonObject toolkitConfig = new JsonObject();
        JsonObject configuration = new JsonObject();
        configuration.add(ToolkitConfig.TOOLKIT_CONFIG, toolkitConfig);
        configuration.add("cipherDetails", cipherDetailsJsonElement);

        Configuration actual = deserializer.deserialize(configuration, Configuration.class, null);

        assertThat(actual.getToolkitConfig()).isNotNull();
    }

    @Test
    void givenConfigurationWithoutSharePointConfig_whenDeserialize_thenDoNotDecryptTheSharePointConfigPassword() {
        Configuration configuration = new Configuration();
        configuration.addRunConfig(new RunConfigBuilder()
                .withConfigurationName("name")
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .create()
        );
        Configuration actual = deserializer.deserialize(new Gson().toJsonTree(configuration), Configuration.class, null);

        assertThat(actual).isNotNull();
    }

    @Test
    void givenConfigurationWithEmptySharePointConfig_whenDeserialize_thenReturnSharePointConfigPasswordIsDefault() {
        Configuration configuration = new Configuration();
        SharePointConfig sharePointConfig = new SharePointConfig();
        sharePointConfig.setPassword(CryptoUtils.encryptSafe("somePassword"));
        configuration.addRunConfig(new RunConfigBuilder()
                .withConfigurationName("name")
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withSharePointConfigs(Stream.of(sharePointConfig).collect(toCollection(LinkedHashSet::new)))
                .create()
        );
        Configuration actual = deserializer.deserialize(new Gson().toJsonTree(configuration), Configuration.class, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getRunConfigs()).hasSize(1);
        assertThat(actual.getRunConfigs().get(0).getSharePointConfigs()).hasSize(1);
    }

    @Test
    void givenConfigurationWithEmptySharePointConfig_whenDeserialize_thenDecryptSharePointConfigPasswordProperly() {
        String testPassword = "testPassword";
        Configuration configuration = new Configuration();
        SharePointConfig sharePointConfig = new SharePointConfig();
        sharePointConfig.setPassword(CryptoUtils.encryptSafe(testPassword));
        configuration.addRunConfig(new RunConfigBuilder()
                .withConfigurationName("name")
                .withItemType(ItemType.SHARE_POINT_DOCS)
                .withSharePointConfigs(Stream.of(sharePointConfig).collect(toCollection(LinkedHashSet::new)))
                .create()
        );
        Configuration actual = deserializer.deserialize(new Gson().toJsonTree(configuration), Configuration.class, null);

        assertThat(actual).isNotNull();
        assertThat(actual.getRunConfigs()).hasSize(1);
        assertThat(actual.getRunConfigs().get(0).getSharePointConfigs()).hasSize(1);
        assertThat(new LinkedList<>(actual.getRunConfigs().get(0).getSharePointConfigs()).getFirst().getPassword())
                .isEqualTo(testPassword);
    }
}