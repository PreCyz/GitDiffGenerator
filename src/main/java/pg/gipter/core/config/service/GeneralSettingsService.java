package pg.gipter.core.config.service;

import pg.gipter.core.config.dao.GeneralSettingsDao;
import pg.gipter.core.config.dao.GeneralSettingsDaoFactory;

import java.util.Optional;

public class GeneralSettingsService {
    private final GeneralSettingsDao repository;

    private GeneralSettingsService() {
        this.repository = GeneralSettingsDaoFactory.getInstance();
    }

    private static class InstanceHolder {
        private static final GeneralSettingsService INSTANCE = new GeneralSettingsService();
    }

    public static GeneralSettingsService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Optional<String> getGithubToken() {
        return repository.getLatestGithubToken();
    }
}
