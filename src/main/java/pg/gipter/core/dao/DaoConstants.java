package pg.gipter.core.dao;

import java.time.format.DateTimeFormatter;

public final class DaoConstants {

    private DaoConstants() { }

    public static final String APPLICATION_PROPERTIES_JSON = "applicationProperties.json";
    public static final String DATA_JSON = "data.json";
    public static final String CUSTOM_COMMAND_JSON = "command.json";
    public static final String CUSTOM_GIFS_JSON = "gifs.json";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
