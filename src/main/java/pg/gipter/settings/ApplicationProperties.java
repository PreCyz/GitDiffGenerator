package pg.gipter.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.command.VersionControlSystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

import static pg.gipter.settings.PropertiesLoader.APPLICATION_PROPERTIES;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public abstract class ApplicationProperties {

    private final Logger logger;
    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected Properties properties;
    protected final ArgExtractor argExtractor;
    private Set<VersionControlSystem> vcs;

    public ApplicationProperties(String[] args) {
        argExtractor = new ArgExtractor(args);
        logger = LoggerFactory.getLogger(this.getClass());
        vcs = new HashSet<>();
        init(args, new PropertiesLoader());
    }

    public final void setVcs(Set<VersionControlSystem> vcs) {
        this.vcs = vcs;
    }

    public final Set<VersionControlSystem> vcsSet() {
        return vcs;
    }

    protected final void init(String[] args, PropertiesLoader propertiesLoader) {
        Optional<Properties> propsFromFile = propertiesLoader.loadPropertiesFromFile();
        if (propsFromFile.isPresent()) {
            properties = propsFromFile.get();
            logger.info("Properties from file loaded [{}].", log());
        } else {
            logger.warn("Can not read [{}].", APPLICATION_PROPERTIES);
            logger.info("Command line argument loaded: {}.", Arrays.toString(args));
        }
    }

    protected final boolean hasProperties() {
        return properties != null;
    }

    protected final boolean containsProperty(String key) {
        return hasProperties() && properties.containsKey(key);
    }

    protected final boolean containsArg(String argName) {
        return argExtractor.containsArg(argName);
    }

    public final String toolkitWSUrl() {
        return argExtractor.toolkitWSUrl();
    }

    public final boolean isToolkitCredentialsSet() {
        return !toolkitUsername().isEmpty() && !ArgExtractor.ArgName.toolkitUsername.defaultValue().equals(toolkitUsername()) &&
                !toolkitPassword().isEmpty() && !ArgExtractor.ArgName.toolkitPassword.defaultValue().equals(toolkitPassword());
    }

    public final String fileName() {
        DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        LocalDate endDate = endDate();
        String fileName;
        if (now.isEqual(endDate)) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
            fileName = String.format("%d-%s-week-%d", now.getYear(), now.getMonth(), weekNumber).toLowerCase();
        } else {
            fileName = String.format("%d-%s-%s-%s",
                    endDate.getYear(),
                    endDate.getMonth(),
                    startDate().format(yyyyMMdd),
                    endDate.format(yyyyMMdd)
            ).toLowerCase();
        }
        if (!itemFileNamePrefix().isEmpty()) {
            fileName = String.format("%s-%s", itemFileNamePrefix(), fileName);
        }
        return fileName + ".txt";
    }

    protected final String log() {
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

    public abstract Set<String> authors();
    public abstract String gitAuthor();
    public abstract String mercurialAuthor();
    public abstract String svnAuthor();
    public abstract String itemPath();
    public abstract Set<String> projectPaths();
    public abstract String committerEmail();
    public abstract LocalDate startDate();
    public abstract LocalDate endDate();
    public abstract CodeProtection codeProtection();
    public abstract boolean isConfirmationWindow();
    public abstract String toolkitUsername();
    public abstract String toolkitPassword();
    public abstract String toolkitDomain();
    public abstract String toolkitUserFolder();
    public abstract String toolkitListName();
    protected abstract String toolkitUrl();
    abstract int periodInDays();
    abstract String itemFileNamePrefix();
}
