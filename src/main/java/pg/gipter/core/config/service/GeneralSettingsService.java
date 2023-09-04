package pg.gipter.core.config.service;

import pg.gipter.core.config.dao.GeneralSettingsDaoFactory;

import java.util.Optional;

public class GeneralSettingsService {
    private GeneralSettingsService() {}

    private static class InstanceHolder {
        private static final GeneralSettingsService INSTANCE = new GeneralSettingsService();
    }

    public static GeneralSettingsService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public Optional<String> getGithubToken() {
        return GeneralSettingsDaoFactory.getInstance().getLatestGithubToken();
    }
}
