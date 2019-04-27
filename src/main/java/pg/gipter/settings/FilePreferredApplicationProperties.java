package pg.gipter.settings;

import pg.gipter.producer.command.UploadType;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
class FilePreferredApplicationProperties extends ApplicationProperties {

    FilePreferredApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public Set<String> authors() {
        if (hasProperties()) {
            String authors = properties.getProperty(
                    ArgName.author.name(), String.join(",", argExtractor.authors())
            );
            return Stream.of(authors.split(",")).collect(toCollection(LinkedHashSet::new));
        }
        return argExtractor.authors();
    }

    @Override
    public String gitAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.gitAuthor.name(), argExtractor.gitAuthor());
        }
        return argExtractor.gitAuthor();
    }

    @Override
    public String mercurialAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.mercurialAuthor.name(), argExtractor.mercurialAuthor());
        }
        return argExtractor.mercurialAuthor();
    }

    @Override
    public String svnAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.svnAuthor.name(), argExtractor.svnAuthor());
        }
        return argExtractor.svnAuthor();
    }

    @Override
    public String itemPath() {
        String itemPath = argExtractor.itemPath();
        if (hasProperties()) {
            itemPath = properties.getProperty(ArgName.itemPath.name(), itemPath);
            if (uploadType() == UploadType.STATEMENT) {
                return itemPath;
            }
        }
        return uploadType() == UploadType.STATEMENT ? itemPath : itemPath + File.separator + fileName();
    }

    @Override
    public String itemFileNamePrefix() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.itemFileNamePrefix.name(), argExtractor.itemFileNamePrefix());
        }
        return argExtractor.itemFileNamePrefix();
    }

    @Override
    public Set<String> projectPaths() {
        if (hasProperties()) {
            String[] projectPaths = properties.getProperty(
                    ArgName.projectPath.name(), ArgName.projectPath.defaultValue()
            ).split(",");
            return new HashSet<>(Arrays.asList(projectPaths));
        }
        return argExtractor.projectPaths();
    }

    @Override
    public int periodInDays() {
        if (hasProperties()) {
            return Math.abs(Integer.parseInt(properties.getProperty(
                    ArgName.periodInDays.name(), String.valueOf(argExtractor.periodInDays())
            )));
        }
        return argExtractor.periodInDays();
    }

    @Override
    public String committerEmail() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.committerEmail.name(), argExtractor.committerEmail());
        }
        return argExtractor.committerEmail();
    }

    @Override
    public LocalDate startDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgName.startDate.name(), LocalDate.now().minusDays(periodInDays()).format(yyyy_MM_dd)
            ).split("-");
            return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return argExtractor.startDate();
    }

    @Override
    public LocalDate endDate() {
        if (hasProperties()) {
            String[] date = argExtractor.endDate().format(yyyy_MM_dd).split("-");
            String endDateStr = properties.getProperty(ArgName.endDate.name());
            if (StringUtils.notEmpty(endDateStr)) {
                date = endDateStr.split("-");
            }
            return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return argExtractor.endDate();
    }

    @Override
    public UploadType uploadType() {
        if (hasProperties()) {
            String codeProtected = properties.getProperty(
                    ArgName.uploadType.name(), ArgName.uploadType.defaultValue()
            );
            return UploadType.valueFor(codeProtected);
        }
        return argExtractor.uploadType();
    }

    @Override
    public boolean isConfirmationWindow() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgName.confirmationWindow.name(), ArgName.confirmationWindow.defaultValue()
            ));
        }
        return argExtractor.isConfirmationWindow();
    }

    @Override
    public String toolkitUsername() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.toolkitUsername.name(), argExtractor.toolkitUsername()).trim().toUpperCase();
        }
        return argExtractor.toolkitUsername();
    }

    @Override
    public String toolkitPassword() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.toolkitPassword.name(), argExtractor.toolkitPassword());
        }
        return argExtractor.toolkitPassword();
    }

    @Override
    public String toolkitDomain() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.toolkitDomain.name(), argExtractor.toolkitDomain());
        }
        return argExtractor.toolkitDomain();
    }

    @Override
    public String toolkitUrl() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.toolkitUrl.name(), argExtractor.toolkitUrl());
        }
        return argExtractor.toolkitUrl();
    }

    @Override
    public String toolkitCopyListName() {
        if (hasProperties()) {
            return properties.getProperty(ArgName.toolkitCopyListName.name(), argExtractor.toolkitListName());
        }
        return argExtractor.toolkitListName();
    }

    @Override
    public boolean isSkipRemote() {
        String propertyName = ArgName.skipRemote.name();
        if (containsProperty(propertyName)) {
            return StringUtils.getBoolean(properties.getProperty(
                    propertyName, ArgName.skipRemote.defaultValue()
            ));
        }
        return argExtractor.isSkipRemote();
    }

    @Override
    public String toolkitUserFolder() {
        if (hasProperties()) {
            return ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
        }
        return argExtractor.toolkitUserFolder();
    }

    @Override
    public boolean isUseUI() {
        String propertyName = ArgName.useUI.name();
        if (containsProperty(propertyName)) {
            return StringUtils.getBoolean(properties.getProperty(
                    propertyName, ArgName.useUI.defaultValue()
            ));
        }
        return argExtractor.isUseUI();
    }

    @Override
    public boolean isActiveTray() {
        return false;
    }

    @Override
    public Set<String> toolkitProjectListNames() {
        if (hasProperties()) {
            String[] toolkitProjectListNames = properties.getProperty(
                    ArgName.toolkitProjectListNames.name(), ArgName.toolkitProjectListNames.defaultValue()
            ).split(",");
            return new HashSet<>(Arrays.asList(toolkitProjectListNames));
        }
        return argExtractor.toolkitProjectListNames();
    }

    @Override
    public boolean isDeleteDownloadedFiles() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgName.deleteDownloadedFiles.name(), ArgName.deleteDownloadedFiles.defaultValue()
            ));
        }
        return argExtractor.isDeleteDownloadedFiles();
    }

    @Override
    public boolean isEnableOnStartup() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgName.enableOnStartup.name(), ArgName.enableOnStartup.defaultValue()
            ));
        }
        return argExtractor.isEnableOnStartup();
    }

    @Override
    public boolean isUseAsFileName() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgName.useAsFileName.name(), ArgName.useAsFileName.defaultValue()
            ));
        }
        return argExtractor.isUseAsFileName();
    }

    @Override
    public boolean isUploadAsHtml() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgName.uploadAsHtml.name(), ArgName.uploadAsHtml.defaultValue()
            ));
        }
        return argExtractor.isUploadAsHtml();
    }

}
