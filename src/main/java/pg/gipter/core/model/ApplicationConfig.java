package pg.gipter.core.model;

import org.slf4j.event.Level;
import pg.gipter.core.ArgName;
import pg.gipter.core.PreferredArgSource;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.StringUtils;

import java.util.*;

public class ApplicationConfig {

    private Boolean confirmationWindow;
    private PreferredArgSource preferredArgSource;
    private Boolean useUI;
    private Boolean noSSO;
    private Boolean activeTray;
    private Boolean silentMode;
    private Boolean enableOnStartup;
    private transient Level loggingLevel;
    private Boolean upgradeFinished;
    private String uiLanguage;
    private Boolean certImportEnabled;
    private Boolean checkLastItemEnabled;
    private String checkLastItemJobCronExpression;
    private Set<CustomCommand> customCommands;
    private Boolean uploadItem;
    private Boolean smartZip;
    private String githubToken;

    public ApplicationConfig() {
        confirmationWindow = StringUtils.getBoolean(ArgName.confirmationWindow.defaultValue());
        preferredArgSource = PreferredArgSource.valueFor(ArgName.preferredArgSource.defaultValue());
        useUI = StringUtils.getBoolean(ArgName.useUI.defaultValue());
        noSSO = StringUtils.getBoolean(ArgName.noSSO.defaultValue());
        activeTray = StringUtils.getBoolean(ArgName.activeTray.defaultValue());
        silentMode = StringUtils.getBoolean(ArgName.silentMode.defaultValue());
        enableOnStartup = StringUtils.getBoolean(ArgName.enableOnStartup.defaultValue());
        loggingLevel = Level.INFO;
        upgradeFinished = StringUtils.getBoolean(ArgName.upgradeFinished.defaultValue());
        uiLanguage = BundleUtils.getDefaultLanguage();
        checkLastItemEnabled = StringUtils.getBoolean(ArgName.checkLastItem.defaultValue());
        checkLastItemJobCronExpression = ArgName.checkLastItemJobCronExpression.defaultValue();
        uploadItem = StringUtils.getBoolean(ArgName.uploadItem.defaultValue());
        smartZip = StringUtils.getBoolean(ArgName.smartZip.defaultValue());
        githubToken = ArgName.githubToken.defaultValue();
    }

    public Boolean getConfirmationWindow() {
        return confirmationWindow;
    }

    public void setConfirmationWindow(Boolean confirmationWindow) {
        this.confirmationWindow = confirmationWindow;
    }

    public PreferredArgSource getPreferredArgSource() {
        return preferredArgSource;
    }

    public void setPreferredArgSource(PreferredArgSource preferredArgSource) {
        this.preferredArgSource = preferredArgSource;
    }

    public Boolean getUseUI() {
        return useUI;
    }

    public void setUseUI(Boolean useUI) {
        this.useUI = useUI;
    }

    public Boolean getNoSSO() {
        return noSSO;
    }

    public void setNoSSO(Boolean noSSO) {
        this.noSSO = noSSO;
    }

    public Boolean getActiveTray() {
        return activeTray;
    }

    public void setActiveTray(Boolean activeTray) {
        this.activeTray = activeTray;
    }

    public Boolean getSilentMode() {
        return silentMode;
    }

    public void setSilentMode(Boolean silentMode) {
        this.silentMode = silentMode;
    }

    public Boolean getEnableOnStartup() {
        return enableOnStartup;
    }

    public void setEnableOnStartup(Boolean enableOnStartup) {
        this.enableOnStartup = enableOnStartup;
    }

    public Level getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public Boolean getUpgradeFinished() {
        return upgradeFinished;
    }

    public void setUpgradeFinished(Boolean upgradeFinished) {
        this.upgradeFinished = upgradeFinished;
    }

    public String getUiLanguage() {
        return uiLanguage;
    }

    public void setUiLanguage(String uiLanguage) {
        this.uiLanguage = uiLanguage;
    }

    public Boolean getCertImportEnabled() {
        return certImportEnabled;
    }

    public void setCertImportEnabled(Boolean certImportEnabled) {
        this.certImportEnabled = certImportEnabled;
    }

    public Boolean getCheckLastItemEnabled() {
        return checkLastItemEnabled;
    }

    public void setCheckLastItemEnabled(Boolean checkLastItemEnabled) {
        this.checkLastItemEnabled = checkLastItemEnabled;
    }

    public String getCheckLastItemJobCronExpression() {
        return checkLastItemJobCronExpression;
    }

    public void setCheckLastItemJobCronExpression(String checkLastItemJobCronExpression) {
        this.checkLastItemJobCronExpression = checkLastItemJobCronExpression;
    }

    public Set<CustomCommand> getCustomCommands() {
        return customCommands;
    }

    public void setCustomCommands(Set<CustomCommand> customCommands) {
        this.customCommands = customCommands;
    }

    public Boolean getUploadItem() {
        return uploadItem;
    }

    public void setUploadItem(Boolean uploadItem) {
        this.uploadItem = uploadItem;
    }

    public Boolean getSmartZip() {
        return smartZip;
    }

    public void setSmartZip(Boolean smartZip) {
        this.smartZip = smartZip;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public String[] toArgumentArray() {
        Collection<String> arguments = new LinkedHashSet<>();
        if (getLoggingLevel() != null) {
            arguments.add(ArgName.loggerLevel.name() + "=" + getLoggingLevel());
        }
        if (getUpgradeFinished() != null) {
            arguments.add(ArgName.upgradeFinished.name() + "=" + getUpgradeFinished());
        }
        if (getUseUI() != null) {
            arguments.add(ArgName.useUI.name() + "=" + getUseUI());
        }
        if (getNoSSO() != null) {
            arguments.add(ArgName.noSSO.name() + "=" + getNoSSO());
        }
        if (getConfirmationWindow() != null) {
            arguments.add(ArgName.confirmationWindow.name() + "=" + getConfirmationWindow());
        }
        if (getEnableOnStartup() != null) {
            arguments.add(ArgName.enableOnStartup.name() + "=" + getEnableOnStartup());
        }
        if (getPreferredArgSource() != null) {
            arguments.add(ArgName.preferredArgSource.name() + "=" + getPreferredArgSource());
        }
        if (getActiveTray() != null) {
            arguments.add(ArgName.activeTray.name() + "=" + getActiveTray());
        }
        if (getSilentMode() != null) {
            arguments.add(ArgName.silentMode.name() + "=" + getSilentMode());
        }
        if (getUiLanguage() != null) {
            arguments.add(ArgName.uiLanguage.name() + "=" + getUiLanguage());
        }
        if (getCheckLastItemEnabled() != null) {
            arguments.add(ArgName.checkLastItem.name() + "=" + getCheckLastItemEnabled());
        }
        if (getCheckLastItemJobCronExpression() != null) {
            arguments.add(ArgName.checkLastItemJobCronExpression.name() + "=" + getCheckLastItemEnabled());
        }
        if (getUploadItem() != null) {
            arguments.add(ArgName.uploadItem.name() + "=" + getUploadItem());
        }
        if (getSmartZip() != null) {
            arguments.add(ArgName.smartZip.name() + "=" + getSmartZip());
        }
        if (getGithubToken() != null) {
            arguments.add(ArgName.githubToken.name() + "=" + getGithubToken());
        }
        return arguments.toArray(new String[0]);
    }

    public static ApplicationConfig valueFrom(String[] args) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length > 1) {
                String argumentName = split[0];
                String argumentValue = split[1];
                if (ArgName.confirmationWindow.name().equals(argumentName)) {
                    applicationConfig.setConfirmationWindow(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.preferredArgSource.name().equals(argumentName)) {
                    applicationConfig.setPreferredArgSource(PreferredArgSource.valueFor(argumentValue));
                } else if (ArgName.useUI.name().equals(argumentName)) {
                    applicationConfig.setUseUI(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.noSSO.name().equals(argumentName)) {
                    applicationConfig.setNoSSO(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.activeTray.name().equals(argumentName)) {
                    applicationConfig.setActiveTray(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.silentMode.name().equals(argumentName)) {
                    applicationConfig.setSilentMode(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.enableOnStartup.name().equals(argumentName)) {
                    applicationConfig.setEnableOnStartup(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.loggerLevel.name().equals(argumentName)) {
                    applicationConfig.setLoggingLevel(Level.valueOf(argumentValue.toUpperCase()));
                } else if (ArgName.upgradeFinished.name().equals(argumentName)) {
                    applicationConfig.setUpgradeFinished(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.uiLanguage.name().equals(argumentName)) {
                    applicationConfig.setUiLanguage(argumentValue);
                } else if (ArgName.checkLastItem.name().equals(argumentName)) {
                    applicationConfig.setCheckLastItemEnabled(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.checkLastItemJobCronExpression.name().equals(argumentName)) {
                    applicationConfig.setCheckLastItemJobCronExpression(argumentValue);
                } else if (ArgName.uploadItem.name().equals(argumentName)) {
                    applicationConfig.setUploadItem(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.smartZip.name().equals(argumentName)) {
                    applicationConfig.setSmartZip(StringUtils.getBoolean(argumentValue));
                } else if (ArgName.githubToken.name().equals(argumentName)) {
                    applicationConfig.setGithubToken(argumentValue);
                }
            }
        }
        return applicationConfig;
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "confirmationWindow=" + confirmationWindow +
                ", preferredArgSource=" + preferredArgSource +
                ", useUI=" + useUI +
                ", noSSO=" + noSSO +
                ", activeTray=" + activeTray +
                ", silentMode=" + silentMode +
                ", enableOnStartup=" + enableOnStartup +
                ", loggingLevel=" + loggingLevel +
                ", upgradeFinished=" + upgradeFinished +
                ", uiLanguage=" + uiLanguage +
                ", checkLastItemEnabled=" + checkLastItemEnabled +
                ", checkLastItemJobCronExpression=" + checkLastItemJobCronExpression +
                ", uploadItem=" + uploadItem +
                ", smartZip=" + smartZip +
                ", githubToken=" + Optional.of(githubToken).map(it -> "***").orElseGet(() -> "N/A") +
                '}';
    }
}
