package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.ToolkitConfig;

import java.lang.reflect.Type;

class PasswordSerializer implements JsonSerializer<Configuration> {

    private static class PasswordSerializerHolder {
        private static final PasswordSerializer INSTANCE = new PasswordSerializer();
    }

    private PasswordSerializer() {
    }

    public static PasswordSerializer getInstance() {
        return PasswordSerializerHolder.INSTANCE;
    }

    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext serializationContext) {
        Configuration result = new Configuration();
        result.setAppConfig(configuration.getAppConfig());
        result.setCipherDetails(configuration.getCipherDetails());
        result.setToolkitConfig(processToolkitConfig(configuration));
        result.setRunConfigs(configuration.getRunConfigs());
        return new Gson().toJsonTree(result, Configuration.class);
    }

    private ToolkitConfig processToolkitConfig(Configuration configuration) {
        ToolkitConfig result = configuration.getToolkitConfig();
        if (configuration.getToolkitConfig() != null) {
            result = new ToolkitConfig(configuration.getToolkitConfig());
        }
        return result;
    }

}
