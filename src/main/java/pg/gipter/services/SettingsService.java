package pg.gipter.services;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class SettingsService {

    protected final static Logger logger = LoggerFactory.getLogger(SettingsService.class);

    public File downloadAsset(final String assetName, final String fedAuth) throws IOException {
        String url = ArgName.toolkitSiteAssetsUrl.defaultValue() + assetName;
        File destination = Paths.get(".", assetName).toFile();
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("Cookie", fedAuth);

        logger.info("Executing request {}", httpget.getRequestUri());
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            return httpclient.execute(httpget, res -> {
                logger.info("Response: {} {} {}", res.getVersion().format(), res.getCode(), res.getReasonPhrase());
                if (Arrays.asList(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_UNAUTHORIZED).contains(res.getCode())) {
                    throw new IOException("Authentication failed.");
                }
                FileUtils.copyInputStreamToFile(res.getEntity().getContent(), destination);
                EntityUtils.consume(res.getEntity());
                return destination;
            });
        }
    }

}
