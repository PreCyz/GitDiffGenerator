package pg.gipter.core.dao.configuration;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.ToolkitConfig;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordSerializerTest {

    PasswordSerializer serializer = PasswordSerializer.getInstance();

    @Test
    void givenNoToolkitConfig_whenSerialize_thenNoToolkitConfigAfterWards() {
        Configuration configuration = new Configuration();

        JsonElement actual = serializer.serialize(configuration, Configuration.class, null);

        assertThat(actual.getAsJsonObject().get(ToolkitConfig.TOOLKIT_CONFIG)).isNull();
    }

}