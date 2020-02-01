package pg.gipter.settings.dto;

import org.slf4j.event.Level;
import pg.gipter.settings.ArgName;
import pg.gipter.settings.PreferredArgSource;
import pg.gipter.utils.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

public class ApplicationConfig {

    private Boolean confirmationWindow;
    private PreferredArgSource preferredArgSource;
    private Boolean useUI;
    private Boolean activeTray;
    private Boolean silentMode;
    private Boolean enableOnStartup;
    private transient Level loggingLevel;
    private Boolean upgradeFinished;

    public ApplicationConfig() {
        confirmationWindow = StringUtils.getBoolean(ArgName.confirmationWindow.defaultValue());
        preferredArgSource = PreferredArgSource.valueFor(ArgName.preferredArgSource.defaultValue());
        useUI = StringUtils.getBoolean(ArgName.useUI.defaultValue());
        activeTray = StringUtils.getBoolean(ArgName.activeTray.defaultValue());
        silentMode = StringUtils.getBoolean(ArgName.silentMode.defaultValue());
        enableOnStartup = StringUtils.getBoolean(ArgName.enableOnStartup.defaultValue());
        loggingLevel = Level.INFO;
        upgradeFinished = StringUtils.getBoolean(ArgName.upgradeFinished.defaultValue());
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
        return arguments.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "ApplicationConfig{" +
                "confirmationWindow=" + confirmationWindow +
                ", preferredArgSource=" + preferredArgSource +
                ", useUI=" + useUI +
                ", activeTray=" + activeTray +
                ", silentMode=" + silentMode +
                ", enableOnStartup=" + enableOnStartup +
                ", loggingLevel=" + loggingLevel +
                ", upgradeFinished=" + upgradeFinished +
                '}';
    }
}
