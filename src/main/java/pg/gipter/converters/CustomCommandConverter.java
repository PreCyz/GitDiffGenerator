package pg.gipter.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.command.CustomCommandDao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class CustomCommandConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(CustomCommandConverter.class);

    private final ApplicationProperties applicationProperties;

    CustomCommandConverter(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean convert() {
        Optional<CustomCommand> customCommand = CustomCommandDao.readCustomCommand();
        if (customCommand.isPresent()) {
            CustomCommand convertedCommand = applicationProperties.getCustomCommand(customCommand.get().getVcs());
            convertedCommand.setCommand(customCommand.get().getCommand());
            convertedCommand.setCommandList(customCommand.get().getCommandList());
            convertedCommand.setOverride(true);
            applicationProperties.addCustomCommand(convertedCommand);
            applicationProperties.save();
            logger.info("Custom command converted [{}].", convertedCommand);
            try {
                Files.deleteIfExists(Paths.get(DaoConstants.CUSTOM_COMMAND_JSON));
            } catch (IOException e) {
                logger.warn("Could not delete [{}].", DaoConstants.CUSTOM_COMMAND_JSON);
            }
            return true;
        }
        logger.info("No custom command detected.");
        return false;
    }
}
