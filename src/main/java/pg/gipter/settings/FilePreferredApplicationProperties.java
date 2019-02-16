package pg.gipter.settings;

import pg.gipter.producer.command.CodeProtection;
import pg.gipter.producer.util.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
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
                    ArgExtractor.ArgName.author.name(), String.join(",", argExtractor.authors())
            );
            return Stream.of(authors.split(",")).collect(toCollection(LinkedHashSet::new));
        }
        return argExtractor.authors();
    }

    @Override
    public String gitAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.gitAuthor.name(), argExtractor.gitAuthor());
        }
        return argExtractor.gitAuthor();
    }

    @Override
    public String mercurialAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.mercurialAuthor.name(), argExtractor.mercurialAuthor());
        }
        return argExtractor.mercurialAuthor();
    }

    @Override
    public String svnAuthor() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.svnAuthor.name(), argExtractor.svnAuthor());
        }
        return argExtractor.svnAuthor();
    }

    @Override
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

    @Override
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

    @Override
    String itemFileNamePrefix() {
        if (hasProperties()) {
            return properties.getProperty(ArgExtractor.ArgName.itemFileNamePrefix.name(), argExtractor.itemFileNamePrefix());
        }
        return argExtractor.itemFileNamePrefix();
    }

    @Override
    public Set<String> projectPaths() {
        if (hasProperties()) {
            String[] projectPaths = properties.getProperty(
                    ArgExtractor.ArgName.projectPath.name(), ArgExtractor.ArgName.projectPath.defaultValue()
            ).split(",");
            return new HashSet<>(Arrays.asList(projectPaths));
        }
        return argExtractor.projectPaths();
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
