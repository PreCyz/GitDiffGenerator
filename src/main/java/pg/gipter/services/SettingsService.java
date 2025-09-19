package pg.gipter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class SettingsService {

    protected final static Logger logger = LoggerFactory.getLogger(SettingsService.class);

    public File downloadAsset(final String assetName) throws IOException {
        String url = ArgName.toolkitSiteAssetsUrl.defaultValue() + assetName;
        File destination = Paths.get(".", assetName).toFile();

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Cookie", CookiesService.getFedAuthString() + "; " + CookiesService.getGotoString())
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
