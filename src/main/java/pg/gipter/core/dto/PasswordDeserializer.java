package pg.gipter.core.dto;

import com.google.gson.*;
import pg.gipter.core.ArgName;
import pg.gipter.utils.PasswordUtils;

import java.lang.reflect.Type;

class PasswordDeserializer implements JsonDeserializer<ToolkitConfig> {

    @Override
    public ToolkitConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        ToolkitConfig toolkitConfig = new ToolkitConfig();
        toolkitConfig.setToolkitUsername(jsonObject.get(ArgName.toolkitUsername.name()).getAsString());
        toolkitConfig.setToolkitPassword(PasswordUtils.decrypt(jsonObject.get(ArgName.toolkitPassword.name()).getAsString()));
        return toolkitConfig;
    }
}
