package pg.gipter.settings;

import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.producer.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class ApplicationProperties {

    private final static String APPLICATION_PROPERTIES = "application.properties";

    private Properties properties;
    private final String[] args;

    public ApplicationProperties(String[] args) {
        this.args = args;
        init();
    }

    private void init() {
        try (InputStream is = new FileInputStream(APPLICATION_PROPERTIES)) {
            properties = new Properties();
            properties.load(is);
            System.out.printf("Properties loaded [%s]%n", log());
        } catch (IOException | NullPointerException e) {
            System.out.printf("Can not read [%s].%n", APPLICATION_PROPERTIES);
            properties = null;
            System.out.printf("Program argument loaded: %s%n", Arrays.toString(args));
        }
    }

    private boolean hasProperties() {
        return properties != null;
    }

    public String author() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.author.name(), ArgExtractor.ArgName.author.defaultValue());
        }
        return ArgExtractor.author(args);
    }

    public String itemPath() {
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
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();
        if (!itemFileName().isEmpty()) {
            fileName = String.format("%s-%s-%s.txt",
                    itemFileName(),
                    startDate().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")),
                    endDate().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
            );
        }
        return fileName;
    }

    private String itemFileName() {
        if (hasProperties()) {
            return properties.getProperty(
                    ArgExtractor.ArgName.itemFileName.name(), ArgExtractor.ArgName.itemFileName.defaultValue()
            );
        }
        return ArgExtractor.itemFileName(args);
    }

    public String[] projectPaths() {
        if (hasProperties()) {
            return properties.getProperty(
                    ArgExtractor.ArgName.projectPath.name(), ArgExtractor.ArgName.projectPath.defaultValue()
            ).split(",");
        }
        return ArgExtractor.projectPaths(args).split(",");
    }

    private int days() {
        if (hasProperties()) {
            return Integer.parseInt(properties.getProperty(
                    ArgExtractor.ArgName.minusDays.name(), ArgExtractor.ArgName.minusDays.defaultValue()
            ));
        }
        return ArgExtractor.days(args);
    }

    public String committerEmail() {
        if (hasProperties()) {
            return properties.getProperty(
                    ArgExtractor.ArgName.committerEmail.name(), ArgExtractor.ArgName.committerEmail.defaultValue()
            );
        }
        return ArgExtractor.committerEmail(args);
    }

    public LocalDate startDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.startDate.name(), ArgExtractor.ArgName.startDate.defaultValue()
            ).split("-");
            return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return ArgExtractor.startDate(args);
    }

    public LocalDate endDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.endDate.name(), ArgExtractor.ArgName.endDate.defaultValue()
            ).split("-");
            return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return ArgExtractor.endDate(args);
    }

    public VersionControlSystem versionControlSystem() {
        if (hasProperties()) {
            String vcs = properties.getProperty(
                    ArgExtractor.ArgName.versionControlSystem.name(),
                    ArgExtractor.ArgName.versionControlSystem.defaultValue()
            );
            return VersionControlSystem.valueFor(vcs.toUpperCase());
        }
        return ArgExtractor.versionControlSystem(args);
    }

    public boolean isCodeProtected() {
        if (hasProperties()) {
            String codeProtected = properties.getProperty(
                    ArgExtractor.ArgName.codeProtected.name(), ArgExtractor.ArgName.codeProtected.defaultValue()
            );
            return StringUtils.getBoolean(codeProtected);
        }
        return ArgExtractor.codeProtected(args);
    }

    private String log() {
        return  "author='" + author() + '\'' +
                ", committerEmail='" + committerEmail() + '\'' +
                ", itemPath='" + itemPath() + '\'' +
                ", projectPath='" + String.join(",", projectPaths()) + '\'' +
                ", days='" + days() + '\'' +
                ", startDate='" + startDate() + '\'' +
                ", endDate='" + endDate() + '\'' +
                ", versionControlSystem='" + versionControlSystem() + '\'' +
                ", codeProtected='" + isCodeProtected() + '\'';
    }
}
