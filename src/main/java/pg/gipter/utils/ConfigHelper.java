package pg.gipter.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import pg.gipter.settings.ArgName;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

class ConfigHelper {

    final static String APP_CONFIG = "appConfig";
    final static String TOOLKIT_CONFIG = "toolkitConfig";
    final static String RUN_CONFIGS = "runConfigs";

    final static Set<String> APP_CONFIG_PROPERTIES = new HashSet<>();
    final static Set<String> TOOLKIT_CONFIG_PROPERTIES = new HashSet<>();
    final static Set<String> RUN_CONFIG_PROPERTIES = new HashSet<>();

    static {
        APP_CONFIG_PROPERTIES.add(ArgName.configurationName.name());
        APP_CONFIG_PROPERTIES.add(ArgName.preferredArgSource.name());
        APP_CONFIG_PROPERTIES.add(ArgName.useUI.name());
        APP_CONFIG_PROPERTIES.add(ArgName.activeTray.name());
        APP_CONFIG_PROPERTIES.add(ArgName.silentMode.name());
        APP_CONFIG_PROPERTIES.add(ArgName.enableOnStartup.name());

        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitUsername.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitPassword.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitDomain.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitCopyListName.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitUrl.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitCopyCase.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitWSUrl.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitUserFolder.name());
        TOOLKIT_CONFIG_PROPERTIES.add(ArgName.toolkitProjectListNames.name());

        RUN_CONFIG_PROPERTIES.add(ArgName.author.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.gitAuthor.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.mercurialAuthor.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.svnAuthor.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.committerEmail.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.uploadType.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.skipRemote.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.itemPath.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.projectPath.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.itemFileNamePrefix.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.useAsFileName.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.periodInDays.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.startDate.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.endDate.name());
        RUN_CONFIG_PROPERTIES.add(ArgName.configurationName.name());
    }

    @NotNull
    private JsonElement createJsonElement(Properties properties, Set<String> propertiesNameSet) {
        JsonObject appConfig = new JsonObject();
        for (String propertyName : propertiesNameSet) {
            String value = properties.getProperty(propertyName);
            if (!StringUtils.nullOrEmpty(value)) {
                appConfig.addProperty(propertyName, value);
            }
        }
        return appConfig;
    }

    JsonObject buildFullJson(Properties properties) {
        JsonObject result = new JsonObject();
        result.add(APP_CONFIG, buildAppConfig(properties));
        result.add(TOOLKIT_CONFIG, buildToolkitConfig(properties));
        result.add(RUN_CONFIGS, buildRunConfigs(properties));
        return result;
    }

    JsonElement buildAppConfig(Properties properties) {
        return createJsonElement(properties, APP_CONFIG_PROPERTIES);
    }

    JsonElement buildToolkitConfig(Properties properties) {
        return createJsonElement(properties, TOOLKIT_CONFIG_PROPERTIES);
    }

    JsonArray buildRunConfigs(Properties properties) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(createJsonElement(properties, RUN_CONFIG_PROPERTIES));
        return jsonArray;
    }

    JsonElement buildRunConfig(Properties properties) {
        return createJsonElement(properties, RUN_CONFIG_PROPERTIES);
    }
}
