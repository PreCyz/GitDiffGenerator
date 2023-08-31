package pg.gipter.core.dao.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CustomCommandDao {

    public static Optional<CustomCommand> readCustomCommand() {
        Optional<CustomCommand> result = Optional.empty();
        final Gson gson = new GsonBuilder().create();
        try (InputStream fis = new FileInputStream(DaoConstants.CUSTOM_COMMAND_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            result = Optional.ofNullable(gson.fromJson(reader, CustomCommand.class));
        } catch (IOException | NullPointerException e) {
            LoggerFactory.getLogger(CustomCommandDao.class).warn("Warning when loading {}. Exception message is: {}",
                    DaoConstants.CUSTOM_COMMAND_JSON, e.getMessage());
        }
        return result;
    }
}
