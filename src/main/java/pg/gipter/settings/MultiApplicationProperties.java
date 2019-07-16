package pg.gipter.settings;

import pg.gipter.utils.PropertiesHelper;

import java.util.Map;
import java.util.Properties;

import static java.util.stream.Collectors.toMap;

public class MultiApplicationProperties {

    private PropertiesHelper propertiesHelper;

    public MultiApplicationProperties() {
        propertiesHelper = new PropertiesHelper();
    }

    public Map<String, ApplicationProperties> getApplicationPropertiesMap() {
        Map<String, Properties> propertiesMap = propertiesHelper.loadAllApplicationProperties();
        return propertiesMap.entrySet()
                .stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        entry -> ApplicationPropertiesFactory.getInstance(propertiesToArray(entry.getValue()))
                ));
    }

    private String[] propertiesToArray(Properties properties) {
        String[] result = new String[properties.size()];
        int idx = 0;
        for (Object keyObj : properties.keySet()) {
            String key = String.valueOf(keyObj);
            result[idx++] = String.format("%s=%s", key, properties.getProperty(key));
        }
        return result;
    }


}
