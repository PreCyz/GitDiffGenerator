package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import pg.gipter.core.ArgName;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.service.SecurityService;

import java.lang.reflect.Type;

class PasswordDeserializer implements JsonDeserializer<Configuration> {

    private PasswordDeserializer() { }

    private static class PasswordDeserializerHolder {
        private static final PasswordDeserializer INSTANCE = new PasswordDeserializer();
    }

    public static PasswordDeserializer getInstance() {
        return PasswordDeserializerHolder.INSTANCE;
    }

    @Override
    public Configuration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext deserializationContext)
            throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject toolkitConfig = jsonObject.getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        if (toolkitConfig != null) {
            String password = toolkitConfig.get(ArgName.toolkitPassword.name()).getAsString();
            String decryptedPassword = SecurityService.getInstance().decrypt(password);
            toolkitConfig.addProperty(ArgName.toolkitPassword.name(), decryptedPassword);
        }
        return new Gson().fromJson(jsonObject, Configuration.class);
    }
}
