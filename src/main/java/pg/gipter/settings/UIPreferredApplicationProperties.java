package pg.gipter.settings;

import pg.gipter.util.PropertiesHelper;
import pg.gipter.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

/**Created by Pawel Gawedzki on 06-Mar-2019.*/
class UIPreferredApplicationProperties extends CliPreferredApplicationProperties {

    UIPreferredApplicationProperties(String[] args) {
        super(args);
    }

    protected void init(String[] args, PropertiesHelper propertiesHelper) {
        Optional<Properties> propsFromFile = propertiesHelper.loadUIApplicationProperties();
        if (propsFromFile.isPresent()) {
            properties = propsFromFile.get();
            logger.info("Properties from [{}] file loaded.", PropertiesHelper.UI_APPLICATION_PROPERTIES);
        } else {
            propsFromFile = propertiesHelper.loadApplicationProperties();
            if (propsFromFile.isPresent()) {
                properties = propsFromFile.get();
                logger.info("Properties from [{}] file loaded.", PropertiesHelper.APPLICATION_PROPERTIES);
            } else {
                logger.warn("Can not load [{}] and [{}].",
                        PropertiesHelper.UI_APPLICATION_PROPERTIES, PropertiesHelper.APPLICATION_PROPERTIES
                );
                logger.info("Command line argument loaded: {}.", Arrays.toString(args));
            }
        }
        logger.info("Application properties loaded: {}.", log());
    }

    @Override
    public boolean isUseUI() {
        return true;
    }

    @Override
    public boolean isActiveTray() {
        boolean activeTray = argExtractor.isActiveTray();
        String argName = ArgName.activeTray.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            activeTray = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(activeTray)));
        }
        return activeTray;
    }

}
