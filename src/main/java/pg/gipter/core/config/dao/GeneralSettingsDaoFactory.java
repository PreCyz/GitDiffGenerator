package pg.gipter.core.config.dao;

public final class GeneralSettingsDaoFactory {

    private GeneralSettingsDaoFactory() {}

    public static GeneralSettingsDao getInstance() {
        GeneralSettingsRepository repository = new GeneralSettingsRepository();
        repository.init();
        return repository;
    }
}
