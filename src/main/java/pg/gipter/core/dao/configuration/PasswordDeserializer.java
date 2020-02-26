package pg.gipter.core.dao.configuration;

import com.google.gson.*;
import pg.gipter.core.ArgName;
import pg.gipter.core.dto.CipherDetails;
import pg.gipter.core.dto.ToolkitConfig;
import pg.gipter.service.SecurityService;
import pg.gipter.utils.CryptoUtils;

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
        JsonObject configuration = jsonElement.getAsJsonObject();
        JsonObject toolkitConfig = configuration.getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
        Gson gson = new Gson();
        if (toolkitConfig != null && toolkitConfig.get(ArgName.toolkitPassword.name()) != null) {
            SecurityService securityService = SecurityService.getInstance();
            String password = toolkitConfig.get(ArgName.toolkitPassword.name()).getAsString();
            String decryptedPassword = "";
            if (configuration.get(CipherDetails.CIPHER_DETAILS) != null) {
                CipherDetails cipherDetails = gson.fromJson(
                        configuration.get(CipherDetails.CIPHER_DETAILS).getAsJsonObject(), CipherDetails.class
                );
                decryptedPassword = securityService.decrypt(password, cipherDetails);
            } else {
                decryptedPassword = CryptoUtils.decryptSafe(password);
            }
            toolkitConfig.addProperty(ArgName.toolkitPassword.name(), decryptedPassword);
        }
        return gson.fromJson(configuration, Configuration.class);
    }
}
