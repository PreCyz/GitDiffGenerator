package pg.gipter.core.dao.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.Configuration;
import pg.gipter.core.model.RunConfig;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;
import pg.gipter.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

class PasswordSerializer implements JsonSerializer<Configuration> {

    private static class PasswordSerializerHolder {
        private static final PasswordSerializer INSTANCE = new PasswordSerializer();
    }

    private PasswordSerializer() {
    }

    public static PasswordSerializer getInstance() {
        return PasswordSerializerHolder.INSTANCE;
    }

    @Override
    public JsonElement serialize(Configuration configuration, Type type, JsonSerializationContext serializationContext) {
        Configuration result = new Configuration();
        result.setAppConfig(configuration.getAppConfig());
        result.setCipherDetails(configuration.getCipherDetails());
        result.setToolkitConfig(processToolkitConfig(configuration));
        result.setRunConfigs(processRunConfigs(configuration));
        return new Gson().toJsonTree(result, Configuration.class);
    }

    private ToolkitConfig processToolkitConfig(Configuration configuration) {
        ToolkitConfig result = configuration.getToolkitConfig();
        if (configuration.getToolkitConfig() != null) {
            result = new ToolkitConfig(configuration.getToolkitConfig());
        }
        return result;
    }

    private String encryptPassword(CipherDetails cipherDetails, String password) {
        String encryptedPassword;
        if (cipherDetails != null) {
            encryptedPassword = SecurityService.getInstance().encrypt(password, cipherDetails);
        } else {
            encryptedPassword = CryptoUtils.encryptSafe(password);
        }
        return encryptedPassword;
    }

    private List<RunConfig> processRunConfigs(Configuration configuration) {
        List<RunConfig> result = new LinkedList<>();

        if (configuration.getRunConfigs() != null) {
            final Predicate<RunConfig> isValidSharePointConfig = rc -> rc.getItemType() == ItemType.SHARE_POINT_DOCS &&
                    rc.getSharePointConfigs() != null && !rc.getSharePointConfigs().isEmpty();
            for (RunConfig rc : configuration.getRunConfigs()) {
                RunConfig runConfig = new RunConfig(rc);
                if (isValidSharePointConfig.test(rc)) {
                    final Set<SharePointConfig> sharePointConfigSet = runConfig.getSharePointConfigs()
                            .stream()
                            .map(SharePointConfig::new)
                            .collect(toSet());
                    runConfig.setSharePointConfigs(sharePointConfigSet);
                }
                result.add(runConfig);
            }
        }
        return result;
    }

    private String encryptSharePointPassword(CipherDetails cipherDetails, String password) {
        String result = password;
        if (!StringUtils.nullOrEmpty(password)) {
            result = (encryptPassword(cipherDetails, password));
        }
        return result;
    }

}
