package pg.gipter.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class ItemTypeConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(ItemTypeConverter.class);

    ItemTypeConverter() {
    }

    @Override
    public boolean convert() {
        Optional<JsonObject> jsonObject = replaceUploadType();
        if (jsonObject.isPresent()) {
            saveConvertedObject(jsonObject.get());
        } else {
            logger.info("There is nothing to convert.");
        }
        return jsonObject.isPresent();
    }

    private Optional<JsonObject> replaceUploadType() {
        Optional<JsonObject> result = Optional.empty();
        Gson gson = new GsonBuilder().create();
        boolean uploadTypeReplaced = false;
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject.has("runConfigs")) {
                final JsonArray runConfigs = jsonObject.getAsJsonArray("runConfigs");
                if (runConfigs.size() > 0) {
                    for (int i = 0; i < runConfigs.size(); i++) {
                        final JsonObject runConfig = runConfigs.get(i).getAsJsonObject();
                        if (runConfig.has("uploadType")) {
                            runConfig.addProperty("itemType", runConfig.get("uploadType").getAsString());
                            logger.info("UploadType converted into ItemType, value [{}]",
                                    runConfig.get("uploadType").getAsString());
                            runConfig.remove("uploadType");
                            uploadTypeReplaced = true;
                        }
                    }
                }
            }
            if (uploadTypeReplaced) {
                result = Optional.of(jsonObject);
            }
        } catch (IOException | NullPointerException e) {
            logger.warn("Warning when loading {}. Exception message is: {}",
                    DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
        }
        return result;
    }

    private void saveConvertedObject(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStream os = new FileOutputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            gson.toJson(jsonObject, writer);
            logger.info("File {} saved after conversion.", DaoConstants.APPLICATION_PROPERTIES_JSON);
        } catch (IOException e) {
            logger.error("Error when writing {}. Exception message is: {}",
                    DaoConstants.APPLICATION_PROPERTIES_JSON, e.getMessage());
            throw new IllegalArgumentException("Error when writing configuration into json.");
        }
    }
}
