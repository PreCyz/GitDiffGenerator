package pg.gipter.settings;

import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.util.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

/**Created by Pawel Gawedzki on 17-Sep-2018.*/
class CliPreferredApplicationProperties extends ApplicationProperties {

    CliPreferredApplicationProperties(String[] args) {
        super(args);
    }

    @Override
    public Set<String> authors() {
        Set<String> authors = argExtractor.authors();
        String argName = ArgExtractor.ArgName.author.name();
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
        String argName = ArgExtractor.ArgName.gitAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            gitAuthor = properties.getProperty(argName, gitAuthor);
        }
        return gitAuthor;
    }

    @Override
    public String mercurialAuthor() {
        String mercurialAuthor = argExtractor.mercurialAuthor();
        String argName = ArgExtractor.ArgName.mercurialAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            mercurialAuthor = properties.getProperty(argName, mercurialAuthor);
        }
        return mercurialAuthor;
    }

    @Override
    public String svnAuthor() {
        String svnAuthor = argExtractor.svnAuthor();
        String argName = ArgExtractor.ArgName.svnAuthor.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            svnAuthor = properties.getProperty(argName, svnAuthor);
        }
        return svnAuthor;
    }

    @Override
    public String itemPath() {
        String itemPath = ArgExtractor.ArgName.itemPath.defaultValue();
        String argName = ArgExtractor.ArgName.itemPath.name();
        if (containsArg(argName)) {
            itemPath = argExtractor.itemPath();
        }
        if (!containsArg(argName) && containsProperty(argName)) {
            itemPath = properties.getProperty(argName, argExtractor.itemPath());
        }
        return codeProtection() == CodeProtection.STATEMENT ? itemPath : itemPath + File.separator + fileName();
    }

    @Override
    String itemFileNamePrefix() {
        String itemFileNamePrefix = argExtractor.itemFileNamePrefix();
        String argName = ArgExtractor.ArgName.itemFileNamePrefix.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            itemFileNamePrefix = properties.getProperty(argName, itemFileNamePrefix);
        }
        return itemFileNamePrefix;
    }

    @Override
    public Set<String> projectPaths() {
        Set<String> projectPaths = argExtractor.projectPaths();
        String argName = ArgExtractor.ArgName.projectPath.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] projectPathsArray = properties.getProperty(argName, String.join(",", projectPaths)).split(",");
            projectPaths = new LinkedHashSet<>(Arrays.asList(projectPathsArray));
        }
        return projectPaths;
    }

    @Override
    int periodInDays() {
        int periodInDays = argExtractor.periodInDays();
        String argName = ArgExtractor.ArgName.periodInDays.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            periodInDays = Math.abs(Integer.parseInt(properties.getProperty(argName, String.valueOf(periodInDays))));
        }
        return periodInDays;
    }

    @Override
    public String committerEmail() {
        String committerEmail = argExtractor.committerEmail();
        String argName = ArgExtractor.ArgName.committerEmail.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            committerEmail = properties.getProperty(argName, committerEmail);
        }
        return committerEmail;
    }

    @Override
    public LocalDate startDate() {
        LocalDate startDate = argExtractor.startDate();
        String argName = ArgExtractor.ArgName.startDate.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            String[] date = properties.getProperty(argName, startDate.format(yyyy_MM_dd)).split("-");
            startDate = LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return startDate;
    }

    @Override
    public LocalDate endDate() {
        LocalDate endDate = argExtractor.endDate();
        String argName = ArgExtractor.ArgName.endDate.name();
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
    public CodeProtection codeProtection() {
        CodeProtection codeProtection = argExtractor.codeProtection();
        String argName = ArgExtractor.ArgName.codeProtection.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            codeProtection = CodeProtection.valueFor(properties.getProperty(argName, codeProtection.name()));
        }
        return codeProtection;
    }

    @Override
    public boolean isConfirmationWindow() {
        boolean confirmation = argExtractor.isConfirmationWindow();
        String argName = ArgExtractor.ArgName.confirmationWindow.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            confirmation = StringUtils.getBoolean(properties.getProperty(argName, String.valueOf(confirmation)));
        }
        return confirmation;
    }

    @Override
    public String toolkitUsername() {
        String toolkitUsername = argExtractor.toolkitUsername();
        String argName = ArgExtractor.ArgName.toolkitUsername.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitUsername = properties.getProperty(argName, toolkitUsername).trim().toUpperCase();
        }
        return toolkitUsername;
    }

    @Override
    public String toolkitPassword() {
        String toolkitPassword = argExtractor.toolkitPassword();
        String argName = ArgExtractor.ArgName.toolkitPassword.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitPassword = properties.getProperty(argName, toolkitPassword);
        }
        return toolkitPassword;
    }

    @Override
    public String toolkitDomain() {
        String toolkitDomain = argExtractor.toolkitDomain();
        String argName = ArgExtractor.ArgName.toolkitDomain.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitDomain = properties.getProperty(argName, toolkitDomain);
        }
        return toolkitDomain;
    }

    @Override
    protected String toolkitUrl() {
        String toolkitUrl = argExtractor.toolkitUrl();
        String argName = ArgExtractor.ArgName.toolkitUrl.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitUrl = properties.getProperty(argName, toolkitUrl);
        }
        return toolkitUrl;
    }

    @Override
    public String toolkitListName() {
        String toolkitListName = argExtractor.toolkitListName();
        String argName = ArgExtractor.ArgName.toolkitListName.name();
        if (!containsArg(argName) && containsProperty(argName)) {
            toolkitListName = properties.getProperty(argName, toolkitListName);
        }
        return toolkitListName;
    }

    @Override
    public String toolkitUserFolder() {
        return ArgExtractor.ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
    }

}
