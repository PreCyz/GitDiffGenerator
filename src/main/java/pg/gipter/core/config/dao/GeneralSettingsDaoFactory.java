package pg.gipter.core.config.dao;

public final class GeneralSettingsDaoFactory {

    private GeneralSettingsDaoFactory() {}

    public static GeneralSettingsDao getInstance() {
        return new GeneralSettingsRepository();
    }
}
