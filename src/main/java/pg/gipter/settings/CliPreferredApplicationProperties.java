package pg.gipter.settings;

import pg.gipter.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 17-Sep-2018. */
class CliPreferredApplicationProperties extends ApplicationProperties {

    CliPreferredApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public Set<String> authors() {
        Set<String> authors = argExtractor.authors();
        String argName = ArgName.author.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String author = properties.getProperty(
                    argName, String.join(",", authors)
            );
            authors = Stream.of(author.split(",")).collect(toCollection(LinkedHashSet::new));
        }
        return authors;
    }

    @Override
    public String gitAuthor() {
        String gitAuthor = argExtractor.gitAuthor();
        String argName = ArgName.gitAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            gitAuthor = properties.getProperty(argName, gitAuthor);
        }
        return gitAuthor;
    }

    @Override
    public String mercurialAuthor() {
        String mercurialAuthor = argExtractor.mercurialAuthor();
        String argName = ArgName.mercurialAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            mercurialAuthor = properties.getProperty(argName, mercurialAuthor);
        }
        return mercurialAuthor;
    }

    @Override
    public String svnAuthor() {
        String svnAuthor = argExtractor.svnAuthor();
        String argName = ArgName.svnAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            svnAuthor = properties.getProperty(argName, svnAuthor);
        }
        return svnAuthor;
    }

    @Override
    public String itemPath() {
        String itemPath = ArgName.itemPath.defaultValue();
        String argName = ArgName.itemPath.name();
        if (containsArg(argName)) {
            itemPath = argExtractor.itemPath();
        }
        if (!containsArg(argName) && containsProperty(argName)) {
            itemPath = properties.getProperty(argName, argExtractor.itemPath());
        }
        return uploadType() == UploadType.STATEMENT ? itemPath : itemPath + File.separator + fileName();
    }

    @Override
    public String itemFileNamePrefix() {
        String itemFileNamePrefix = argExtractor.itemFileNamePrefix();
        String argName = ArgName.itemFileNamePrefix.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            itemFileNamePrefix = properties.getProperty(argName, itemFileNamePrefix);
        }
        return itemFileNamePrefix;
    }

    @Override
    public Set<String> projectPaths() {
        Set<String> projectPaths = argExtractor.projectPaths();
        String argName = ArgName.projectPath.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] projectPathsArray = properties.getProperty(argName, String.join(",", projectPaths)).split(",");
            projectPaths = new LinkedHashSet<>(Arrays.asList(projectPathsArray));
        }
        return projectPaths;
    }

    @Override
    public int periodInDays() {
        int periodInDays = argExtractor.periodInDays();
        String argName = ArgName.periodInDays.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            periodInDays = Math.abs(Integer.parseInt(properties.getProperty(argName, String.valueOf(periodInDays))));
        }
        return periodInDays;
    }

    @Override
    public String committerEmail() {
        String committerEmail = argExtractor.committerEmail();
        String argName = ArgName.committerEmail.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            committerEmail = properties.getProperty(argName, committerEmail);
        }
        return committerEmail;
    }

    @Override
    public LocalDate startDate() {
        LocalDate startDate = argExtractor.startDate();
        String argName = ArgName.startDate.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] date = properties.getProperty(argName, startDate.format(yyyy_MM_dd)).split("-");
            startDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        } else if (!containsArg(ArgName.periodInDays.name()) && containsProperty(ArgName.periodInDays.name())) {
            startDate = LocalDate.now().minusDays(periodInDays());
        }
        return startDate;
    }

    @Override
    public LocalDate endDate() {
        LocalDate endDate = argExtractor.endDate();
        String argName = ArgName.endDate.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] date = endDate.format(yyyy_MM_dd).split("-");
            String endDateStr = properties.getProperty(argName);
            if (StringUtils.notEmpty(endDateStr)) {
                date = endDateStr.split("-");
            }
            endDate = LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return endDate;
    }

    @Override
    public UploadType uploadType() {
        UploadType uploadType = argExtractor.uploadType();
        String argName = ArgName.uploadType.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            uploadType = UploadType.valueFor(properties.getProperty(argName, uploadType.name()));
        }
        return uploadType;
    }

    @Override
    public boolean isConfirmationWindow() {
        boolean confirmation = argExtractor.isConfirmationWindow();
        String argName = ArgName.confirmationWindow.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            confirmation = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(confirmation)));
        }
        return confirmation;
    }

    @Override
    public String toolkitUsername() {
        String toolkitUsername = argExtractor.toolkitUsername();
        String argName = ArgName.toolkitUsername.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitUsername = properties.getProperty(argName, toolkitUsername).trim().toUpperCase();
        }
        return toolkitUsername;
    }

    @Override
    public String toolkitPassword() {
        String toolkitPassword = argExtractor.toolkitPassword();
        String argName = ArgName.toolkitPassword.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitPassword = properties.getProperty(argName, toolkitPassword);
        }
        return toolkitPassword;
    }

    @Override
    public String toolkitDomain() {
        String toolkitDomain = argExtractor.toolkitDomain();
        String argName = ArgName.toolkitDomain.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitDomain = properties.getProperty(argName, toolkitDomain);
        }
        return toolkitDomain;
    }

    @Override
    public String toolkitUrl() {
        String toolkitUrl = argExtractor.toolkitUrl();
        String argName = ArgName.toolkitUrl.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitUrl = properties.getProperty(argName, toolkitUrl);
        }
        return toolkitUrl;
    }

    @Override
    public String toolkitCopyListName() {
        String toolkitListName = argExtractor.toolkitListName();
        String argName = ArgName.toolkitCopyListName.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitListName = properties.getProperty(argName, toolkitListName);
        }
        return toolkitListName;
    }

    @Override
    public boolean isSkipRemote() {
        boolean skipRemote = argExtractor.isSkipRemote();
        String argName = ArgName.skipRemote.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            skipRemote = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(skipRemote)));
        }
        return skipRemote;
    }

    @Override
    public String toolkitUserFolder() {
        return ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
    }

    @Override
    public boolean isUseUI() {
        boolean useUI = argExtractor.isUseUI();
        String argName = ArgName.useUI.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            useUI = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(useUI)));
        }
        return useUI;
    }

    @Override
    public boolean isActiveTray() {
        return false;
    }

    @Override
    public Set<String> toolkitProjectListNames() {
        Set<String> toolkitProjectListNames = argExtractor.toolkitProjectListNames();
        String argName = ArgName.toolkitProjectListNames.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] array = properties.getProperty(argName, String.join(",", toolkitProjectListNames)).split(",");
            toolkitProjectListNames = new LinkedHashSet<>(Arrays.asList(array));
        }
        return toolkitProjectListNames;
    }

    @Override
    public boolean isDeleteDownloadedFiles() {
        boolean delete = argExtractor.isDeleteDownloadedFiles();
        String argName = ArgName.deleteDownloadedFiles.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            delete = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(delete)));
        }
        return delete;
    }

    @Override
    public boolean isEnableOnStartup() {
        return false;
    }

}
