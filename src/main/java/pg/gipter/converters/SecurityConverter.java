package pg.gipter.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.CachedConfiguration;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.SecurityService;
import pg.gipter.utils.CryptoUtils;
import pg.gipter.utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SecurityConverter implements Converter {

    protected final CachedConfiguration cachedConfiguration;
    protected final SecurityService securityService;

    private final Logger logger = LoggerFactory.getLogger(SecurityConverter.class);

    public SecurityConverter() {
        cachedConfiguration = DaoFactory.getCachedConfiguration();
        securityService = SecurityService.getInstance();
    }

    @Override
    public boolean convert() {
        boolean result = false;
        Optional<CipherDetails> cipherDetails = securityService.readCipherDetails();
        if (cipherDetails.isEmpty()) {
            CipherDetails generatedCipher = securityService.generateCipherDetails();
            securityService.writeCipherDetails(generatedCipher);

            Optional<ToolkitConfig> toolkitConfig = readToolkitConfig();
            if (toolkitConfig.isPresent()) {
                String password = toolkitConfig.get().getToolkitSSOPassword();
                String decryptedPassword = null;
                if (!StringUtils.nullOrEmpty(password) && !ArgName.toolkitSSOPassword.defaultValue().equals(password)) {
                    decryptedPassword = CryptoUtils.decryptSafe(password);
                }

                if (!StringUtils.nullOrEmpty(decryptedPassword)) {
                    toolkitConfig.get().setToolkitSSOPassword(decryptedPassword);
                    cachedConfiguration.saveToolkitConfig(toolkitConfig.get());
                    result = true;
                }
            } else {
                logger.info("There is not toolkit credentials to convert.");
            }
        } else {
            logger.info("Security file detected.");
            result = true;
        }
        return result;
    }

    protected Optional<ToolkitConfig> readToolkitConfig() {
        Optional<ToolkitConfig> result = Optional.empty();
        try (InputStream fis = new FileInputStream(DaoConstants.APPLICATION_PROPERTIES_JSON);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)
        ) {
            Gson gson = new GsonBuilder().create();
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            JsonObject toolkitConfigJsonObject = config.getAsJsonObject(ToolkitConfig.TOOLKIT_CONFIG);
            result = Optional.ofNullable(gson.fromJson(toolkitConfigJsonObject, ToolkitConfig.class));
        } catch (IOException | NullPointerException e) {
            logger.warn("Could not read toolkit config. {}", e.getMessage());
        }
        return result;
    }

}
