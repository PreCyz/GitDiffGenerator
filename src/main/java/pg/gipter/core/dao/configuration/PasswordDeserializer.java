package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import pg.gipter.core.ArgName;
import pg.gipter.core.dto.Configuration;
import pg.gipter.service.SecurityService;

import java.lang.reflect.Type;

class PasswordDeserializer implements JsonDeserializer<Configuration> {

    private SecurityService securityService;

    PasswordDeserializer() {
        securityService = new SecurityService();
    }

    @Override
    public Configuration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject toolkitConfig = jsonObject.getAsJsonObject(Configuration.TOOLKIT_CONFIG);
        String decryptedPassword = securityService.decrypt(toolkitConfig.get(ArgName.toolkitPassword.name()).getAsString());
        jsonObject.addProperty(ArgName.toolkitPassword.name(), decryptedPassword);
        return new Gson().fromJson(jsonObject, Configuration.class);
    }
}
