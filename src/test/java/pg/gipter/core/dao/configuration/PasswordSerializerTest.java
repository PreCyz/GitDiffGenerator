package pg.gipter.core.dao.configuration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.RunConfigBuilder;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producers.command.ItemType;

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
    void givenRunConfigsWithSharePointConfigsAndSharePointPassword_whenSerialize_thenEncryptSharePointPassword() {
        SharePointConfig spc1 = new SharePointConfig("url", "fullUrl");
        spc1.setProject("project1");
        SharePointConfig spc2 = new SharePointConfig("url", "fullUrl");
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
        SharePointConfig spc1 = new SharePointConfig("url", "fullUrl");
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