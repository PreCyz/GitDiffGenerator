package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;
import pg.gipter.toolkit.EdgeCdpClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class SettingsService {

    protected final static Logger logger = LoggerFactory.getLogger(SettingsService.class);

    public File downloadAsset(final String assetName) throws IOException {
        String url = ArgName.toolkitSiteAssetsUrl.defaultValue() + assetName;
        File destination = Paths.get(".", assetName).toFile();

        EdgeCdpClient.Response response = EdgeCdpClient.request("GET", url, Map.of(), null);
        logger.info("Response [{}]: {}", url, response.status());
        if (response.status() == 403 || response.status() == 401) {
            throw new IOException("Authentication failed.");
        }
        Files.write(destination.toPath(), response.body());
        return destination;
    }

}