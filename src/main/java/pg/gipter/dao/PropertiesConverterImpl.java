package pg.gipter.dao;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

class PropertiesConverterImpl implements PropertiesConverter {

    private final Logger logger = LoggerFactory.getLogger(PropertiesConverterImpl.class);

    private final PropertiesDaoImpl propertiesDaoImpl;

    public PropertiesConverterImpl() {
        propertiesDaoImpl = new PropertiesDaoImpl();
    }

    @Override
    public boolean convertPropertiesToNewFormat() {
        JsonObject jsonObject = propertiesDaoImpl.readJsonConfig();
        if (jsonObject == null) {
            convertPropertiesToJson();
            deletePropertyFile(PropertiesDaoImpl.APPLICATION_PROPERTIES);
            deletePropertyFile(PropertiesDaoImpl.UI_APPLICATION_PROPERTIES);
            new DataDaoImpl().convertExistingJob(propertiesDaoImpl.loadAllApplicationProperties().keySet());
            return true;
        }
        return false;
    }

    private void deletePropertyFile(String propertyFile) {
        try {
            Files.deleteIfExists(Paths.get(propertyFile));
        } catch (IOException e) {
            logger.warn("Can not delete {} file. {}", propertyFile, e.getMessage());
        }
    }

    private void convertPropertiesToJson() {
        boolean nothingToConvert = true;
        Optional<Properties> properties = propertiesDaoImpl.loadApplicationProperties();
        if (properties.isPresent()) {
            propertiesDaoImpl.buildAndSaveJsonConfig(properties.get(), PropertiesDaoImpl.APPLICATION_PROPERTIES);
            nothingToConvert = false;
        }
        properties = propertiesDaoImpl.loadUIApplicationProperties();
        if (properties.isPresent()) {
            propertiesDaoImpl.buildAndSaveJsonConfig(properties.get(), PropertiesDaoImpl.UI_APPLICATION_PROPERTIES);
            nothingToConvert = false;
        }
        if (nothingToConvert) {
            logger.info("There is no old properties to convert to JSON format.");
        }
    }
}
