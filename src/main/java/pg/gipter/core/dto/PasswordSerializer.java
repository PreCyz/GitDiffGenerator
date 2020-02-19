package pg.gipter.core.dto;

import com.google.gson.*;
import pg.gipter.utils.CryptoUtils;
import pg.gipter.utils.StringUtils;

import java.lang.reflect.Type;

class PasswordSerializer implements JsonSerializer<String> {

    @Override
    public JsonElement serialize(String value, Type type, JsonSerializationContext serializationContext) {
        JsonElement jsonElement = null;
        if (!StringUtils.nullOrEmpty(value)) {
            jsonElement = new JsonPrimitive(CryptoUtils.encryptSafe(value));
        }
        return jsonElement;
    }

}
