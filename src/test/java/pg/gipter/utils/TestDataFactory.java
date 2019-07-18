package pg.gipter.utils;

import org.jetbrains.annotations.NotNull;
import pg.gipter.settings.ArgName;

import java.util.Properties;
import java.util.Random;

public class TestDataFactory {

    private TestDataFactory() { }

    @NotNull
    static Properties generateProperty() {
        Properties properties = new Properties();
        for (ArgName argName : ArgName.values()) {
            properties.put(argName.name(), argName.defaultValue());
        }
        properties.put(ArgName.configurationName.name(), String.valueOf(new Random().nextInt(100)));
        return properties;
    }
}
