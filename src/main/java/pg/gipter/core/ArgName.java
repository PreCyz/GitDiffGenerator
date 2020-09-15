package pg.gipter.core;

import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDate;

import static pg.gipter.core.ApplicationProperties.yyyy_MM_dd;

public enum ArgName {

    author {
        @Override
        public String defaultValue() {
            return "NO_AUTHORS";
        }
    },
    gitAuthor {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    mercurialAuthor {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    svnAuthor {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    committerEmail {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    itemType {
        @Override
        public String defaultValue() {
            return ItemType.SIMPLE.name();
        }
    },
    skipRemote {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    fetchAll {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    toolkitProjectListNames {
        @Override
        public String defaultValue() {
            return "Deliverables";
        }
    },
    deleteDownloadedFiles {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    configurationName {
        @Override
        public String defaultValue() {
            return "";
        }
    },

    itemPath {
        @Override
        public String defaultValue() {
            return "NO_ITEM_PATH";
        }
    },
    projectPath {
        @Override
        public String defaultValue() {
            return "NO_PROJECT_PATH";
        }
    },
    itemFileNamePrefix {
        @Override
        public String defaultValue() {
            return "";
        }
    },

    periodInDays {
        @Override
        public String defaultValue() {
            return "7";
        }
    },
    startDate {
        @Override
        public String defaultValue() {
            return LocalDate.now().minusDays(Integer.parseInt(periodInDays.defaultValue())).format(yyyy_MM_dd);
        }
    },
    endDate {
        @Override
        public String defaultValue() {
            return LocalDate.now().format(yyyy_MM_dd);
        }
    },

    confirmationWindow {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    preferredArgSource {
        @Override
        public String defaultValue() {
            return PreferredArgSource.CLI.name();
        }
    },
    useUI {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    activeTray {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    silentMode {
        @Override
        public String defaultValue() {
            return "N";
        }
    },
    upgradeFinished {
        @Override
        public String defaultValue() {
            return "N";
        }
    },
    enableOnStartup {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    loggerLevel {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    certImport {
        @Override
        public String defaultValue() {
            return "N";
        }
    },
    uiLanguage {
        @Override
        public String defaultValue() {
            return BundleUtils.getDefaultLanguage();
        }
    },

    toolkitUsername {
        @Override
        public String defaultValue() {
            return "UNKNOWN_USER";
        }
    },
    toolkitPassword {
        @Override
        public String defaultValue() {
            return "UNKNOWN";
        }
    },
    toolkitDomain {
        @Override
        public String defaultValue() {
            return "NCDMZ";
        }
    },
    toolkitCopyListName {
        @Override
        public String defaultValue() {
            return "WorkItems";
        }
    },
    toolkitUrl {
        @Override
        public String defaultValue() {
            return "https://goto.netcompany.com";
        }
    },
    toolkitCopyCase {
        @Override
        public String defaultValue() {
            return "/cases/GTE106/NCSCOPY";
        }
    },
    toolkitWSUrl {
        @Override
        public String defaultValue() {
            return toolkitUrl.defaultValue() + toolkitCopyCase.defaultValue() + "/_vti_bin/lists.asmx";
        }
    },
    toolkitUserFolder {
        @Override
        public String defaultValue() {
            return toolkitUrl.defaultValue() + toolkitCopyCase.defaultValue() +
                    "/Lists/" + toolkitCopyListName.defaultValue() + "/";
        }
    };

    public abstract String defaultValue();
}
