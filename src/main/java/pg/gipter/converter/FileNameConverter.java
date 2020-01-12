package pg.gipter.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.dao.DaoFactory;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.NameSetting;
import pg.gipter.utils.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

class FileNameConverter implements Converter {

    private final Logger logger = LoggerFactory.getLogger(FileNameConverter.class);

    private final ConfigurationDao propertiesDao;

    FileNameConverter() {
        propertiesDao = DaoFactory.getConfigurationDao();
    }

    @Override
    public boolean convert() {
        Optional<NameSetting> nameSetting = propertiesDao.loadFileNameSetting();

        if (nameSetting.isPresent()) {
            for (Map.Entry<String, Properties> entry : propertiesDao.loadAllConfigs().entrySet()) {
                String itemFileName = entry.getValue().getProperty(ArgName.itemFileNamePrefix.name());
                if (!StringUtils.nullOrEmpty(itemFileName)) {
                    logger.info("Conversion of the old file name [{}] into new format.", itemFileName);
                    for (Map.Entry<String, NamePatternValue> settingEntry : nameSetting.get().getNameSettings().entrySet()) {
                        String wordToReplace = settingEntry.getKey().substring(1, settingEntry.getKey().length() - 1);
                        itemFileName = itemFileName.replaceAll(wordToReplace, settingEntry.getValue().name());
                    }
                    entry.getValue().setProperty(ArgName.itemFileNamePrefix.name(), itemFileName);
                    propertiesDao.saveRunConfig(entry.getValue());
                    logger.info("Item file name converted. New item file name is [{}].", itemFileName);
                } else {
                    logger.info("Configuration [{}] does use custom file names.", entry.getValue().getProperty(ArgName.configurationName.name()));
                }
            }
            propertiesDao.removeFileNameSetting();
            return true;
        }
        logger.info("No need to convert item file name.");
        return false;
    }

}
