package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producer.command.ItemType;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;
import pg.gipter.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

class PasswordSerializer implements JsonSerializer<Configuration> {

    private static class PasswordSerializerHolder {
        private static final PasswordSerializer INSTANCE = new PasswordSerializer();
    }

    private PasswordSerializer() { }

    public static PasswordSerializer getInstance() {
        return PasswordSerializerHolder.INSTANCE;
    }

    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext serializationContext) {
        Configuration result = configuration;
        List<RunConfig> runConfigs = configuration.getRunConfigs();
        if (configuration.getToolkitConfig() != null) {
            ToolkitConfig toolkitConfig = new ToolkitConfig(configuration.getToolkitConfig());
            String password = toolkitConfig.getToolkitPassword();
            if (!StringUtils.nullOrEmpty(password)) {
                String encryptedPassword = encryptPassword(configuration, password);
                toolkitConfig.setToolkitPassword(encryptedPassword);

                result = new Configuration();
                result.setToolkitConfig(toolkitConfig);
                result.setAppConfig(configuration.getAppConfig());
                result.setRunConfigs(runConfigs);
                result.setCipherDetails(configuration.getCipherDetails());
            }
        }

        Predicate<RunConfig> isValidSharePointConfig = rc -> rc.getItemType() == ItemType.SHARE_POINT_DOCS &&
                rc.getSharePointConfigs() != null && !rc.getSharePointConfigs().isEmpty();
        if (runConfigs != null && !runConfigs.isEmpty() && runConfigs.stream().anyMatch(isValidSharePointConfig)) {
            Set<SharePointConfig> sharePointConfigs = runConfigs.stream()
                    .map(RunConfig::getSharePointConfigs)
                    .flatMap(Collection::stream)
                    .collect(toSet());
            sharePointConfigs.forEach(spc -> {
                String password = spc.getPassword();
                if (!StringUtils.nullOrEmpty(password)) {
                    spc.setPassword(encryptPassword(configuration, password));
                }
            });
        }
        return new Gson().toJsonTree(result, Configuration.class);
    }

    private String encryptPassword(Configuration configuration, String password) {
        String encryptedPassword;
        if (configuration.getCipherDetails() != null) {
            encryptedPassword = SecurityService.getInstance().encrypt(password, configuration.getCipherDetails());
        } else {
            encryptedPassword = CryptoUtils.encryptSafe(password);
        }
        return encryptedPassword;
    }

}
