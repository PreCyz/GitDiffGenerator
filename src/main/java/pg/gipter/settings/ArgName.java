package pg.gipter.settings;

import pg.gipter.producer.command.CodeProtection;

import java.time.LocalDate;

import static pg.gipter.settings.ApplicationProperties.yyyy_MM_dd;

public enum ArgName {

    author("NO_AUTHORS_GIVEN"),
    gitAuthor(""),
    mercurialAuthor(""),
    svnAuthor(""),
    committerEmail(""),
    codeProtection(CodeProtection.NONE.name()),
    skipRemote("Y"),

    itemPath("NO_ITEM_PATH_GIVEN"),
    projectPath("NO_PROJECT_PATH_GIVEN"),
    itemFileNamePrefix(""),

    periodInDays("7"),
    startDate(LocalDate.now().minusDays(Integer.parseInt(periodInDays.defaultValue)).format(yyyy_MM_dd)),
    endDate(LocalDate.now().format(yyyy_MM_dd)),

    confirmationWindow("N"),
    preferredArgSource(PreferredArgSource.CLI.name()),
    useUI("Y"),
    activeTray("Y"),

    toolkitUsername("NO_TOOLKIT_USERNAME_GIVEN"),
    toolkitPassword("NO_TOOLKIT_PASSWORD_GIVEN"),
    toolkitDomain("NCDMZ"),
    toolkitListName("WorkItems"),
    toolkitUrl("https://goto.netcompany.com/cases/GTE106/NCSCOPY"),
    toolkitWSUrl(toolkitUrl.defaultValue + "/_vti_bin/lists.asmx"),
    toolkitUserFolder(toolkitUrl.defaultValue + "/Lists/" + toolkitListName.defaultValue + "/");

    private String defaultValue;

    ArgName(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    String defaultValue() {
        return defaultValue;
    }
}
