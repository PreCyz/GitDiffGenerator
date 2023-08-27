package pg.gipter.core.dao.gif;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.LoggerFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.command.CustomCommandDao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class GifDao {

    public static Optional<List<CustomGif>> readCustomGifs() {
        Optional<List<CustomGif>> result = Optional.empty();
        final Gson gson = new GsonBuilder().create();
        try (InputStream fis = new FileInputStream(DaoConstants.CUSTOM_GIFS_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            Type listType = new TypeToken<List<CustomGif>>() {}.getType();
            result = Optional.ofNullable(gson.fromJson(reader, listType));
        } catch (IOException | NullPointerException e) {
            LoggerFactory.getLogger(CustomCommandDao.class).warn("Warning when loading {}. Exception message is: {}",
                    DaoConstants.CUSTOM_COMMAND_JSON, e.getMessage());
        }
        return result;
    }
}
