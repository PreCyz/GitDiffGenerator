package pg.gipter.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.producer.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class ApplicationProperties {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
    private static final String APPLICATION_PROPERTIES = "application.properties";

    private Properties properties;
    private final ArgExtractor argExtractor;

    public ApplicationProperties(String[] args) {
        argExtractor = new ArgExtractor(args);
        init(args);
    }

    private void init(String[] args) {
        try (InputStream is = new FileInputStream(APPLICATION_PROPERTIES)) {
            properties = new Properties();
            properties.load(is);
            logger.info("Properties loaded [{}]", log());
        } catch (IOException | NullPointerException e) {
            logger.warn("Can not read [{}].", APPLICATION_PROPERTIES);
            properties = null;
            logger.info("Program argument loaded: {}", Arrays.toString(args));
        }
    }

    private boolean hasProperties() {
        return properties != null;
    }

    public String author() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.author.name(), argExtractor.author());
        }
        return argExtractor.author();
    }

    public String itemPath() {
        String itemPath = argExtractor.itemPath();
        if (hasProperties()) {
            itemPath = properties.getProperty(ArgExtractor.ArgName.itemPath.name(), itemPath);
        }
        return itemPath + File.separator + fileName();
    }

    public String fileName() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        String fileName = String.format("%d-%s-week-%d.txt", now.getYear(), now.getMonth().name(), weekNumber).toLowerCase();
        if (!itemFileName().isEmpty()) {
            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            fileName = String.format("%s-%s-%s.txt", itemFileName(), startDate().format(yyyyMMdd), endDate().format(yyyyMMdd));
        }
        return fileName;
    }

    private String itemFileName() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.itemFileName.name(), argExtractor.itemFileName());
        }
        return argExtractor.itemFileName();
    }

    public Set<String> projectPaths() {
        if (hasProperties()) {
            String[] projectPaths = properties.getProperty(
                    ArgExtractor.ArgName.projectPath.name(), ArgExtractor.ArgName.projectPath.defaultValue()
            ).split(",");
            return new HashSet<>(Arrays.asList(projectPaths));
        }
        return argExtractor.projectPaths();
    }

    private int days() {
        if (hasProperties()) {
            return Integer.parseInt(properties.getProperty(
                    ArgExtractor.ArgName.minusDays.name(), ArgExtractor.ArgName.minusDays.defaultValue()
            ));
        }
        return argExtractor.days();
    }

    public String committerEmail() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.committerEmail.name(), argExtractor.committerEmail());
        }
        return argExtractor.committerEmail();
    }

    public LocalDate startDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.startDate.name(), ArgExtractor.ArgName.startDate.defaultValue()
            ).split("-");
            return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return argExtractor.startDate();
    }

    public LocalDate endDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.endDate.name(), ArgExtractor.ArgName.endDate.defaultValue()
            ).split("-");
            return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return argExtractor.endDate();
    }

    public VersionControlSystem versionControlSystem() {
        if (hasProperties()) {
            String vcs = properties.getProperty(
                    ArgExtractor.ArgName.versionControlSystem.name(),
                    ArgExtractor.ArgName.versionControlSystem.defaultValue()
            );
            return VersionControlSystem.valueFor(vcs.toUpperCase());
        }
        return argExtractor.versionControlSystem();
    }

    public boolean codeProtected() {
        if (hasProperties()) {
            String codeProtected = properties.getProperty(
                    ArgExtractor.ArgName.codeProtected.name(), ArgExtractor.ArgName.codeProtected.defaultValue()
            );
            return StringUtils.getBoolean(codeProtected);
        }
        return argExtractor.codeProtected();
    }

    public String toolkitUsername() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitUsername.name(), argExtractor.toolkitUsername());
        }
        return argExtractor.toolkitUsername();
    }

    public String toolkitUserEmail() {
        if (hasProperties()) {
            String userName = properties.getProperty(ArgExtractor.ArgName.toolkitUsername.name());
            if (userName == null || userName.isEmpty()) {
                return userName + argExtractor.emailDomain();
            }
        }
        return argExtractor.toolkitUserEmail();
    }

    public String toolkitPassword() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitPassword.name(), argExtractor.toolkitPassword());
        }
        return argExtractor.toolkitPassword();
    }

    public String toolkitDomain() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitDomain.name(), argExtractor.toolkitDomain());
        }
        return argExtractor.toolkitDomain();
    }

    public String toolkitUrl() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitUrl.name(), argExtractor.toolkitUrl());
        }
        return argExtractor.toolkitUrl();
    }

    public String toolkitListName() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitListName.name(), argExtractor.toolkitListName());
        }
        return argExtractor.toolkitListName();
    }

    public String toolkitWSUrl() {
        return argExtractor.toolkitWSUrl();
    }

    public String toolkitUserFolder() {
        return argExtractor.toolkitUserFolder();
    }

    public boolean isToolkitPropertiesSet() {
        return !toolkitUsername().equals(ArgExtractor.ArgName.toolkitUsername.defaultValue()) &&
                !toolkitPassword().equals(ArgExtractor.ArgName.toolkitPassword.defaultValue());
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
                ", codeProtected='" + codeProtected() + '\'' +
                ", toolkitPropertiesSet='" + isToolkitPropertiesSet() + '\'' +
                ", toolkitUrl='" + toolkitUrl() + '\'' +
                ", toolkitListName='" + toolkitListName() + '\'';
    }
}
