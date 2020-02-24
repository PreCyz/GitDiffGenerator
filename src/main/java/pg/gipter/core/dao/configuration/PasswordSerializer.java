package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.StringUtils;

import java.lang.reflect.Type;

class PasswordSerializer implements JsonSerializer<Configuration> {

    private static class PasswordSerializerHolder {
        private static final PasswordSerializer INSTANCE = new PasswordSerializer();
    }

    private PasswordSerializer() { }

    public static PasswordSerializer getInstance() {
        return PasswordSerializerHolder.INSTANCE;
    }

    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext serializationContext) {
        Configuration result = configuration;
        if (configuration.getToolkitConfig() != null) {
            ToolkitConfig toolkitConfig = new ToolkitConfig(configuration.getToolkitConfig());
            String password = toolkitConfig.getToolkitPassword();
            if (!StringUtils.nullOrEmpty(password)) {
                toolkitConfig.setToolkitPassword(SecurityService.getInstance().encrypt(password));
                result = new Configuration();
                result.setToolkitConfig(toolkitConfig);
                result.setAppConfig(configuration.getAppConfig());
                result.setRunConfigs(configuration.getRunConfigs());
                result.setCipher(configuration.getCipher());
            }
        }
        return new Gson().toJsonTree(result, Configuration.class);
    }

}
