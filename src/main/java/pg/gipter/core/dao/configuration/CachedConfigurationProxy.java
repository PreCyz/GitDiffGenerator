package pg.gipter.core.dao.configuration;

import pg.gipter.core.model.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;

class CachedConfigurationProxy extends ApplicationJsonReader implements CachedConfiguration {

    private final ConfigurationDao configurationDao;
    private Configuration cachedConfiguration;

    private static class CachedConfigurationProxyHolder {
        private static final CachedConfigurationProxy INSTANCE = new CachedConfigurationProxy();
    }

    private CachedConfigurationProxy() {
        this.configurationDao = ConfigurationDaoFactory.getConfigurationDao();
    }

    public static CachedConfigurationProxy getInstance() {
        return CachedConfigurationProxyHolder.INSTANCE;
    }

    @Override
    public void resetCache() {
        cachedConfiguration = null;
    }

    private void initializeCacheIfEmpty() {
        if (cachedConfiguration == null) {
            cachedConfiguration = Optional.ofNullable(readJsonConfig()).orElseGet(Configuration::new);
        }
    }

    @Override
    public void removeConfig(String configurationName) {
        initializeCacheIfEmpty();
        configurationDao.removeConfig(configurationName);
        cachedConfiguration.removeRunConfig(configurationName);
        logger.info("Configuration cache updated.");
    }

    @Override
    public String[] loadArgumentArray(String configurationName) {
        return configurationDao.loadArgumentArray(configurationName);
    }

    @Override
    public ToolkitConfig loadToolkitConfig() {
        initializeCacheIfEmpty();
        return Optional.ofNullable(cachedConfiguration.getToolkitConfig()).orElseGet(ToolkitConfig::new);
    }

    @Override
    public void saveToolkitConfig(ToolkitConfig toolkitConfig) {
        initializeCacheIfEmpty();
        configurationDao.saveToolkitConfig(toolkitConfig);
        cachedConfiguration.setToolkitConfig(toolkitConfig);
        logger.info("Configuration cache updated.");
    }

    @Override
    public ApplicationConfig loadApplicationConfig() {
        initializeCacheIfEmpty();
        return Optional.ofNullable(cachedConfiguration.getAppConfig()).orElseGet(ApplicationConfig::new);
    }

    @Override
    public Map<String, RunConfig> loadRunConfigMap() {
        initializeCacheIfEmpty();
        return Optional.ofNullable(cachedConfiguration.getRunConfigs())
                .orElseGet(ArrayList::new)
                .stream()
                .collect(toMap(RunConfig::getConfigurationName, rc -> rc, (v1, v2) -> v1));
    }

    @Override
    public Optional<RunConfig> loadRunConfig(String configurationName) {
        return Optional.ofNullable(loadRunConfigMap().get(configurationName));
    }

    @Override
    public RunConfig getRunConfigFromArray(String[] args) {
        return configurationDao.getRunConfigFromArray(args);
    }

    @Override
    public void saveConfiguration(Configuration configuration) {
        initializeCacheIfEmpty();
        configurationDao.saveConfiguration(configuration);
        cachedConfiguration = configuration;
    }

    public <T> void updateCachedConfiguration(T value) {
        cachedConfiguration = Optional.ofNullable(readJsonConfig()).orElseGet(Configuration::new);
        if (value instanceof ApplicationConfig) {
            cachedConfiguration.setAppConfig((ApplicationConfig) value);
        } else if (value instanceof ToolkitConfig) {
            cachedConfiguration.setToolkitConfig((ToolkitConfig) value);
        } else if (value instanceof RunConfig) {
            cachedConfiguration.addRunConfig((RunConfig) value);
        } else if (value instanceof CipherDetails) {
            cachedConfiguration.setCipherDetails((CipherDetails) value);
        }
    }
}
