package pg.gipter.services.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Validates the actual signed-in principal, not a directory lookup of the requested email. */
public final class ToolkitIdentity {
    private ToolkitIdentity() { }

    public static boolean matches(int status, String contentType, String body, String expectedEmail) {
        if (status != 200 || contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).contains("json")) {
            return false;
        }
        try {
            JsonObject user = JsonParser.parseString(body).getAsJsonObject();
            if (user.has("d")) user = user.getAsJsonObject("d");
            return user.get("Id").getAsInt() > 0
                    && expectedEmail.equalsIgnoreCase(user.get("Email").getAsString());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
