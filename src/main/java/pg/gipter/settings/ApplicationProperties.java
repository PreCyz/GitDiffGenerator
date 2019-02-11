package pg.gipter.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.producer.util.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static pg.gipter.settings.PropertiesLoader.APPLICATION_PROPERTIES;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public class ApplicationProperties {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private Properties properties;
    private final ArgExtractor argExtractor;
    private Set<VersionControlSystem> vcs;

    public ApplicationProperties(String[] args) {
        argExtractor = new ArgExtractor(args);
        init(args, new PropertiesLoader());
        vcs = new HashSet<>();
    }

    public void setVcs(Set<VersionControlSystem> vcs) {
        this.vcs = vcs;
    }

    public Set<VersionControlSystem> vcsSet() {
        return vcs;
    }

    void init(String[] args, PropertiesLoader propertiesLoader) {
        Optional<Properties> propsFromFile = propertiesLoader.loadPropertiesFromFile();
        if (propsFromFile.isPresent()) {
            properties = propsFromFile.get();
            logger.info("Properties from file loaded [{}].", log());
        } else {
            logger.warn("Can not read [{}].", APPLICATION_PROPERTIES);
            logger.info("Command line argument loaded: {}.", Arrays.toString(args));
        }
    }

    boolean hasProperties() {
        return properties != null;
    }

    public Set<String> authors() {
        if (hasProperties()) {
            String authors = properties.getProperty(
                    ArgExtractor.ArgName.author.name(), String.join(",", argExtractor.authors())
            );
            return Stream.of(authors.split(",")).collect(toCollection(LinkedHashSet::new));
        }
        return argExtractor.authors();
    }

    public String gitAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.gitAuthor.name(), argExtractor.gitAuthor());
        }
        return argExtractor.gitAuthor();
    }

    public String mercurialAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.mercurialAuthor.name(), argExtractor.mercurialAuthor());
        }
        return argExtractor.mercurialAuthor();
    }

    public String svnAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.svnAuthor.name(), argExtractor.svnAuthor());
        }
        return argExtractor.svnAuthor();
    }

    public String itemPath() {
        String itemPath = argExtractor.itemPath();
        if (hasProperties()) {
            itemPath = properties.getProperty(ArgExtractor.ArgName.itemPath.name(), itemPath);
            if (codeProtection() == CodeProtection.STATEMENT) {
                return itemPath;
            }
        }
        return codeProtection() == CodeProtection.STATEMENT ? itemPath : itemPath + File.separator + fileName();
    }

    public String fileName() {
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        LocalDate endDate = endDate();
        String fileName;
        if (now.isEqual(endDate)) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
            fileName = String.format("%d-%s-week-%d", now.getYear(), now.getMonth(), weekNumber).toLowerCase();
        } else {
            fileName = String.format("%d-%s-%s-%s", endDate.getYear(), endDate.getMonth(), startDate().format(yyyyMMdd), endDate.format(yyyyMMdd)).toLowerCase();
        }
        if (!itemFileNamePrefix().isEmpty()) {
            fileName = String.format("%s-%s", itemFileNamePrefix(), fileName);
        }
        return fileName + ".txt";
    }

    String itemFileNamePrefix() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.itemFileNamePrefix.name(), argExtractor.itemFileNamePrefix());
        }
        return argExtractor.itemFileNamePrefix();
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

    int periodInDays() {
        if (hasProperties()) {
            return Math.abs(Integer.parseInt(properties.getProperty(
                    ArgExtractor.ArgName.periodInDays.name(), String.valueOf(argExtractor.periodInDays())
            )));
        }
        return argExtractor.periodInDays();
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
                    ArgExtractor.ArgName.startDate.name(), LocalDate.now().minusDays(periodInDays()).format(yyyy_MM_dd)
            ).split("-");
            return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return argExtractor.startDate();
    }

    public LocalDate endDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.endDate.name(), argExtractor.endDate().format(yyyy_MM_dd)
            ).split("-");
            return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return argExtractor.endDate();
    }

    public CodeProtection codeProtection() {
        if (hasProperties()) {
            String codeProtected = properties.getProperty(
                    ArgExtractor.ArgName.codeProtection.name(), ArgExtractor.ArgName.codeProtection.defaultValue()
            );
            return CodeProtection.valueFor(codeProtected);
        }
        return argExtractor.codeProtection();
    }

    public boolean isConfirmation() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgExtractor.ArgName.confirmationWindow.name(), ArgExtractor.ArgName.confirmationWindow.defaultValue()
            ));
        }
        return argExtractor.isConfirmation();
    }

    public String toolkitUsername() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitUsername.name(), argExtractor.toolkitUsername()).trim().toUpperCase();
        }
        return argExtractor.toolkitUsername();
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

    private String toolkitUrl() {
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
        if (hasProperties()) {
            return ArgExtractor.ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
        }
        return argExtractor.toolkitUserFolder();
    }

    public boolean isToolkitCredentialsSet() {
        return !toolkitUsername().isEmpty() && !ArgExtractor.ArgName.toolkitUsername.defaultValue().equals(toolkitUsername()) &&
                !toolkitPassword().isEmpty() && !ArgExtractor.ArgName.toolkitPassword.defaultValue().equals(toolkitPassword());
    }

    private String log() {
        return  "authors='" + authors() + '\'' +
                ", gitAuthor='" + gitAuthor() + '\'' +
                ", mercurialAuthor='" + mercurialAuthor() + '\'' +
                ", svnAuthor='" + svnAuthor() + '\'' +
                ", committerEmail='" + committerEmail() + '\'' +
                ", itemPath='" + itemPath() + '\'' +
                ", fileName='" + fileName() + '\'' +
                ", projectPath='" + String.join(",", projectPaths()) + '\'' +
                ", periodInDays='" + periodInDays() + '\'' +
                ", startDate='" + startDate() + '\'' +
                ", endDate='" + endDate() + '\'' +
                ", codeProtection='" + codeProtection() + '\'' +
                ", toolkitCredentialsSet='" + isToolkitCredentialsSet() + '\'' +
                ", toolkitUsername='" + toolkitUsername() + '\'' +
                ", toolkitUrl='" + toolkitUrl() + '\'' +
                ", toolkitWSUrl='" + toolkitWSUrl() + '\'' +
                ", toolkitDomain='" + toolkitDomain() + '\'' +
                ", toolkitListName='" + toolkitListName() + '\'' +
                ", toolkitUserFolder='" + toolkitUserFolder()+ '\''
                ;
    }
}
