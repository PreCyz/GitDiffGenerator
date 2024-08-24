package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.Arrays;

public class SettingsService {

    protected final static Logger logger = LoggerFactory.getLogger(SettingsService.class);

    public File downloadAsset(final String assetName, final String fedAuth) throws IOException {
        String url = ArgName.toolkitSiteAssetsUrl.defaultValue() + assetName;
        File destination = Paths.get(".", assetName).toFile();

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Cookie", fedAuth)
                .build();

        try {
            HttpResponse<InputStream> res = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            logger.info("Response [{}]: {} {}", url, res.version().name(), res.statusCode());
            if (Arrays.asList(403, 401).contains(res.statusCode())) {
                throw new IOException("Authentication failed.");
            }
            Files.copy(res.body(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            res.body().close();
            return destination;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
