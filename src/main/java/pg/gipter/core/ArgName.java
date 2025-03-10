package pg.gipter.core;

import pg.gipter.FlowType;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.SystemUtils;

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
            return "N";
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
    checkLastItem {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    checkLastItemJobCronExpression {
        @Override
        public String defaultValue() {
            return "0 5 12 20 * ?";
        }
    },
    uiLanguage {
        @Override
        public String defaultValue() {
            return BundleUtils.getDefaultLanguage();
        }
    },
    githubToken {
        @Override
        public String defaultValue() {
            return "";
        }
    },
    toolkitUsername {
        @Override
        public String defaultValue() {
            return SystemUtils.userName();
        }
    },
    toolkitFolderName {
        @Override
        public String defaultValue() {
            return SystemUtils.userName();
        }
    },
    toolkitCopyListName {
        @Override
        public String defaultValue() {
            return "WorkItems";
        }
    },
    toolkitHostUrl {
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
    toolkitUserFolderUrl {
        @Override
        public String defaultValue() {
            return toolkitHostUrl.defaultValue() + toolkitCopyCase.defaultValue() +
                    "/Lists/" + toolkitCopyListName.defaultValue() + "/";
        }
    },
    toolkitSiteAssetsUrl {
        @Override
        public String defaultValue() {
            return toolkitHostUrl.defaultValue() + toolkitCopyCase.defaultValue() + "/SiteAssets/";
        }
    },
    emailDomain {
        @Override
        public String defaultValue() {
            return "@netcompany.com";
        }
    },
    fetchTimeout {
        @Override
        public String defaultValue() {
            return "60";
        }
    },
    uploadItem {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    smartZip {
        @Override
        public String defaultValue() {
            return "Y";
        }
    },
    noSSO {
        @Override
        public String defaultValue() {
            return "N";
        }
    },
    flowType {
        @Override
        public String defaultValue() {
            return FlowType.REGULAR.name();
        }
    };

    public abstract String defaultValue();
}
