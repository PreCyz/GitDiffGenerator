package pg.gipter.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.utils.PropertiesHelper;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

import static pg.gipter.utils.PropertiesHelper.APPLICATION_PROPERTIES;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public abstract class ApplicationProperties {

    protected final Logger logger;
    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ISO_DATE;

    protected Properties properties;
    protected final ArgExtractor argExtractor;
    private Set<VersionControlSystem> vcs;
    private String[] args;

    public ApplicationProperties(String[] args) {
        this.args = args;
        argExtractor = new ArgExtractor(args);
        logger = LoggerFactory.getLogger(this.getClass());
        vcs = new HashSet<>();
        init(args, new PropertiesHelper());
    }

    public String[] getArgs() {
        return args;
    }

    public final void setVcs(Set<VersionControlSystem> vcs) {
        this.vcs = vcs;
    }

    public final Set<VersionControlSystem> vcsSet() {
        return vcs;
    }

    protected void init(String[] args, PropertiesHelper propertiesHelper) {
        Optional<Properties> propsFromFile = propertiesHelper.loadApplicationProperties();
        if (propsFromFile.isPresent()) {
            properties = propsFromFile.get();
            logger.info("Properties from [{}] file loaded.", APPLICATION_PROPERTIES);
        } else {
            logger.warn("Can not load [{}].", APPLICATION_PROPERTIES);
            logger.info("Command line argument loaded: {}.", Arrays.toString(args));
        }
        logger.info("Application properties loaded: {}.", log());
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
        return !toolkitUsername().isEmpty() && !ArgName.toolkitUsername.defaultValue().equals(toolkitUsername()) &&
                !toolkitPassword().isEmpty() && !ArgName.toolkitPassword.defaultValue().equals(toolkitPassword());
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
        String extension = "txt";
        if (uploadType() == UploadType.DOCUMENTS) {
            extension = "zip";
        }
        return fileName + "." + extension;
    }

    public PreferredArgSource preferredArgSource() {
        return argExtractor.preferredArgSource();
    }

    public String version() {
        String version = "";

        InputStream is = getClass().getClassLoader().getResourceAsStream("version.txt");
        if (is == null) {
            logger.warn("Can not read version.");
        } else {
            Scanner scan = new Scanner(is);
            if (scan.hasNextLine()) {
                version = scan.nextLine();
            }
        }
        return version;
    }

    public final boolean isSilentMode() {
        return argExtractor.isSilentMode();
    }

    protected final String log() {
        return  "version='" + version() + '\'' +
                ", authors='" + String.join(",", authors()) + '\'' +
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
                ", uploadType='" + uploadType() + '\'' +
                ", documentFilters='" + String.join(",", documentFilters()) + '\'' +
                ", preferredArgSource='" + preferredArgSource() + '\'' +
                ", skipRemote='" + isSkipRemote() + '\'' +
                ", useUI='" + isUseUI() + '\'' +
                ", silentMode='" + isSilentMode() + '\'' +
                ", toolkitCredentialsSet='" + isToolkitCredentialsSet() + '\'' +
                ", toolkitUsername='" + toolkitUsername() + '\'' +
                ", toolkitUrl='" + toolkitUrl() + '\'' +
                ", toolkitWSUrl='" + toolkitWSUrl() + '\'' +
                ", toolkitDomain='" + toolkitDomain() + '\'' +
                ", toolkitCopyListName='" + toolkitCopyListName() + '\'' +
                ", toolkitUserFolder='" + toolkitUserFolder() + '\'' +
                ", toolkitProjectListNames='" + String.join(",", toolkitProjectListNames()) + '\''
                ;
    }

    public abstract Set<String> authors();
    public abstract String gitAuthor();
    public abstract String mercurialAuthor();
    public abstract String svnAuthor();
    public abstract String itemPath();
    public abstract Set<String> projectPaths();
    public abstract String itemFileNamePrefix();
    public abstract String committerEmail();
    public abstract LocalDate startDate();
    public abstract LocalDate endDate();
    public abstract int periodInDays();
    public abstract UploadType uploadType();
    public abstract boolean isConfirmationWindow();
    public abstract String toolkitUsername();
    public abstract String toolkitPassword();
    public abstract String toolkitDomain();
    public abstract String toolkitUserFolder();
    public abstract String toolkitCopyListName();
    public abstract Set<String> toolkitProjectListNames();
    public abstract String toolkitUrl();
    public abstract boolean isSkipRemote();
    public abstract boolean isUseUI();
    public abstract boolean isActiveTray();
    public abstract Set<String> documentFilters();
    public abstract boolean isDeleteDownloadedFiles();
}
