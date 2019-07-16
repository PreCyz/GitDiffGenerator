package pg.gipter.settings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pg.gipter.utils.PropertiesHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class MultiApplicationPropertiesTest {

    private MultiApplicationProperties multiApplicationProperties;

    @BeforeEach
    void setUp() {
        multiApplicationProperties = new MultiApplicationProperties();
        Properties properties = new Properties();
        for (ArgName argName : ArgName.values()) {
            properties.put(argName.name(), argName.defaultValue());
        }
        properties.put(ArgName.configurationName.name(), String.valueOf(new Random().nextInt(100)));
        new PropertiesHelper().addAndSaveApplicationProperties(properties);
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(Paths.get("applicationProperties.json"));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void givenJsonPropertyFile_when_thenReturnProperMap() {
        Map<String, ApplicationProperties> actualMap = multiApplicationProperties.getApplicationPropertiesMap();

        assertThat(actualMap).hasSize(1);
        actualMap.forEach((k, v) -> assertThat(k).isEqualTo(v.configurationName()));
    }
}