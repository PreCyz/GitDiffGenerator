package pg.gipter.services;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ArgName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class SettingsService {

    protected final static Logger logger = LoggerFactory.getLogger(SettingsService.class);

    public File downloadAsset(final String assetName, final String fedAuth) throws IOException {
        String url = ArgName.toolkitSiteAssetsUrl.defaultValue() + assetName;
        File destination = Paths.get(".", assetName).toFile();
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("Cookie", fedAuth);

        logger.info("Executing request {}", httpget.getRequestLine());
        try (CloseableHttpClient httpclient = HttpClients.custom().build();
             CloseableHttpResponse response = httpclient.execute(httpget);
        ) {
            logger.info("Response {}", response.getStatusLine());
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), destination);
            EntityUtils.consume(response.getEntity());
            return destination;
        }
    }

}
