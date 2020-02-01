package pg.gipter.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.configuration.ConfigurationDao;
import pg.gipter.dao.DaoFactory;
import pg.gipter.producer.command.UploadType;
import pg.gipter.producer.command.VersionControlSystem;
import pg.gipter.settings.dto.ApplicationConfig;
import pg.gipter.settings.dto.NamePatternValue;
import pg.gipter.settings.dto.RunConfig;
import pg.gipter.settings.dto.ToolkitConfig;
import pg.gipter.utils.StringUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
public abstract class ApplicationProperties {

    protected final Logger logger;
    public static final DateTimeFormatter yyyy_MM_dd = DateTimeFormatter.ISO_DATE;

    protected ApplicationConfig applicationConfig;
    protected ToolkitConfig toolkitConfig;
    protected Map<String, RunConfig> runConfigMap;
    protected RunConfig currentRunConfig;
    protected final ArgExtractor argExtractor;
    private Set<VersionControlSystem> vcs;
    private String[] args;
    private ConfigurationDao configurationDao;

    public ApplicationProperties(String[] args) {
        this.args = args;
        argExtractor = new ArgExtractor(args);
        logger = LoggerFactory.getLogger(this.getClass());
        vcs = new HashSet<>();
        configurationDao = DaoFactory.getConfigurationDao();
        init(configurationDao);
    }

    public String[] getArgs() {
        return args;
    }

    public void loadRunConfig(String configurationName) {
        runConfigMap = configurationDao.loadRunConfigMap();
        if (!runConfigMap.isEmpty() && runConfigMap.containsKey(configurationName)) {
            currentRunConfig = runConfigMap.get(configurationName);
        } else {
            currentRunConfig = new RunConfig();
        }
    }

    public final void setVcs(Set<VersionControlSystem> vcs) {
        this.vcs = vcs;
    }

    public final Set<VersionControlSystem> vcsSet() {
        return vcs;
    }

    protected void init(ConfigurationDao configurationDao) {
        applicationConfig = configurationDao.loadApplicationConfig();
        toolkitConfig = configurationDao.loadToolkitConfig();
        runConfigMap = configurationDao.loadRunConfigMap();
        if (runConfigMap.isEmpty()) {
            currentRunConfig = new RunConfig();
        } else if (runConfigMap.containsKey(argExtractor.configurationName())) {
            currentRunConfig = runConfigMap.get(argExtractor.configurationName());
        } else {
            currentRunConfig = new ArrayList<>(runConfigMap.entrySet()).get(0).getValue();
        }
        if (currentRunConfig != null) {
            logger.info("Configuration [{}] loaded.", argExtractor.configurationName());
        } else {
            logger.warn("Can not load configuration [{}].", argExtractor.configurationName());
            logger.info("Command line argument loaded: {}.",
                    Stream.of(argExtractor.getArgs()).filter(arg -> !arg.startsWith(ArgName.toolkitPassword.name()))
                            .collect(Collectors.joining(" "))
            );
        }
        logger.info("Application properties loaded: {}.", log());
    }

    protected boolean isOtherAuthorsExists() {
        boolean isNoOtherAuthors = StringUtils.nullOrEmpty(committerEmail());
        isNoOtherAuthors &= StringUtils.nullOrEmpty(gitAuthor());
        isNoOtherAuthors &= StringUtils.nullOrEmpty(svnAuthor());
        isNoOtherAuthors &= StringUtils.nullOrEmpty(mercurialAuthor());
        return !isNoOtherAuthors;
    }

    protected final boolean containsArg(String argName) {
        return argExtractor.containsArg(argName);
    }

    public final String toolkitWSUrl() {
        return argExtractor.toolkitWSUrl();
    }

    public final String toolkitCopyCase() {
        return argExtractor.toolkitCopyCase();
    }

    public final boolean isToolkitCredentialsSet() {
        return !toolkitUsername().isEmpty() && !ArgName.toolkitUsername.defaultValue().equals(toolkitUsername()) &&
                !toolkitPassword().isEmpty() && !ArgName.toolkitPassword.defaultValue().equals(toolkitPassword());
    }

    public final String fileName() {
        String fileName = itemFileNamePrefix();
        if (fileName.isEmpty()) {
            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate now = LocalDate.now();
            LocalDate endDate = endDate();
            if (now.isEqual(endDate)) {
                int weekNumber = getWeekNumber(now);
                fileName = String.format("%d-%s-week-%d", now.getYear(), now.getMonth(), weekNumber).toLowerCase();
            } else {
                fileName = String.format("%d-%s-%s-%s",
                        endDate.getYear(),
                        endDate.getMonth(),
                        startDate().format(yyyyMMdd),
                        endDate.format(yyyyMMdd)
                ).toLowerCase();
            }
        } else {
            for (NamePatternValue patternValue : NamePatternValue.values()) {
                if (fileName.contains(patternValue.name())) {
                    String pattern = "\\{" + patternValue.name() + "\\}";
                    fileName = fileName.replaceAll(pattern, valueFromPattern(patternValue));
                }
            }
        }
        String extension = getFileExtension();
        return fileName + "." + extension;
    }

    public int getWeekNumber(LocalDate localDate) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return localDate.get(weekFields.weekOfWeekBasedYear());
    }

    String valueFromPattern(NamePatternValue patternValue) {
        if (patternValue == null) {
            return "";
        }
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        switch (patternValue) {
            case CURRENT_DATE:
                return LocalDate.now().format(yyyy_MM_dd);
            case CURRENT_YEAR:
                return String.valueOf(LocalDate.now().getYear());
            case CURRENT_MONTH_NAME:
                return LocalDate.now().getMonth().name();
            case CURRENT_MONTH_NUMBER:
                return String.valueOf(LocalDate.now().getMonthValue());
            case CURRENT_WEEK_NUMBER:
                return String.valueOf(LocalDate.now().get(weekFields.weekOfWeekBasedYear()));
            case START_DATE:
                return startDate().format(yyyy_MM_dd);
            case START_DATE_YEAR:
                return String.valueOf(startDate().getYear());
            case START_DATE_MONTH_NAME:
                return startDate().getMonth().name();
            case START_DATE_MONTH_NUMBER:
                return String.valueOf(startDate().getMonthValue());
            case START_DATE_WEEK_NUMBER:
                return String.valueOf(startDate().get(weekFields.weekOfWeekBasedYear()));
            case END_DATE:
                return endDate().format(yyyy_MM_dd);
            case END_DATE_YEAR:
                return String.valueOf(endDate().getYear());
            case END_DATE_MONTH_NAME:
                return endDate().getMonth().name();
            case END_DATE_MONTH_NUMBER:
                return String.valueOf(endDate().getMonthValue());
            case END_DATE_WEEK_NUMBER:
                return String.valueOf(endDate().get(weekFields.weekOfWeekBasedYear()));
            default:
                return "";
        }
    }

    String getFileExtension() {
        String extension = "txt";
        if (uploadType() == UploadType.TOOLKIT_DOCS) {
            extension = "zip";
        }
        return extension;
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
        String log = "version='" + version() + '\'';
        if (currentRunConfig != null) {
            log += ", configurationName='" + configurationName() + '\'' +
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
                    ", skipRemote='" + isSkipRemote() + '\'' +
                    ", fetchAll='" + isFetchAll() + '\'' +
                    ", deleteDownloadedFiles='" + isDeleteDownloadedFiles() + '\'';
        }
        if (applicationConfig != null) {
            log += ", preferredArgSource='" + preferredArgSource() + '\'' +
                    ", useUI='" + isUseUI() + '\'' +
                    ", silentMode='" + isSilentMode() + '\'' +
                    ", enableOnStartup='" + isEnableOnStartup() + '\'' +
                    ", upgradeFinished='" + isUpgradeFinished() + '\'' +
                    ", loggerLevel='" + loggerLevel() + '\'';
        }
        if (toolkitConfig != null) {
            log += ", toolkitCredentialsSet='" + isToolkitCredentialsSet() + '\'' +
                    ", toolkitUsername='" + toolkitUsername() + '\'' +
                    ", toolkitUrl='" + toolkitUrl() + '\'' +
                    ", toolkitWSUrl='" + toolkitWSUrl() + '\'' +
                    ", toolkitDomain='" + toolkitDomain() + '\'' +
                    ", toolkitCopyListName='" + toolkitCopyListName() + '\'' +
                    ", toolkitUserFolder='" + toolkitUserFolder() + '\'' +
                    ", toolkitProjectListNames='" + String.join(",", toolkitProjectListNames());

        }
        return  log;
    }

    public abstract String configurationName();
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
    public abstract boolean isDeleteDownloadedFiles();
    public abstract boolean isSkipRemote();

    public abstract boolean isFetchAll();
    public abstract String toolkitUsername();
    public abstract String toolkitPassword();
    public abstract String toolkitDomain();
    public abstract String toolkitUserFolder();
    public abstract String toolkitCopyListName();
    public abstract Set<String> toolkitProjectListNames();

    public abstract String toolkitUrl();
    public abstract boolean isConfirmationWindow();
    public abstract boolean isActiveTray();
    public abstract boolean isEnableOnStartup();
    public abstract boolean isUseUI();
    public abstract String loggerLevel();

    public abstract boolean isUpgradeFinished();
}
