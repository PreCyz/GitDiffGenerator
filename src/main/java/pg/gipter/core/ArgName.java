package pg.gipter.core;

import pg.gipter.core.producers.command.ItemType;

import java.time.LocalDate;

import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

public enum ArgName {

    author("NO_AUTHORS"),
    gitAuthor(""),
    mercurialAuthor(""),
    svnAuthor(""),
    committerEmail(""),
    itemType(ItemType.SIMPLE.name()),
    skipRemote("Y"),
    fetchAll("Y"),
    toolkitProjectListNames("Deliverables"),
    deleteDownloadedFiles("Y"),
    configurationName(""),

    itemPath("NO_ITEM_PATH"),
    projectPath("NO_PROJECT_PATH"),
    itemFileNamePrefix(""),

    periodInDays("7"),
    startDate(LocalDate.now().minusDays(Integer.parseInt(periodInDays.defaultValue)).format(yyyy_MM_dd)),
    endDate(LocalDate.now().format(yyyy_MM_dd)),

    confirmationWindow("Y"),
    preferredArgSource(PreferredArgSource.CLI.name()),
    useUI("Y"),
    activeTray("Y"),
    silentMode("N"),
    upgradeFinished("N"),
    enableOnStartup("Y"),
    loggerLevel(""),

    toolkitUsername("UNKNOWN_USER"),
    toolkitPassword("UNKNOWN"),
    toolkitDomain("NCDMZ"),
    toolkitCopyListName("WorkItems"),
    toolkitUrl("https://goto.netcompany.com"),
    toolkitCopyCase("/cases/GTE106/NCSCOPY"),
    toolkitWSUrl(toolkitUrl.defaultValue + toolkitCopyCase.defaultValue + "/_vti_bin/lists.asmx"),
    toolkitUserFolder(toolkitUrl.defaultValue + toolkitCopyCase.defaultValue + "/Lists/" + toolkitCopyListName.defaultValue + "/")
    ;

    private String defaultValue;

    ArgName(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
