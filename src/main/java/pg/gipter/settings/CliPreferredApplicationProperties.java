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
        if (authors.contains(ArgExtractor.ArgName.author.defaultValue()) && hasProperties()) {
            String author = properties.getProperty(
                    ArgExtractor.ArgName.author.name(), String.join(",", argExtractor.authors())
            );
            authors = Stream.of(author.split(",")).collect(toCollection(LinkedHashSet::new));
        }
        return authors;
    }

    @Override
    public String gitAuthor() {
        String gitAuthor = argExtractor.gitAuthor();
        if (StringUtils.nullOrEmpty(gitAuthor) && hasProperties()) {
            gitAuthor = properties.getProperty(ArgExtractor.ArgName.gitAuthor.name(), argExtractor.gitAuthor());
        }
        return gitAuthor;
    }

    @Override
    public String mercurialAuthor() {
        String mercurialAuthor = argExtractor.mercurialAuthor();
        if (StringUtils.nullOrEmpty(mercurialAuthor) && hasProperties()) {
            mercurialAuthor = properties.getProperty(ArgExtractor.ArgName.mercurialAuthor.name(), argExtractor.mercurialAuthor());
        }
        return mercurialAuthor;
    }

    @Override
    public String svnAuthor() {
        String svnAuthor = argExtractor.svnAuthor();
        if (StringUtils.nullOrEmpty(svnAuthor) && hasProperties()) {
            svnAuthor = properties.getProperty(ArgExtractor.ArgName.svnAuthor.name(), argExtractor.svnAuthor());
        }
        return svnAuthor;
    }

    @Override
    public String itemPath() {
        String itemPath = ArgExtractor.ArgName.itemPath.defaultValue();
        if (argExtractor.hasArgs()) {
            itemPath = argExtractor.itemPath();
        }
        if (itemPath.startsWith(ArgExtractor.ArgName.itemPath.defaultValue()) && hasProperties()) {
            itemPath = properties.getProperty(ArgExtractor.ArgName.itemPath.name(), argExtractor.itemPath());
        }
        return codeProtection() == CodeProtection.STATEMENT ? itemPath : itemPath + File.separator + fileName();
    }

    @Override
    String itemFileNamePrefix() {
        String itemFileNamePrefix = argExtractor.itemFileNamePrefix();
        if (itemFileNamePrefix.isEmpty() && hasProperties()) {
            itemFileNamePrefix = properties.getProperty(ArgExtractor.ArgName.itemFileNamePrefix.name(), argExtractor.itemFileNamePrefix());
        }
        return itemFileNamePrefix;
    }

    @Override
    public Set<String> projectPaths() {
        Set<String> projectPaths = argExtractor.projectPaths();
        if (projectPaths.contains(ArgExtractor.ArgName.projectPath.defaultValue()) && hasProperties()) {
            String[] projectPathsArray = properties.getProperty(
                    ArgExtractor.ArgName.projectPath.name(), ArgExtractor.ArgName.projectPath.defaultValue()
            ).split(",");
            projectPaths = new LinkedHashSet<>(Arrays.asList(projectPathsArray));
        }
        return projectPaths;
    }

    @Override
    int periodInDays() {
        if (hasProperties()) {
            return Math.abs(Integer.parseInt(properties.getProperty(
                    ArgExtractor.ArgName.periodInDays.name(), String.valueOf(argExtractor.periodInDays())
            )));
        }
        return argExtractor.periodInDays();
    }

    @Override
    public String committerEmail() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.committerEmail.name(), argExtractor.committerEmail());
        }
        return argExtractor.committerEmail();
    }

    @Override
    public LocalDate startDate() {
        if (hasProperties()) {
            String[] date = properties.getProperty(
                    ArgExtractor.ArgName.startDate.name(), LocalDate.now().minusDays(periodInDays()).format(yyyy_MM_dd)
            ).split("-");
            return LocalDate.of(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
        }
        return argExtractor.startDate();
    }

    @Override
    public LocalDate endDate() {
        if (hasProperties()) {
            String[] date = argExtractor.endDate().format(yyyy_MM_dd).split("-");
            String endDateStr = properties.getProperty(ArgExtractor.ArgName.endDate.name());
            if (StringUtils.notEmpty(endDateStr)) {
                date = endDateStr.split("-");
            }
            return LocalDate.of(Integer.valueOf(date[0]), Integer.valueOf(date[1]), Integer.valueOf(date[2]));
        }
        return argExtractor.endDate();
    }

    @Override
    public CodeProtection codeProtection() {
        if (hasProperties()) {
            String codeProtected = properties.getProperty(
                    ArgExtractor.ArgName.codeProtection.name(), ArgExtractor.ArgName.codeProtection.defaultValue()
            );
            return CodeProtection.valueFor(codeProtected);
        }
        return argExtractor.codeProtection();
    }

    @Override
    public boolean isConfirmation() {
        if (hasProperties()) {
            return StringUtils.getBoolean(properties.getProperty(
                    ArgExtractor.ArgName.confirmationWindow.name(), ArgExtractor.ArgName.confirmationWindow.defaultValue()
            ));
        }
        return argExtractor.isConfirmation();
    }

    @Override
    public String toolkitUsername() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitUsername.name(), argExtractor.toolkitUsername()).trim().toUpperCase();
        }
        return argExtractor.toolkitUsername();
    }

    @Override
    public String toolkitPassword() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitPassword.name(), argExtractor.toolkitPassword());
        }
        return argExtractor.toolkitPassword();
    }

    @Override
    public String toolkitDomain() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitDomain.name(), argExtractor.toolkitDomain());
        }
        return argExtractor.toolkitDomain();
    }

    @Override
    protected String toolkitUrl() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitUrl.name(), argExtractor.toolkitUrl());
        }
        return argExtractor.toolkitUrl();
    }

    @Override
    public String toolkitListName() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.toolkitListName.name(), argExtractor.toolkitListName());
        }
        return argExtractor.toolkitListName();
    }

    @Override
    public String toolkitUserFolder() {
        if (hasProperties()) {
            return ArgExtractor.ArgName.toolkitUserFolder.defaultValue() + toolkitUsername();
        }
        return argExtractor.toolkitUserFolder();
    }

}
