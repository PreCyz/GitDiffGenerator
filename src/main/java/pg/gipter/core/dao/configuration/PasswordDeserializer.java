package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.services.SecurityService;
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
        Gson gson = new Gson();

        JsonArray runConfigs = configuration.getAsJsonArray(RunConfig.RUN_CONFIGS);
        if (runConfigs != null && !runConfigs.isEmpty()) {
            for (int rci = 0; rci < runConfigs.size(); ++rci) {
                JsonElement runConfig = runConfigs.get(rci);
                if (runConfig != null) {
                    JsonElement sharePointConfigs = runConfig.getAsJsonObject().get(SharePointConfig.SHARE_POINT_CONFIGS);
                    if (sharePointConfigs != null) {
                        JsonArray spcArray = sharePointConfigs.getAsJsonArray();
                        for (int i = 0; i < spcArray.size(); ++i) {
                            JsonObject spc = spcArray.get(i).getAsJsonObject();
                            JsonElement encryptedPassword = spc.get(SharePointConfig.PASSWORD_MEMBER_NAME);
                            if (encryptedPassword != null) {
                                spc.addProperty(
                                        SharePointConfig.PASSWORD_MEMBER_NAME,
                                        decryptPassword(configuration, encryptedPassword.getAsString())
                                );
                            }
                        }
                    }
                }
            }
        }
        return gson.fromJson(configuration, Configuration.class);
    }

    private String decryptPassword(JsonObject configuration, String password) {
        String decryptedPassword;
        if (configuration.get(CipherDetails.CIPHER_DETAILS) != null) {
            CipherDetails cipherDetails = new Gson().fromJson(
                    configuration.get(CipherDetails.CIPHER_DETAILS).getAsJsonObject(), CipherDetails.class
            );
            decryptedPassword = SecurityService.getInstance().decrypt(password, cipherDetails);
        } else {
            decryptedPassword = CryptoUtils.decryptSafe(password);
        }
        return decryptedPassword;
    }
}
