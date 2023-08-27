package pg.gipter.converters;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.model.ToolkitConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SSOPasswordConverter extends SecurityConverter {

    private final Logger logger = LoggerFactory.getLogger(SSOPasswordConverter.class);

    public SSOPasswordConverter() {
        super();
    }

    @Override
    public boolean convert() {
        boolean result = false;
        Optional<JsonObject> jsonObject = readToolkitConfigAsJsonObject();
        if (jsonObject.isPresent() && jsonObject.get().has("toolkitPassword")) {
            String toolkitPassword = jsonObject.get().getAsJsonPrimitive("toolkitPassword").getAsString();
            Optional<ToolkitConfig> toolkitConfig = readToolkitConfig();
            if (toolkitConfig.isPresent()) {
                toolkitConfig.get().setToolkitSSOPassword(toolkitPassword);
                cachedConfiguration.saveToolkitConfig(toolkitConfig.get());
                result = true;
            }
        } else {
            logger.info("There is not toolkit credentials to convert.");
        }
        return result;
}

    protected Optional<JsonObject> readToolkitConfigAsJsonObject() {
        Optional<JsonObject> result = Optional.empty();
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            Gson gson = new GsonBuilder().create();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            JsonObject toolkitConfigJsonObject = config.getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
            result = Optional.ofNullable(toolkitConfigJsonObject);
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not read toolkit config. {}", e.getMessage());
        }
        return result;
    }

}
