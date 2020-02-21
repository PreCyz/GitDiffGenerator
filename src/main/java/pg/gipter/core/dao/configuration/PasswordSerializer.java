package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import pg.gipter.core.dto.Configuration;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.StringUtils;

import java.lang.reflect.Type;

class PasswordSerializer implements JsonSerializer<Configuration> {

    private SecurityService securityService;

    PasswordSerializer() {
        securityService = new SecurityService();
    }

    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext serializationContext) {
        Configuration result = configuration;
        if (configuration.getToolkitConfig() != null) {
            ToolkitConfig toolkitConfig = new ToolkitConfig(configuration.getToolkitConfig());
            String password = toolkitConfig.getToolkitPassword();
            if (!StringUtils.nullOrEmpty(password)) {
                toolkitConfig.setToolkitPassword(securityService.encrypt(password));
                result = new Configuration();
                result.setToolkitConfig(toolkitConfig);
                result.setAppConfig(configuration.getAppConfig());
                result.setRunConfigs(configuration.getRunConfigs());
            }
        }
        return new Gson().toJsonTree(result, Configuration.class);
    }

}
