package pg.gipter;

import pg.gipter.core.ArgName;
import pg.gipter.utils.StringUtils;

import java.util.Properties;
import java.util.Random;

public class TestDataFactory {

    private TestDataFactory() { }

    public static Properties generateProperty() {
        Properties properties = new Properties();
        for (ArgName argName : ArgName.values()) {
            if (!StringUtils.nullOrEmpty(argName.defaultValue())) {
                properties.put(argName.name(), argName.defaultValue());
            }
        }
        properties.put(ArgName.configurationName.name(), String.valueOf(new Random().nextInt(100)));
        return properties;
    }
}
