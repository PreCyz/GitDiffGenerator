package pg.gipter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
class ApplicationProperties {

    private Properties properties;
    private final String[] args;

    ApplicationProperties(String[] args) {
        this.args = args;
    }

    ApplicationProperties init() {
        try (InputStream is = new FileInputStream("application.properties")) {
            properties = new Properties();
            properties.load(is);
            System.out.printf("Properties loaded [%s]%n", log());
        } catch (IOException | NullPointerException e) {
            System.out.println("Problem with reading application.properties");
            properties = null;
            System.out.printf("Program argument loaded %s%n", Arrays.toString(args));
        }
        return this;
    }

    private boolean hasProperties() {
        return properties != null;
    }

    String author() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.author.name(), ArgExtractor.ArgName.author.defaultValue());
        }
        return ArgExtractor.author(args);
    }

    String itemPath() {
        String itemPath = ArgExtractor.path(args);
        if (hasProperties()) {
            itemPath = properties.getProperty(ArgExtractor.ArgName.itemPath.name(), ArgExtractor.ArgName.itemPath.defaultValue());
        }
        return itemPath + File.separator + fileName();
    }

    private String fileName() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        return String.format("%s-week-%d.txt", now.getMonth().name(), weekNumber).toLowerCase();
    }

    String[] projectPaths() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.projectPath.name(), ArgExtractor.ArgName.projectPath.defaultValue()).split(",");
        }
        return ArgExtractor.projectPaths(args).split(",");
        }

    String gitBashPath() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.gitBashPath.name(), ArgExtractor.ArgName.gitBashPath.defaultValue());
        }
        return ArgExtractor.gitBashPath(args);
    }

    int days() {
        if (hasProperties()) {
            return Integer.parseInt(properties.getProperty(ArgExtractor.ArgName.minusDays.name(), ArgExtractor.ArgName.minusDays.defaultValue()));
        }
        return ArgExtractor.days(args);
    }

    String committerEmail() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.committerEmail.name(), ArgExtractor.ArgName.committerEmail.defaultValue());
        }
        return ArgExtractor.gitCommitterEmail(args);
    }

    private String log() {
        return  "author='" + author() + '\'' +
                ", committerEmail='" + committerEmail() + '\'' +
                ", itemPath='" + itemPath() + '\'' +
                ", projectPath='" + String.join(",", projectPaths()) + '\'' +
                ", gitBashPath='" + gitBashPath() + '\'' +
                ", days='" + days() + '\'';
    }
}
