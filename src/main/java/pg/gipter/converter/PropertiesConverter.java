package pg.gipter.converter;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.dao.DaoFactory;
import pg.gipter.dao.PropertiesDao;
import pg.gipter.settings.ApplicationProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

class PropertiesConverter implements Converter {

    private final Logger logger = LoggerFactory.getLogger(PropertiesConverter.class);

    private final PropertiesDao propertiesDao;

    PropertiesConverter() {
        propertiesDao = DaoFactory.getPropertiesDao();
    }

    @Override
    public boolean convert() {
        JsonObject jsonObject = propertiesDao.readJsonConfig();
        if (jsonObject == null) {
            convertPropertiesToJson();
            deletePropertyFile(ApplicationProperties.APPLICATION_PROPERTIES);
            deletePropertyFile(ApplicationProperties.UI_APPLICATION_PROPERTIES);
            DaoFactory.getDataDao().convertExistingJob(propertiesDao.loadAllApplicationProperties().keySet());
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
        Optional<Properties> properties = propertiesDao.loadApplicationProperties();
        if (properties.isPresent()) {
            propertiesDao.buildAndSaveJsonConfig(properties.get(), ApplicationProperties.APPLICATION_PROPERTIES);
            nothingToConvert = false;
        }
        properties = propertiesDao.loadUIApplicationProperties();
        if (properties.isPresent()) {
            propertiesDao.buildAndSaveJsonConfig(properties.get(), ApplicationProperties.UI_APPLICATION_PROPERTIES);
            nothingToConvert = false;
        }
        if (nothingToConvert) {
            logger.info("There is no old properties to convert to JSON format.");
        }
    }
}
